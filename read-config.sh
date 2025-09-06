#!/bin/bash

# Função para ler configurações do local.properties
read_local_properties() {
    local properties_file="local.properties"
    
    if [ ! -f "$properties_file" ]; then
        echo "❌ ERRO: $properties_file não encontrado!"
        echo "   Crie o arquivo com as configurações de rede:"
        echo "   base.ip=SEU_IP"
        echo "   production.port=8081" 
        echo "   debug.port=8082"
        exit 1
    fi
    
    # Ler valores do properties
    BASE_IP=$(grep "^base.ip=" "$properties_file" | cut -d'=' -f2 | tr -d ' ')
    PRODUCTION_PORT=$(grep "^production.port=" "$properties_file" | cut -d'=' -f2 | tr -d ' ')
    DEBUG_PORT=$(grep "^debug.port=" "$properties_file" | cut -d'=' -f2 | tr -d ' ')
    
    # Valores padrão se não encontrados
    BASE_IP=${BASE_IP:-"192.168.1.5"}
    PRODUCTION_PORT=${PRODUCTION_PORT:-"8081"}
    DEBUG_PORT=${DEBUG_PORT:-"8082"}
    
    # Se o IP for 10.0.2.2 (emulador), a API deve usar 0.0.0.0 para aceitar conexões
    if [ "$BASE_IP" = "10.0.2.2" ]; then
        API_HOST="0.0.0.0"
    else
        API_HOST="$BASE_IP"
    fi
    
    # Exportar variáveis para serem usadas pelos scripts
    export BASE_IP
    export API_HOST
    export PRODUCTION_PORT  
    export DEBUG_PORT
    
    # Debug opcional
    if [ "$SHOW_CONFIG" = "true" ]; then
        echo "📋 Configuração lida do $properties_file:"
        echo "   Client IP: $BASE_IP"
        echo "   API Host: $API_HOST"  
        echo "   Production Port: $PRODUCTION_PORT"
        echo "   Debug Port: $DEBUG_PORT"
        echo ""
    fi
}

# Se o script for executado diretamente, mostrar config
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    SHOW_CONFIG=true
    read_local_properties
fi