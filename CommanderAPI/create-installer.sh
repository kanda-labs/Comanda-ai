#!/bin/bash

# Script para criar instaladores nativos multiplataforma da CommanderAPI

set -e

echo "🚀 Criando instaladores nativos multiplataforma da CommanderAPI..."

# Build do JAR fat
echo "📦 Construindo JAR fat..."
./gradlew clean fatJar

# Verificar se o JAR foi criado
JAR_FILE="build/libs/CommanderAPI-fat.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Erro: JAR não foi criado em $JAR_FILE"
    exit 1
fi

echo "✅ JAR criado: $(ls -lh $JAR_FILE | awk '{print $5}')"

# Criar diretório de output
OUTPUT_DIR="build/installer"
mkdir -p "$OUTPUT_DIR"

# Testar se jpackage está disponível
if ! command -v jpackage &> /dev/null; then
    echo "❌ jpackage não encontrado. Certifique-se de usar JDK 17+"
    exit 1
fi

echo "🔧 Usando jpackage: $(which jpackage)"
echo "☕ Java version: $(java -version 2>&1 | head -n 1)"
echo ""

# Lista de tipos de instalador para criar
INSTALLER_TYPES=()
SUCCESS_COUNT=0
FAILED_TYPES=()

# Detectar sistema operacional e definir instaladores possíveis
OS=$(uname -s)
case $OS in
    "Darwin")
        echo "🍎 Sistema macOS detectado"
        INSTALLER_TYPES=("app" "dmg")
        ;;
    "Linux")
        echo "🐧 Sistema Linux detectado"
        INSTALLER_TYPES=("deb" "rpm")
        ;;
    "MINGW"*|"MSYS"*|"CYGWIN"*)
        echo "🪟 Sistema Windows detectado"
        INSTALLER_TYPES=("msi" "exe")
        ;;
    *)
        echo "⚠️ Sistema operacional não reconhecido: $OS"
        echo "Tentando criar instaladores universais..."
        INSTALLER_TYPES=("app" "deb" "rpm")
        ;;
esac

# Função para criar instalador
create_installer() {
    local installer_type=$1
    local emoji=""
    local description=""
    
    case $installer_type in
        "dmg")
            emoji="🍎"
            description="macOS DMG"
            ;;
        "app")
            emoji="🍎"
            description="macOS App Bundle"
            ;;
        "deb")
            emoji="🐧"
            description="Linux DEB package"
            ;;
        "rpm")
            emoji="🐧"
            description="Linux RPM package"
            ;;
        "msi")
            emoji="🪟"
            description="Windows MSI installer"
            ;;
        "exe")
            emoji="🪟"
            description="Windows EXE installer"
            ;;
        *)
            emoji="📦"
            description="Generic package"
            ;;
    esac
    
    echo "🏗️ Criando instalador $emoji $description ($installer_type)..."
    
    # Preparar argumentos específicos por tipo
    local extra_args=()
    case $installer_type in
        "dmg"|"app")
            extra_args+=(
                "--mac-package-name" "CommanderAPI"
                "--mac-package-identifier" "co.kandalabs.commander.api"
            )
            ;;
        "deb")
            extra_args+=(
                "--linux-package-name" "commanderapi"
                "--linux-app-category" "Office"
                "--linux-shortcut"
                "--linux-menu-group" "Development"
            )
            ;;
        "rpm")
            extra_args+=(
                "--linux-package-name" "commanderapi"
                "--linux-app-category" "Office"
                "--linux-shortcut"
                "--linux-menu-group" "Development"
            )
            ;;
        "msi"|"exe")
            extra_args+=(
                "--win-console"
                "--win-dir-chooser"
                "--win-menu"
                "--win-shortcut"
            )
            ;;
    esac
    
    # Executar jpackage
    if jpackage \
        --input build/libs \
        --main-jar CommanderAPI-fat.jar \
        --main-class kandalabs.commander.application.ApplicationKt \
        --name CommanderAPI \
        --app-version 1.0 \
        --description "CommanderAPI Restaurant Order Management System" \
        --vendor "KandaLabs" \
        --dest "$OUTPUT_DIR" \
        --type "$installer_type" \
        --java-options "-Xmx512m" \
        --java-options "-Dfile.encoding=UTF-8" \
        "${extra_args[@]}" \
        > /tmp/jpackage-$installer_type.log 2>&1; then
        
        echo "  ✅ $emoji $description criado com sucesso!"
        ((SUCCESS_COUNT++))
        return 0
    else
        echo "  ❌ $emoji Falha ao criar $description"
        echo "     Log salvo em: /tmp/jpackage-$installer_type.log"
        FAILED_TYPES+=("$installer_type")
        return 1
    fi
}

# Criar instaladores para todos os tipos suportados
echo "🎯 Criando instaladores para os seguintes tipos: ${INSTALLER_TYPES[*]}"
echo ""

for installer_type in "${INSTALLER_TYPES[@]}"; do
    create_installer "$installer_type"
    echo ""
done

