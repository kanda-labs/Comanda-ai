#!/bin/bash

echo "🔧 Iniciando CommanderAPI em modo DEBUG (porta 8082)"
echo "📦 Database: data-debug.db (DESENVOLVIMENTO - não afeta produção)"
echo "🌐 Host: localhost (192.168.1.4)"
echo ""

# Perguntar se quer copiar dados de produção
echo "❓ Deseja copiar dados de PRODUÇÃO para DEBUG? (y/n)"
echo "   Isso substituirá o banco debug atual pelos dados reais de produção"
read -p "   Resposta: " -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🔄 Copiando banco de produção..."
    
    # Definir caminhos dos bancos
    PROD_DB="CommanderAPI/data.db"
    DEBUG_DB="CommanderAPI/data-debug.db"
    
    # Verificar se o banco de produção existe
    if [ ! -f "$PROD_DB" ]; then
        echo "❌ ERRO: Banco de produção não encontrado em: $PROD_DB"
        echo "   Execute o servidor de produção primeiro: ./start-production.sh"
        echo ""
        echo "❓ Deseja continuar com o banco debug atual? (y/n)"
        read -p "   Resposta: " -r
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo "❌ Cancelando inicialização"
            exit 1
        fi
    else
        # Fazer backup se o banco debug existir
        if [ -f "$DEBUG_DB" ]; then
            BACKUP_NAME="CommanderAPI/data-debug-backup-$(date +%Y%m%d_%H%M%S).db"
            echo "💾 Backup do banco debug atual: $BACKUP_NAME"
            cp "$DEBUG_DB" "$BACKUP_NAME"
        fi
        
        # Copiar banco
        cp "$PROD_DB" "$DEBUG_DB"
        echo "✅ Banco debug atualizado com dados de produção!"
        echo ""
    fi
else
    echo "ℹ️  Usando banco debug existente (sem cópia de produção)"
    echo ""
fi

# Iniciar servidor
echo "🚀 Iniciando servidor debug..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

cd CommanderAPI

export ENVIRONMENT=debug
export PORT=8082
export HOST="192.168.1.4"
export DATABASE_URL="jdbc:sqlite:data-debug.db"

./gradlew run --args="--port=8082 --host=192.168.1.4"