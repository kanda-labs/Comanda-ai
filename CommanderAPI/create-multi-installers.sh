#!/bin/bash

# Script final para criar instaladores multiplataforma da CommanderAPI
# Versão compatível com macOS, Linux e Windows

set -e

echo "🚀 CRIADOR DE INSTALADORES MULTIPLATAFORMA"
echo "=========================================="
echo "CommanderAPI - Restaurant Order Management"
echo ""

# Build do JAR fat
echo "📦 Construindo JAR executável..."
./gradlew clean fatJar

JAR_FILE="build/libs/CommanderAPI-fat.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ ERRO: JAR não foi criado em $JAR_FILE"
    exit 1
fi

echo "✅ JAR criado com sucesso: $(ls -lh $JAR_FILE | awk '{print $5}')"

# Preparar ambiente
OUTPUT_DIR="build/installer"
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

OS=$(uname -s)
echo "💻 Sistema operacional: $OS"
echo "☕ Java: $(java -version 2>&1 | head -n 1)"
echo ""

# Verificar jpackage
if ! command -v jpackage &> /dev/null; then
    echo "❌ ERRO: jpackage não encontrado"
    echo "         Você precisa do JDK 14+ para criar instaladores nativos"
    echo "         Apenas o JAR executável está disponível em: $JAR_FILE"
    exit 1
fi

echo "🔧 Ferramenta jpackage encontrada: $(which jpackage)"
echo ""

# Contadores
SUCCESS_COUNT=0
FAILED_COUNT=0

# Função simplificada para criar instalador
create_installer() {
    local type=$1
    local emoji=$2
    local name=$3
    shift 3
    local extra_args=("$@")
    
    echo "🏗️ Criando $emoji $name..."
    
    if jpackage \
        --input build/libs \
        --main-jar CommanderAPI-fat.jar \
        --main-class kandalabs.commander.application.ApplicationKt \
        --name CommanderAPI \
        --app-version 1.0 \
        --description "CommanderAPI Restaurant Order Management System" \
        --vendor "KandaLabs" \
        --dest "$OUTPUT_DIR" \
        --type "$type" \
        --java-options "-Xmx512m" \
        --java-options "-Dfile.encoding=UTF-8" \
        "${extra_args[@]}" \
        >/dev/null 2>/dev/null; then
        
        echo "   ✅ $emoji $name criado com SUCESSO!"
        ((SUCCESS_COUNT++))
        return 0
    else
        echo "   ❌ $emoji $name FALHOU"
        ((FAILED_COUNT++))
        return 1
    fi
}

echo "🎯 Tentando criar instaladores para todas as plataformas..."
echo ""

# 1. App Image (Portável) - funciona em qualquer sistema Unix
echo "1️⃣ Aplicação Portável (funciona em qualquer sistema)"
create_installer "app-image" "📱" "App Portável"
echo ""

# 2. macOS DMG - só funciona no macOS, mas vamos tentar sempre
echo "2️⃣ Instalador macOS"
if [[ "$OS" == "Darwin" ]]; then
    create_installer "dmg" "🍎" "macOS DMG" \
        "--mac-package-name" "CommanderAPI" \
        "--mac-package-identifier" "co.kandalabs.commander.api"
else
    echo "   ⚠️ 🍎 macOS DMG pulado (requer macOS)"
fi
echo ""

# 3. Linux DEB - pode funcionar no macOS se ferramentas estão disponíveis
echo "3️⃣ Instalador Linux (Ubuntu/Debian)"
create_installer "deb" "🐧" "Linux DEB" \
    "--linux-package-name" "commanderapi" \
    "--linux-app-category" "Development" \
    "--linux-shortcut"
echo ""

# 4. Linux RPM - pode funcionar no macOS se ferramentas estão disponíveis  
echo "4️⃣ Instalador Linux (RedHat/CentOS)"
create_installer "rpm" "🐧" "Linux RPM" \
    "--linux-package-name" "commanderapi" \
    "--linux-app-category" "Development" \
    "--linux-shortcut"
echo ""

# 5. Windows MSI - raramente funciona fora do Windows
echo "5️⃣ Instalador Windows (MSI)"
if [[ "$OS" =~ ^(MINGW|MSYS|CYGWIN) ]]; then
    create_installer "msi" "🪟" "Windows MSI" \
        "--win-console" \
        "--win-dir-chooser" \
        "--win-menu" \
        "--win-shortcut"
else
    echo "   ⚠️ 🪟 Windows MSI pulado (requer Windows)"
fi
echo ""

# 6. Windows EXE - raramente funciona fora do Windows
echo "6️⃣ Instalador Windows (EXE)"
if [[ "$OS" =~ ^(MINGW|MSYS|CYGWIN) ]]; then
    create_installer "exe" "🪟" "Windows EXE" \
        "--win-console" \
        "--win-dir-chooser" \
        "--win-menu" \
        "--win-shortcut"
else
    echo "   ⚠️ 🪟 Windows EXE pulado (requer Windows)"
fi
echo ""

# Resultado final
echo "================================================"
echo "📊 RESULTADO FINAL DA CONSTRUÇÃO"
echo "================================================"
echo "✅ Instaladores criados: $SUCCESS_COUNT"
echo "❌ Falhas/Pulos: $FAILED_COUNT"
echo ""

