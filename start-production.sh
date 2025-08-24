#!/bin/bash

echo "🚀 Iniciando CommanderAPI em modo PRODUÇÃO (porta 8081)"
echo "📦 Database: data.db (PRODUÇÃO)"
echo "🌐 Host: localhost (127.0.0.1)"
echo ""

cd CommanderAPI

export ENVIRONMENT=production
export PORT=8081
export HOST="127.0.0.1"
export DATABASE_URL="jdbc:sqlite:data.db"

./gradlew run --args="--port=8081 --host=127.0.0.1"