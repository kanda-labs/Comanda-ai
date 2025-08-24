#!/bin/bash

echo "📊 GERENCIADOR DE BANCOS DE DADOS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Função para mostrar informações do banco
show_database_info() {
    local db_path=$1
    local db_name=$2
    
    if [ -f "$db_path" ]; then
        local size=$(ls -lh "$db_path" | awk '{print $5}')
        local date=$(ls -l "$db_path" | awk '{print $6" "$7" "$8}')
        echo "✅ $db_name"
        echo "   📁 Arquivo: $db_path"
        echo "   📏 Tamanho: $size"
        echo "   📅 Modificado: $date"
    else
        echo "❌ $db_name"
        echo "   📁 Arquivo: $db_path (NÃO EXISTE)"
    fi
    echo ""
}

# Mostrar status dos bancos
echo "📦 STATUS DOS BANCOS:"
echo ""
show_database_info "CommanderAPI/data.db" "PRODUÇÃO"
show_database_info "CommanderAPI/data-debug.db" "DEBUG"

# Listar backups
echo "💾 BACKUPS DISPONÍVEIS:"
BACKUP_COUNT=$(ls CommanderAPI/data-debug-backup-*.db 2>/dev/null | wc -l)
if [ $BACKUP_COUNT -gt 0 ]; then
    ls -lht CommanderAPI/data-debug-backup-*.db | head -5 | while read line; do
        file=$(echo $line | awk '{print $9}')
        size=$(echo $line | awk '{print $5}')
        date=$(echo $line | awk '{print $6" "$7" "$8}')
        echo "   📄 $(basename $file) - $size - $date"
    done
    if [ $BACKUP_COUNT -gt 5 ]; then
        echo "   ... e mais $((BACKUP_COUNT - 5)) backup(s)"
    fi
else
    echo "   (Nenhum backup encontrado)"
fi
echo ""

# Menu de opções
echo "🛠️  AÇÕES DISPONÍVEIS:"
echo "1. Copiar PRODUÇÃO → DEBUG"
echo "2. Restaurar backup para DEBUG" 
echo "3. Limpar backups antigos (manter últimos 5)"
echo "4. Mostrar estatísticas detalhadas"
echo "5. Sair"
echo ""

read -p "Escolha uma opção (1-5): " choice

case $choice in
    1)
        echo ""
        echo "🔄 Copiando banco de PRODUÇÃO para DEBUG..."
        ./copy-prod-to-debug.sh
        ;;
    2)
        echo ""
        echo "💾 Backups disponíveis:"
        ls CommanderAPI/data-debug-backup-*.db 2>/dev/null | nl
        echo ""
        read -p "Digite o número do backup para restaurar (ou 0 para cancelar): " backup_num
        if [ "$backup_num" != "0" ]; then
            backup_file=$(ls CommanderAPI/data-debug-backup-*.db 2>/dev/null | sed -n "${backup_num}p")
            if [ -n "$backup_file" ]; then
                cp "$backup_file" "CommanderAPI/data-debug.db"
                echo "✅ Backup restaurado: $backup_file → data-debug.db"
            else
                echo "❌ Backup inválido"
            fi
        fi
        ;;
    3)
        echo ""
        echo "🧹 Limpando backups antigos (mantendo últimos 5)..."
        ls -t CommanderAPI/data-debug-backup-*.db 2>/dev/null | tail -n +6 | xargs -r rm
        echo "✅ Limpeza concluída"
        ;;
    4)
        echo ""
        echo "📈 ESTATÍSTICAS DETALHADAS:"
        echo ""
        for db in "CommanderAPI/data.db" "CommanderAPI/data-debug.db"; do
            if [ -f "$db" ]; then
                echo "🔍 $(basename $db):"
                sqlite3 "$db" "SELECT name FROM sqlite_master WHERE type='table';" | while read table; do
                    count=$(sqlite3 "$db" "SELECT COUNT(*) FROM $table;" 2>/dev/null || echo "0")
                    echo "   📋 $table: $count registros"
                done
                echo ""
            fi
        done
        ;;
    5)
        echo "👋 Até logo!"
        exit 0
        ;;
    *)
        echo "❌ Opção inválida"
        ;;
esac