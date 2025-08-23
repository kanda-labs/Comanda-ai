#!/bin/bash

# Script para criar TODOS os instaladores possíveis da CommanderAPI
# Este script tenta criar instaladores para Windows, Linux e macOS

set -e

echo "🚀 Criando instaladores MULTIPLATAFORMA da CommanderAPI..."
echo "==========================================================="

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

# Detectar sistema operacional atual
OS=$(uname -s)
echo "💻 Sistema operacional: $OS"
echo ""

# Função para testar se um tipo de instalador é suportado
test_installer_type() {
    local type=$1
    echo "🧪 Testando suporte para tipo '$type'..."
    
    jpackage \
        --input build/libs \
        --main-jar CommanderAPI-fat.jar \
        --main-class kandalabs.commander.application.ApplicationKt \
        --name TestApp \
        --app-version 1.0 \
        --dest /tmp/test-jpackage \
        --type "$type" \
        --dry-run \
        > /tmp/test-$type.log 2>&1
    
    local result=$?
    rm -rf /tmp/test-jpackage /tmp/TestApp* 2>/dev/null || true
    return $result
}

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
        "app-image")
            emoji="📱"
            description="Portable App Directory"
            ;;
        *)
            emoji="📦"
            description="Package ($installer_type)"
            ;;
    esac
    
    echo "🏗️ Criando instalador $emoji $description..."
    
    # Preparar argumentos específicos por tipo
    local extra_args=()
    case $installer_type in
        "dmg")
            extra_args+=(
                "--mac-package-name" "CommanderAPI"
                "--mac-package-identifier" "co.kandalabs.commander.api"
            )
            ;;
        "deb")
            extra_args+=(
                "--linux-package-name" "commanderapi"
                "--linux-app-category" "Development"
                "--linux-shortcut"
                "--linux-menu-group" "Development"
            )
            ;;
        "rpm")
            extra_args+=(
                "--linux-package-name" "commanderapi"
                "--linux-app-category" "Development"
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
    
    # Executar jpackage com timeout
    if timeout 300 jpackage \
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
        > "/tmp/jpackage-$installer_type.log" 2>&1; then
        
        echo "  ✅ $emoji $description criado com sucesso!"
        return 0
    else
        echo "  ❌ $emoji Falha ao criar $description"
        echo "     Log disponível em: /tmp/jpackage-$installer_type.log"
        echo "     Primeiras linhas do erro:"
        head -n 5 "/tmp/jpackage-$installer_type.log" | sed 's/^/       /'
        return 1
    fi
}

# Lista de todos os tipos de instalador para testar
ALL_INSTALLER_TYPES=("app-image" "dmg" "deb" "rpm" "msi" "exe")
SUPPORTED_TYPES=()
SUCCESS_COUNT=0

echo "🔍 Descobrindo tipos de instalador suportados neste sistema..."
echo ""

# Testar cada tipo para ver se é suportado
for type in "${ALL_INSTALLER_TYPES[@]}"; do
    if test_installer_type "$type"; then
        echo "  ✅ $type - SUPORTADO"
        SUPPORTED_TYPES+=("$type")
    else
        echo "  ❌ $type - não suportado neste sistema"
    fi
done

echo ""
echo "📋 Tipos suportados detectados: ${SUPPORTED_TYPES[*]}"
echo ""