if [ $SUCCESS_COUNT -gt 0 ]; then
    echo "🎉 SUCESSO! Instaladores criados com sucesso!"
    echo ""
    echo "📂 ARQUIVOS DISPONÍVEIS EM: $OUTPUT_DIR"
    echo "==============================================="
    
    # Listar todos os arquivos criados com detalhes
    echo "📋 Lista completa de arquivos:"
    
    # JAR sempre existe
    echo "   ☕ $(ls -lh build/libs/CommanderAPI-fat.jar | awk '{print $9, "(" $5 ")"}')"
    
    # Listar instaladores criados
    find "$OUTPUT_DIR" -maxdepth 1 \( -name "*.dmg" -o -name "*.deb" -o -name "*.rpm" -o -name "*.msi" -o -name "*.exe" \) 2>/dev/null | while read file; do
        if [ -f "$file" ]; then
            size=$(ls -lh "$file" | awk '{print $5}')
            echo "   📦 $(basename "$file") ($size)"
        fi
    done
    
    # App portável (diretório)
    if [ -d "$OUTPUT_DIR/CommanderAPI" ]; then
        size=$(du -sh "$OUTPUT_DIR/CommanderAPI" | awk '{print $1}')
        echo "   📱 CommanderAPI/ (pasta portável - $size)"
    fi
    
    echo ""
    echo "🚀 INSTRUÇÕES DE DISTRIBUIÇÃO"
    echo "=============================="
    
    echo "1️⃣ MULTIPLATAFORMA (Java):"
    echo "   • Arquivo: build/libs/CommanderAPI-fat.jar"
    echo "   • Como usar: java -jar CommanderAPI-fat.jar"
    echo "   • Requisito: Java 17+"
    echo ""
    
    if ls "$OUTPUT_DIR"/*.dmg >/dev/null 2>&1; then
        echo "2️⃣ MACOS:"
        echo "   • Arquivo: $(basename "$OUTPUT_DIR"/*.dmg)"
        echo "   • Como instalar: Duplo clique → Arrastar para Applications"
        echo "   • Como executar: Procurar CommanderAPI no Launchpad"
        echo ""
    fi
    
    if ls "$OUTPUT_DIR"/*.deb >/dev/null 2>&1; then
        echo "3️⃣ LINUX (Ubuntu/Debian):"
        echo "   • Arquivo: $(basename "$OUTPUT_DIR"/*.deb)"
        echo "   • Como instalar: sudo dpkg -i $(basename "$OUTPUT_DIR"/*.deb)"
        echo "   • Como executar: commanderapi (ou buscar no menu)"
        echo ""
    fi
    
    if ls "$OUTPUT_DIR"/*.rpm >/dev/null 2>&1; then
        echo "4️⃣ LINUX (RedHat/CentOS):"
        echo "   • Arquivo: $(basename "$OUTPUT_DIR"/*.rpm)"  
        echo "   • Como instalar: sudo rpm -i $(basename "$OUTPUT_DIR"/*.rpm)"
        echo "   • Como executar: commanderapi (ou buscar no menu)"
        echo ""
    fi
    
    if ls "$OUTPUT_DIR"/*.msi >/dev/null 2>&1; then
        echo "5️⃣ WINDOWS (MSI):"
        echo "   • Arquivo: $(basename "$OUTPUT_DIR"/*.msi)"
        echo "   • Como instalar: Duplo clique → Seguir assistente"
        echo "   • Como executar: Menu Iniciar → CommanderAPI"
        echo ""
    fi
    
    if ls "$OUTPUT_DIR"/*.exe >/dev/null 2>&1; then
        echo "6️⃣ WINDOWS (EXE):"
        echo "   • Arquivo: $(basename "$OUTPUT_DIR"/*.exe)"
        echo "   • Como instalar: Executar → Seguir assistente"
        echo "   • Como executar: Menu Iniciar → CommanderAPI"
        echo ""
    fi
    
    if [ -d "$OUTPUT_DIR/CommanderAPI" ]; then
        echo "7️⃣ PORTÁVEL (Qualquer sistema):"
        echo "   • Arquivo: Pasta CommanderAPI/ completa"
        echo "   • Como executar: ./CommanderAPI/bin/CommanderAPI"
        echo "   • Vantagem: Não precisa instalação"
        echo ""
    fi
    
    echo "🌐 APÓS EXECUTAR (qualquer método):"
    echo "   • API principal: http://localhost:8081"
    echo "   • Documentação: http://localhost:8081/swagger-ui"
    echo "   • Status de saúde: http://localhost:8081/health"
    echo "   • Ícone aparece na bandeja do sistema"
    echo ""
    
    echo "🎯 DISTRIBUIÇÃO RECOMENDADA:"
    echo "   • Usuários técnicos: JAR universal"
    echo "   • Usuários finais: Instalador nativo da plataforma"
    echo "   • Sem Java instalado: Use instalador nativo (inclui Java)"
    echo ""
    
else
    echo "💥 NENHUM instalador foi criado!"
    echo ""
    echo "😞 Mas não se preocupe! Você ainda tem:"
    echo "   ✅ JAR executável: build/libs/CommanderAPI-fat.jar"
    echo "   📝 Como usar: java -jar build/libs/CommanderAPI-fat.jar"
    echo ""
    echo "🔧 Para melhorar na próxima vez:"
    echo "   • Verifique se tem JDK 17+ (não apenas JRE)"
    echo "   • Em Linux: instale rpm-build e dpkg-dev"
    echo "   • Em Windows: use este script no Windows para .msi/.exe"
fi

echo ""
echo "🔄 Para gerar novamente: ./create-multi-installers.sh"
echo "📚 Documentação completa: EXECUTABLE_DISTRIBUTION.md"

# Retorna sucesso se pelo menos o JAR existe (que sempre deve existir)
exit 0