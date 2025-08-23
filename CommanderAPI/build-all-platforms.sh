#!/bin/bash

# Script robusto para criar instaladores multiplataforma da CommanderAPI
# Tenta criar todos os formatos possíveis baseado no sistema atual

set -e

echo "🚀 BUILD MULTIPLATAFORMA - CommanderAPI"
echo "======================================="

# Build do JAR fat primeiro
echo "📦 Construindo JAR fat..."
./gradlew clean fatJar

JAR_FILE="build/libs/CommanderAPI-fat.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Erro: JAR não foi criado"
    exit 1
fi

echo "✅ JAR criado: $(ls -lh $JAR_FILE | awk '{print $5}')"

# Preparar diretórios
OUTPUT_DIR="build/installer"
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

# Informações do sistema
OS=$(uname -s)
echo "💻 Sistema: $OS"
echo "☕ Java: $(java -version 2>&1 | head -n 1)"
echo ""

# Contadores
SUCCESS_COUNT=0
TOTAL_ATTEMPTS=0

# Função para tentar criar instalador
try_installer() {
    local type=$1
    local emoji=$2
    local description=$3
    shift 3
    local extra_args=("$@")
    
    echo "🏗️ Tentando criar: $emoji $description ($type)"
    ((TOTAL_ATTEMPTS++))
    
    # Executar jpackage com timeout de 5 minutos
    if timeout 300 jpackage \
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
        >/tmp/jpackage-$type.log 2>&1; then
        
        echo "  ✅ $emoji $description - SUCESSO!"
        ((SUCCESS_COUNT++))
        return 0
    else
        local error_code=$?
        echo "  ❌ $emoji $description - FALHOU (código: $error_code)"
        
        # Mostrar primeiras linhas do erro se não for timeout
        if [ $error_code -ne 124 ]; then
            echo "     Erro:"
            head -n 3 "/tmp/jpackage-$type.log" 2>/dev/null | sed 's/^/       /' || echo "       (log não disponível)"
        else
            echo "     Timeout após 5 minutos"
        fi
        return 1
    fi
}

echo "🎯 Tentando criar todos os instaladores possíveis..."
echo ""

# 1. App Image (Portável) - funciona em todos os sistemas
try_installer "app-image" "📱" "App Portável"

# 2. macOS DMG - só funciona no macOS
if [[ "$OS" == "Darwin" ]]; then
    try_installer "dmg" "🍎" "macOS DMG" \
        "--mac-package-name" "CommanderAPI" \
        "--mac-package-identifier" "co.kandalabs.commander.api"
fi

# 3. Linux DEB - funciona em macOS e Linux com ferramentas adequadas
try_installer "deb" "🐧" "Linux DEB" \
    "--linux-package-name" "commanderapi" \
    "--linux-app-category" "Development" \
    "--linux-shortcut" \
    "--linux-menu-group" "Development"

# 4. Linux RPM - funciona em macOS e Linux com ferramentas adequadas
try_installer "rpm" "🐧" "Linux RPM" \
    "--linux-package-name" "commanderapi" \
    "--linux-app-category" "Development" \
    "--linux-shortcut" \
    "--linux-menu-group" "Development"

# 5. Windows MSI - geralmente não funciona fora do Windows
if [[ "$OS" == MINGW* ]] || [[ "$OS" == MSYS* ]] || [[ "$OS" == CYGWIN* ]]; then
    try_installer "msi" "🪟" "Windows MSI" \
        "--win-console" \
        "--win-dir-chooser" \
        "--win-menu" \
        "--win-shortcut"
else
    echo "🏗️ Tentando criar: 🪟 Windows MSI (msi)"
    echo "  ⚠️ Pulando - requer Windows"
    ((TOTAL_ATTEMPTS++))
fi

# 6. Windows EXE - geralmente não funciona fora do Windows  
if [[ "$OS" == MINGW* ]] || [[ "$OS" == MSYS* ]] || [[ "$OS" == CYGWIN* ]]; then
    try_installer "exe" "🪟" "Windows EXE" \
        "--win-console" \
        "--win-dir-chooser" \
        "--win-menu" \
        "--win-shortcut"
else
    echo "🏗️ Tentando criar: 🪟 Windows EXE (exe)"
    echo "  ⚠️ Pulando - requer Windows"
    ((TOTAL_ATTEMPTS++))
