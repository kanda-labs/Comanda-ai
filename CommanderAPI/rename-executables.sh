#!/bin/bash

# Script para padronizar nomes dos executáveis CommanderAPI
# Renomeia todos os arquivos para seguir o padrão: CommanderAPI-v1.0-[plataforma].[extensão]

set -e

VERSION="v1.0"
OUTPUT_DIR="build/installer"

echo "🏷️ PADRONIZAÇÃO DE NOMES - CommanderAPI"
echo "======================================="
echo "Versão: $VERSION"
echo "Diretório: $OUTPUT_DIR"
echo ""

# Verificar se diretório existe
if [ ! -d "$OUTPUT_DIR" ]; then
    echo "❌ Diretório $OUTPUT_DIR não encontrado!"
    exit 1
fi

cd "$OUTPUT_DIR"

echo "📋 ARQUIVOS ANTES DA RENOMEAÇÃO:"
ls -1 *.dmg *.pkg *.app *.sh *.bat *.jar 2>/dev/null || echo "   (nenhum arquivo encontrado)"
echo ""

RENAMED_COUNT=0

# Função para renomear arquivo se existir
rename_if_exists() {
    local old_name="$1"
    local new_name="$2"
    local description="$3"
    
    if [ -e "$old_name" ]; then
        if [ "$old_name" != "$new_name" ]; then
            mv "$old_name" "$new_name"
            echo "✅ $description: $new_name"
            ((RENAMED_COUNT++))
        else
            echo "ℹ️ $description: já tem nome correto"
        fi
    fi
}

# Função para copiar e renomear se existir
copy_rename_if_exists() {
    local source="$1"
    local new_name="$2"
    local description="$3"
    
    if [ -e "$source" ]; then
        cp "$source" "$new_name"
        echo "📋 $description: $new_name (copiado)"
        ((RENAMED_COUNT++))
    fi
}

echo "🔄 EXECUTANDO RENOMEAÇÕES:"

# 1. DMG do macOS
rename_if_exists "CommanderAPI-1.0.dmg" "CommanderAPI-$VERSION-macOS.dmg" "macOS DMG"

# 2. PKG do macOS
rename_if_exists "CommanderAPI-1.0.pkg" "CommanderAPI-$VERSION-macOS.pkg" "macOS PKG"

# 3. App Bundle do macOS
rename_if_exists "CommanderAPI.app" "CommanderAPI-$VERSION-macOS.app" "macOS App Bundle"

# 4. Script instalador Linux
rename_if_exists "install-linux.sh" "CommanderAPI-$VERSION-linux-installer.sh" "Linux Installer"

# 5. Script instalador Windows
rename_if_exists "install-windows.bat" "CommanderAPI-$VERSION-windows-installer.bat" "Windows Installer"

# 6. JAR Universal (copiar do libs se não existir aqui)
if [ ! -e "CommanderAPI-$VERSION-universal.jar" ]; then
    copy_rename_if_exists "../libs/CommanderAPI-fat.jar" "CommanderAPI-$VERSION-universal.jar" "Universal JAR"
else
    echo "ℹ️ Universal JAR: já existe"
fi

echo ""

# Verificar se há arquivos com nomes antigos
echo "🔍 VERIFICANDO ARQUIVOS COM NOMES ANTIGOS:"
old_files_found=0

for old_pattern in "CommanderAPI-1.*.dmg" "CommanderAPI-1.*.pkg" "CommanderAPI.app" "install-*.sh" "install-*.bat" "CommanderAPI-fat.jar"; do
    if ls $old_pattern >/dev/null 2>&1; then
        echo "⚠️ Arquivo com nome antigo encontrado: $(ls $old_pattern)"
        ((old_files_found++))
    fi
done

if [ $old_files_found -eq 0 ]; then
    echo "✅ Nenhum arquivo com nome antigo encontrado"
fi

echo ""
echo "📋 ARQUIVOS APÓS RENOMEAÇÃO:"
ls -1 CommanderAPI-$VERSION-* 2>/dev/null | while read file; do
    size=$(ls -lah "$file" | awk '{print $5}')
    echo "   📦 $file ($size)"
done

echo ""
echo "📊 RESULTADO:"
echo "============="
echo "✅ Arquivos renomeados/copiados: $RENAMED_COUNT"
echo "📁 Localização: $(pwd)"

if [ $RENAMED_COUNT -gt 0 ]; then
    echo ""
    echo "🎯 PADRÃO FINAL DOS NOMES:"
    echo "   CommanderAPI-$VERSION-macOS.dmg        (Instalador macOS DMG)"
    echo "   CommanderAPI-$VERSION-macOS.pkg        (Instalador macOS PKG)"  
    echo "   CommanderAPI-$VERSION-macOS.app        (App Bundle macOS)"
    echo "   CommanderAPI-$VERSION-linux-installer.sh   (Instalador Linux)"
    echo "   CommanderAPI-$VERSION-windows-installer.bat (Instalador Windows)"
    echo "   CommanderAPI-$VERSION-universal.jar    (JAR Universal)"
    echo ""
    echo "🎉 Padronização concluída com sucesso!"
else
    echo ""
    echo "ℹ️ Todos os arquivos já estavam com nomes padronizados"
fi

echo ""
echo "🔄 Para executar novamente: ./rename-executables.sh"