# Tentar criar instaladores adicionais se estivermos em macOS ou Linux
if [[ "$OS" == "Darwin" ]] || [[ "$OS" == "Linux" ]]; then
    echo "🔄 Tentando criar instaladores adicionais multiplataforma..."
    echo ""
    
    # Adicionar tipos que podem funcionar em qualquer sistema Unix
    ADDITIONAL_TYPES=()
    
    # Se estamos no macOS, tentar criar DEB
    if [[ "$OS" == "Darwin" ]] && [[ ! " ${INSTALLER_TYPES[*]} " =~ " deb " ]]; then
        ADDITIONAL_TYPES+=("deb")
    fi
    
    # Se estamos no Linux, tentar criar APP (pode funcionar)
    if [[ "$OS" == "Linux" ]] && [[ ! " ${INSTALLER_TYPES[*]} " =~ " app " ]]; then
        ADDITIONAL_TYPES+=("app")
    fi
    
    for installer_type in "${ADDITIONAL_TYPES[@]}"; do
        echo "🧪 Tentativa experimental: $installer_type"
        create_installer "$installer_type" || true
        echo ""
    done
fi

echo "=============================================="
echo "📊 RESUMO DA GERAÇÃO DE INSTALADORES"
echo "=============================================="
echo "✅ Instaladores criados com sucesso: $SUCCESS_COUNT"

if [ ${#FAILED_TYPES[@]} -gt 0 ]; then
    echo "❌ Tipos que falharam: ${FAILED_TYPES[*]}"
    echo ""
    echo "💡 Dicas para tipos que falharam:"
    for failed_type in "${FAILED_TYPES[@]}"; do
        case $failed_type in
            "msi"|"exe")
                echo "   - $failed_type: Requer Windows ou Wine configurado"
                ;;
            "deb"|"rpm")
                echo "   - $failed_type: Requer Linux ou ferramentas específicas"
                ;;
            "dmg"|"app")
                echo "   - $failed_type: Requer macOS"
                ;;
        esac
    done
fi

echo ""
echo "📁 Localização dos instaladores: $OUTPUT_DIR"
echo ""
echo "🔍 Arquivos gerados:"
ls -lah "$OUTPUT_DIR" | grep -v "^total"
echo ""

# Gerar instruções de uso para cada arquivo
echo "🚀 INSTRUÇÕES DE USO:"
echo "=============================================="

if ls "$OUTPUT_DIR"/*.jar >/dev/null 2>&1; then
    echo "☕ JAR Executável:"
    echo "   java -jar build/libs/CommanderAPI-fat.jar"
    echo ""
fi

if ls "$OUTPUT_DIR"/*.dmg >/dev/null 2>&1; then
    echo "🍎 macOS (DMG):"
    echo "   1. Duplo clique no arquivo .dmg"
    echo "   2. Arraste CommanderAPI.app para Applications"
    echo "   3. Execute CommanderAPI.app"
    echo ""
fi

if ls "$OUTPUT_DIR"/*.app >/dev/null 2>&1; then
    echo "🍎 macOS (App Bundle):"
    echo "   1. Copie CommanderAPI.app para Applications"
    echo "   2. Execute CommanderAPI.app"
    echo ""
fi

if ls "$OUTPUT_DIR"/*.deb >/dev/null 2>&1; then
    echo "🐧 Linux (DEB):"
    echo "   1. sudo dpkg -i $OUTPUT_DIR/CommanderAPI*.deb"
    echo "   2. sudo apt-get install -f  # se houver dependências"
    echo "   3. commanderapi"
    echo ""
fi

if ls "$OUTPUT_DIR"/*.rpm >/dev/null 2>&1; then
    echo "🐧 Linux (RPM):"
    echo "   1. sudo rpm -i $OUTPUT_DIR/CommanderAPI*.rpm"
    echo "   2. commanderapi"
    echo ""
fi

if ls "$OUTPUT_DIR"/*.msi >/dev/null 2>&1; then
    echo "🪟 Windows (MSI):"
    echo "   1. Duplo clique no arquivo .msi"
    echo "   2. Siga o assistente de instalação"
    echo "   3. Execute CommanderAPI do menu iniciar"
    echo ""
fi

if ls "$OUTPUT_DIR"/*.exe >/dev/null 2>&1; then
    echo "🪟 Windows (EXE):"
    echo "   1. Execute o arquivo .exe"
    echo "   2. Siga o assistente de instalação"
    echo "   3. Execute CommanderAPI do menu iniciar"
    echo ""
fi

echo "🌐 A API estará disponível em: http://localhost:8081"
echo "📊 Swagger UI: http://localhost:8081/swagger-ui"
echo "💗 Health Check: http://localhost:8081/health"
echo ""

if [ $SUCCESS_COUNT -gt 0 ]; then
    echo "🎉 Instaladores prontos para distribuição!"
    exit 0
else
    echo "💥 Nenhum instalador foi criado com sucesso"
    echo "Verifique os logs em /tmp/jpackage-*.log"
    exit 1
fi