fi

echo ""
echo "==========================================="
echo "📊 RESULTADO FINAL"
echo "==========================================="
echo "✅ Sucessos: $SUCCESS_COUNT de $TOTAL_ATTEMPTS tentativas"
echo ""

if [ $SUCCESS_COUNT -gt 0 ]; then
    echo "🔍 ARQUIVOS CRIADOS:"
    find "$OUTPUT_DIR" -type f \( -name "*.dmg" -o -name "*.deb" -o -name "*.rpm" -o -name "*.msi" -o -name "*.exe" \) -exec ls -lah {} \; 2>/dev/null | while read line; do
        echo "   $line"
    done
    
    # Verificar se app-image foi criado (é um diretório)
    if [ -d "$OUTPUT_DIR/CommanderAPI" ]; then
        echo "   $(ls -lah "$OUTPUT_DIR" | grep "CommanderAPI")"
    fi
    
    echo ""
    echo "📦 COMO DISTRIBUIR:"
    echo "-------------------"
    
    # Instruções baseadas no que foi realmente criado
    if ls "$OUTPUT_DIR"/*.dmg >/dev/null 2>&1; then
        echo "🍎 macOS: Distribuir $(basename "$OUTPUT_DIR"/*.dmg)"
        echo "   Instalação: Duplo clique → Arrastar para Applications"
    fi
    
    if ls "$OUTPUT_DIR"/*.deb >/dev/null 2>&1; then
        echo "🐧 Ubuntu/Debian: Distribuir $(basename "$OUTPUT_DIR"/*.deb)"
        echo "   Instalação: sudo dpkg -i [arquivo.deb]"
    fi
    
    if ls "$OUTPUT_DIR"/*.rpm >/dev/null 2>&1; then
        echo "🐧 RedHat/CentOS: Distribuir $(basename "$OUTPUT_DIR"/*.rpm)"
        echo "   Instalação: sudo rpm -i [arquivo.rpm]"
    fi
    
    if ls "$OUTPUT_DIR"/*.msi >/dev/null 2>&1; then
        echo "🪟 Windows: Distribuir $(basename "$OUTPUT_DIR"/*.msi)"
        echo "   Instalação: Duplo clique → Seguir assistente"
    fi
    
    if ls "$OUTPUT_DIR"/*.exe >/dev/null 2>&1; then
        echo "🪟 Windows: Distribuir $(basename "$OUTPUT_DIR"/*.exe)"
        echo "   Instalação: Executar → Seguir assistente"
    fi
    
    if [ -d "$OUTPUT_DIR/CommanderAPI" ]; then
        echo "📱 Portable: Distribuir pasta CommanderAPI/ completa"
        echo "   Execução: ./CommanderAPI/bin/CommanderAPI"
    fi
    
    echo ""
    echo "☕ SEMPRE DISPONÍVEL:"
    echo "   JAR Universal: build/libs/CommanderAPI-fat.jar"
    echo "   Execução: java -jar CommanderAPI-fat.jar"
    echo ""
    
    echo "🌐 APÓS INSTALAÇÃO:"
    echo "   • API: http://localhost:8081"
    echo "   • Swagger UI: http://localhost:8081/swagger-ui"  
    echo "   • Health Check: http://localhost:8081/health"
    echo ""
    
    echo "🎉 Distribuição multiplataforma CONCLUÍDA!"
    
else
    echo "💥 Nenhum instalador foi criado com sucesso!"
    echo ""
    echo "🔧 Para investigar os erros:"
    ls -1 /tmp/jpackage-*.log 2>/dev/null | while read log; do
        echo "   cat $log"
    done
    echo ""
    echo "⚠️ Possíveis causas:"
    echo "   • jpackage requer JDK 14+ (você tem: $(java -version 2>&1 | head -n 1))"
    echo "   • Algumas ferramentas de empacotamento podem estar ausentes"
    echo "   • Instaladores Windows requerem sistema Windows"
fi

echo ""
echo "🔄 Para tentar novamente: ./build-all-platforms.sh"

# Retornar código de sucesso se pelo menos um instalador foi criado
exit $([[ $SUCCESS_COUNT -gt 0 ]] && echo 0 || echo 1)