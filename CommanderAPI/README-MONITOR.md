# CommanderAPI Health Monitor

Scripts de monitoramento contínuo que verificam se a API está online e reiniciam automaticamente quando necessário.

## 🚀 Como Usar

### Linux/Mac
```bash
./monitor-api.sh
```

### Windows (Command Prompt)
```cmd
monitor-api.bat
```

### Windows (PowerShell)
```powershell
.\monitor-api.ps1
```

## ⚙️ Configuração

### Parâmetros Padrão
- **URL da API:** `http://localhost:8081/health`
- **Porta:** `8081`
- **Intervalo de verificação:** `30 segundos`
- **Máximo de falhas:** `3 tentativas`
- **Log:** `monitor.log`

### Personalizar Configuração (PowerShell)
```powershell
.\monitor-api.ps1 -ApiUrl "http://localhost:9000/health" -CheckInterval 60 -MaxRetries 5
```

## 🔧 Funcionalidades

### ✅ Monitoramento Contínuo
- Verifica a API a cada 30 segundos
- Exibe status em tempo real no terminal
- Registra eventos no arquivo de log

### 🔄 Restart Automático
- Detecta quando a API está offline
- Mata processos Gradle/Java existentes
- Libera a porta 8081
- Reinicia a API automaticamente
- Aguarda confirmação que a API voltou online

### 📊 Visual Status
- **Verde (●):** API Online
- **Vermelho (●):** API Offline
- **Logs detalhados:** Salvos em `monitor.log`

### 🛑 Limpeza de Processos
- Mata processos `gradle run`
- Mata processos Java/Kotlin
- Libera portas ocupadas
- Aguarda estabilização antes de reiniciar

## 📋 Exemplo de Output

```
================================================
    CommanderAPI Health Monitor
================================================
URL: http://localhost:8081/health
Intervalo: 30s
Pressione Ctrl+C para parar o monitoramento
================================================

● 14:30:15 - API Online
● 14:30:45 - API Online
● 14:31:15 - API Offline (1/3)
● 14:31:45 - API Offline (2/3)
● 14:32:15 - API Offline (3/3)

[14:32:15] REINICIANDO API...
[14:32:18] Matando processos existentes...
[14:32:21] Iniciando CommanderAPI...
[14:32:35] ✅ API iniciada com sucesso!

● 14:32:45 - API Online
```

## 🔍 Logs

Todos os eventos são registrados em `monitor.log`:

```
2025-08-23 14:30:10 - 🚀 Iniciando monitoramento contínuo da CommanderAPI
2025-08-23 14:32:15 - ❌ API offline há 3 tentativas. Reiniciando...
2025-08-23 14:32:18 - Matando processos Gradle existentes...
2025-08-23 14:32:21 - Iniciando CommanderAPI...
2025-08-23 14:32:35 - ✅ API iniciada com sucesso!
2025-08-23 14:32:35 - ✅ Reinicialização concluída
```

## ⚠️ Requisitos

### Sistema
- **Linux/Mac:** curl, bash, lsof
- **Windows:** curl (ou PowerShell), netstat

### Java/Gradle
- JDK 22+
- Gradle configurado no projeto
- Endpoint `/health` funcionando na API

## 🚪 Parar o Monitor

- **Ctrl+C** para interromper graciosamente
- O monitor faz limpeza automática ao sair
- Logs ficam preservados

## 🔧 Troubleshooting

### "curl: command not found"
**Windows:** Instale curl ou use a versão PowerShell

### "Permission denied"
**Linux/Mac:** Execute `chmod +x monitor-api.sh`

### API não inicia
- Verifique se a porta 8081 está livre
- Confirme se o Gradle está funcionando: `./gradlew build`
- Verifique logs em `api-output.log` e `monitor.log`

### Processo não mata
- Use `lsof -i:8081` (Linux/Mac) ou `netstat -ano | findstr :8081` (Windows)
- Mate manualmente: `kill -9 <PID>` ou `taskkill /f /pid <PID>`