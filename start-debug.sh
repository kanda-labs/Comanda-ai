#!/bin/bash

# Ler configurações do local.properties
source "$(dirname "$0")/read-config.sh"
read_local_properties

echo "🔧 Iniciando CommanderAPI em modo DEBUG (porta $DEBUG_PORT)"
echo "📦 Database: data-debug.db (DESENVOLVIMENTO - não afeta produção)"
echo "🌐 API Host: $API_HOST"
echo "📱 Client conecta em: $BASE_IP"
echo ""

cd CommanderAPI

export ENVIRONMENT=debug
export PORT=$DEBUG_PORT
export HOST="$API_HOST"
export DATABASE_URL="jdbc:sqlite:data-debug.db"

./gradlew run --args="--port=$DEBUG_PORT --host=$API_HOST"