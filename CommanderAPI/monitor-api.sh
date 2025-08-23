#!/bin/bash

# CommanderAPI Health Monitor
# Monitora continuamente a API e reinicia se necessário

API_URL="http://localhost:8081/health"
API_PORT=8081
CHECK_INTERVAL=30  # segundos entre verificações
MAX_RETRIES=3
LOG_FILE="monitor.log"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

check_api() {
    curl -s --connect-timeout 5 --max-time 10 "$API_URL" > /dev/null 2>&1
    return $?
}

kill_gradle_processes() {
    log "${YELLOW}Matando processos Gradle existentes...${NC}"
    
    # Mata processos gradle
    pkill -f "gradle.*run" 2>/dev/null
    pkill -f "GradleDaemon" 2>/dev/null
    pkill -f "kotlin.*Application" 2>/dev/null
    
    # Mata processos usando a porta 8081
    local pids=$(lsof -t -i:$API_PORT 2>/dev/null)
    if [ ! -z "$pids" ]; then
        echo "$pids" | xargs kill -9 2>/dev/null
        log "Processos na porta $API_PORT terminados: $pids"
    fi
    
    sleep 3
}

start_api() {
    log "${BLUE}Iniciando CommanderAPI...${NC}"
    
    # Navega para o diretório do projeto
    cd "$(dirname "$0")"
    
    # Inicia a API em background
    nohup ./gradlew run > api-output.log 2>&1 &
    local gradle_pid=$!
    
    log "Gradle iniciado com PID: $gradle_pid"
    
    # Aguarda a API ficar disponível (até 60 segundos)
    local wait_count=0
    local max_wait=60
    
    while [ $wait_count -lt $max_wait ]; do
        sleep 1
        if check_api; then
            log "${GREEN}✅ API iniciada com sucesso!${NC}"
            return 0
        fi
        wait_count=$((wait_count + 1))
        echo -n "."
    done
    
    log "${RED}❌ Falha ao iniciar API após ${max_wait}s${NC}"
    return 1
}

monitor_loop() {
    log "${GREEN}🚀 Iniciando monitoramento contínuo da CommanderAPI${NC}"
    log "URL: $API_URL"
    log "Intervalo de verificação: ${CHECK_INTERVAL}s"
    log "Pressione Ctrl+C para parar o monitoramento"
    echo ""
    
    local consecutive_failures=0
    
    while true; do
        if check_api; then
            if [ $consecutive_failures -gt 0 ]; then
                log "${GREEN}✅ API voltou online após $consecutive_failures falhas${NC}"
            fi
            consecutive_failures=0
            echo -ne "${GREEN}●${NC} $(date '+%H:%M:%S') - API Online    \r"
        else
            consecutive_failures=$((consecutive_failures + 1))
            echo -ne "${RED}●${NC} $(date '+%H:%M:%S') - API Offline (${consecutive_failures}/${MAX_RETRIES})    \r"
            
            if [ $consecutive_failures -ge $MAX_RETRIES ]; then
                echo ""
                log "${RED}❌ API offline há $consecutive_failures tentativas. Reiniciando...${NC}"
                
                kill_gradle_processes
                
                if start_api; then
                    consecutive_failures=0
                    log "${GREEN}✅ Reinicialização concluída${NC}"
                else
                    log "${RED}❌ Falha na reinicialização. Tentando novamente em ${CHECK_INTERVAL}s...${NC}"
                fi
            fi
        fi
        
        sleep $CHECK_INTERVAL
    done
}

# Tratamento de sinal para limpeza
cleanup() {
    echo ""
    log "${YELLOW}🛑 Monitor interrompido pelo usuário${NC}"
    exit 0
}

trap cleanup SIGINT SIGTERM

# Verificação inicial
log "${BLUE}Verificando status inicial da API...${NC}"
if ! check_api; then
    log "${YELLOW}API não está rodando. Iniciando...${NC}"
    kill_gradle_processes
    start_api
fi

# Inicia o loop de monitoramento
monitor_loop