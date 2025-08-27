#!/bin/bash

echo "🔧 Iniciando CommanderAPI em modo DEBUG (porta 8082)"
echo "📦 Database: data-debug.db (DESENVOLVIMENTO - não afeta produção)"
echo "🌐 Host: localhost (192.168.2.200)"
echo ""

cd CommanderAPI

export ENVIRONMENT=debug
export PORT=8082
export HOST="192.168.2.200"
export DATABASE_URL="jdbc:sqlite:data-debug.db"

./gradlew run --args="--port=8082 --host=192.168.2.200"