if [ ${#SUPPORTED_TYPES[@]} -eq 0 ]; then
    echo "💥 Nenhum tipo de instalador suportado encontrado!"
    echo "Isso pode indicar um problema com jpackage ou Java."
    exit 1
fi

# Criar instaladores para todos os tipos suportados
echo "🎯 Criando instaladores para ${#SUPPORTED_TYPES[@]} tipos..."
echo ""

for installer_type in "${SUPPORTED_TYPES[@]}"; do
    if create_installer "$installer_type"; then
        ((SUCCESS_COUNT++))
    fi
    echo ""
done

# Se estivermos no macOS, tentar criar instaladores Linux (experimentalmente)
if [[ "$OS" == "Darwin" ]]; then
    echo "🧪 EXPERIMENTO: Tentando criar instaladores Linux no macOS..."
    echo "(Isso pode funcionar se as ferramentas estiverem disponíveis)"
    echo ""
    
    for linux_type in "deb" "rpm"; do
        if [[ ! " ${SUPPORTED_TYPES[*]} " =~ " $linux_type " ]]; then
            echo "🔬 Tentativa experimental: $linux_type"
            if create_installer "$linux_type"; then
                ((SUCCESS_COUNT++))
            fi
            echo ""
        fi
    done
fi

echo "=============================================="
echo "📊 RESUMO FINAL"
echo "=============================================="
echo "✅ Instaladores criados com sucesso: $SUCCESS_COUNT"
echo "📁 Localização: $OUTPUT_DIR"
echo ""

if [ $SUCCESS_COUNT -gt 0 ]; then
    echo "🔍 ARQUIVOS GERADOS:"
    ls -lah "$OUTPUT_DIR" | grep -E '\.(dmg|deb|rpm|msi|exe)$|CommanderAPI$' | while read line; do
        echo "   $line"
    done
    echo ""
    
    echo "📦 INSTRUÇÕES DE DISTRIBUIÇÃO:"
    echo "=============================================="
    
    # Gerar instruções específicas para cada arquivo encontrado
    if ls "$OUTPUT_DIR"/*.dmg >/dev/null 2>&1; then
        echo "🍎 macOS (DMG):"
        echo "   • Distribuir: $(ls "$OUTPUT_DIR"/*.dmg | head -1 | xargs basename)"
        echo "   • Instalação: Duplo clique → Arrastar para Applications"
        echo ""
    fi
    
    if ls "$OUTPUT_DIR"/*.deb >/dev/null 2>&1; then
        echo "🐧 Ubuntu/Debian (DEB):"
        echo "   • Distribuir: $(ls "$OUTPUT_DIR"/*.deb | head -1 | xargs basename)"
        echo "   • Instalação: sudo dpkg -i [arquivo.deb]"
        echo "   • Executar: commanderapi"
        echo ""
    fi
    
    if ls "$OUTPUT_DIR"/*.rpm >/dev/null 2>&1; then
        echo "🐧 RedHat/CentOS (RPM):"
        echo "   • Distribuir: $(ls "$OUTPUT_DIR"/*.rpm | head -1 | xargs basename)"
        echo "   • Instalação: sudo rpm -i [arquivo.rpm]"
        echo "   • Executar: commanderapi"
        echo ""
    fi
    
    if ls "$OUTPUT_DIR"/*.msi >/dev/null 2>&1; then
        echo "🪟 Windows (MSI):"
        echo "   • Distribuir: $(ls "$OUTPUT_DIR"/*.msi | head -1 | xargs basename)"
        echo "   • Instalação: Duplo clique → Seguir wizard"
        echo ""
    fi
    
    if ls "$OUTPUT_DIR"/*.exe >/dev/null 2>&1; then
        echo "🪟 Windows (EXE):"
        echo "   • Distribuir: $(ls "$OUTPUT_DIR"/*.exe | head -1 | xargs basename)"
        echo "   • Instalação: Executar → Seguir wizard"
        echo ""
    fi
    
    if ls "$OUTPUT_DIR"/CommanderAPI >/dev/null 2>&1; then
        echo "📱 Portable App:"
        echo "   • Distribuir: Pasta CommanderAPI completa"
        echo "   • Execução: ./CommanderAPI/bin/CommanderAPI"
        echo ""
    fi
    
    # JAR sempre disponível
    echo "☕ JAR Universal:"
    echo "   • Distribuir: build/libs/CommanderAPI-fat.jar"
    echo "   • Execução: java -jar CommanderAPI-fat.jar"
    echo ""
    
    echo "🌐 Após instalação, a API estará em:"
    echo "   • URL: http://localhost:8081"
    echo "   • Swagger: http://localhost:8081/swagger-ui"
    echo "   • Health: http://localhost:8081/health"
    echo ""
    
    echo "🎉 Distribuição multiplataforma PRONTA!"
else
    echo "💥 Nenhum instalador foi criado com sucesso!"
    echo ""
    echo "📋 Para debug, verifique os logs:"
    ls -1 /tmp/jpackage-*.log 2>/dev/null | while read log; do
        echo "   $log"
    done
fi

echo ""
echo "⚡ Para regenerar: ./create-all-installers.sh"

exit $([[ $SUCCESS_COUNT -gt 0 ]] && echo 0 || echo 1)