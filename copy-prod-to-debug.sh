#!/bin/bash

echo "📋 Copiando banco de PRODUÇÃO para DEBUG..."
echo "🔄 Isso permite testar com dados reais sem afetar produção"
echo ""

# Definir caminhos dos bancos
PROD_DB="CommanderAPI/data.db"
DEBUG_DB="CommanderAPI/data-debug.db"

# Verificar se o banco de produção existe
if [ ! -f "$PROD_DB" ]; then
    echo "❌ ERRO: Banco de produção não encontrado em: $PROD_DB"
    echo "   Certifique-se que o servidor de produção já foi executado pelo menos uma vez"
    exit 1
fi

# Criar backup do banco debug atual (se existir)
if [ -f "$DEBUG_DB" ]; then
    BACKUP_NAME="CommanderAPI/data-debug-backup-$(date +%Y%m%d_%H%M%S).db"
    echo "💾 Fazendo backup do banco debug atual: $BACKUP_NAME"
    cp "$DEBUG_DB" "$BACKUP_NAME"
fi

# Copiar banco de produção para debug
echo "🔄 Copiando $PROD_DB → $DEBUG_DB"
cp "$PROD_DB" "$DEBUG_DB"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ SUCESSO! Banco de debug atualizado com dados de produção"
    echo ""
    echo "📊 Informações dos bancos:"
    echo "   📦 Produção: $(ls -lh "$PROD_DB" | awk '{print $5}') - $PROD_DB"
    echo "   🔧 Debug:    $(ls -lh "$DEBUG_DB" | awk '{print $5}') - $DEBUG_DB"
    echo ""
    echo "🚀 Agora você pode iniciar o servidor debug com:"
    echo "   ./start-debug.sh"
    echo ""
    echo "⚠️  LEMBRE-SE: O banco debug é independente - mudanças não afetam produção!"
else
    echo "❌ ERRO: Falha ao copiar banco de dados"
    exit 1
fi