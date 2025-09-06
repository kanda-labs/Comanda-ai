#!/bin/bash

# Ler configurações do local.properties
source "$(dirname "$0")/read-config.sh"
read_local_properties

echo "🚀 Iniciando CommanderAPI em modo PRODUÇÃO (porta $PRODUCTION_PORT)"
echo "📦 Database: data.db (PRODUÇÃO)"
echo "🌐 API Host: $API_HOST"
echo "📱 Client conecta em: $BASE_IP"
echo ""

cd CommanderAPI

export ENVIRONMENT=production
export PORT=$PRODUCTION_PORT
export HOST="$API_HOST"
export DATABASE_URL="jdbc:sqlite:data.db"

./gradlew run --args="--port=$PRODUCTION_PORT --host=$API_HOST"