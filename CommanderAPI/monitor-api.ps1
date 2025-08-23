# CommanderAPI Health Monitor for Windows PowerShell
# Monitora continuamente a API e reinicia se necessário

param(
    [string]$ApiUrl = "http://localhost:8081/health",
    [int]$ApiPort = 8081,
    [int]$CheckInterval = 30,
    [int]$MaxRetries = 3,
    [string]$LogFile = "monitor.log"
)

function Write-Log {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logEntry = "$timestamp - $Message"
    Write-Host $logEntry
    Add-Content -Path $LogFile -Value $logEntry
}

function Test-ApiHealth {
    try {
        $response = Invoke-WebRequest -Uri $ApiUrl -Method Get -TimeoutSec 10 -UseBasicParsing
        return $response.StatusCode -eq 200
    }
    catch {
        return $false
    }
}

function Stop-ApiProcesses {
    Write-Log "Matando processos existentes..."
    
    # Mata processos Java/Gradle
    Get-Process | Where-Object { $_.ProcessName -match "java|javaw" } | Stop-Process -Force -ErrorAction SilentlyContinue
    
    # Mata processos usando a porta 8081
    $netstat = netstat -ano | Select-String ":$ApiPort"
    if ($netstat) {
        $pids = $netstat | ForEach-Object { 
            $_.ToString().Split()[-1] 
        } | Where-Object { $_ -match '^\d+$' } | Sort-Object -Unique
        
        foreach ($pid in $pids) {
            try {
                Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
                Write-Log "Processo PID $pid terminado"
            }
            catch {
                # Ignora erros
            }
        }
    }
    
    Start-Sleep -Seconds 3
}

function Start-Api {
    Write-Log "Iniciando CommanderAPI..."
    
    # Inicia a API em background
    $process = Start-Process -FilePath "gradlew.bat" -ArgumentList "run" -WindowStyle Hidden -PassThru -RedirectStandardOutput "api-output.log" -RedirectStandardError "api-error.log"
    Write-Log "Gradle iniciado com PID: $($process.Id)"
    
    # Aguarda a API ficar disponível (até 60 segundos)
    $waitCount = 0
    $maxWait = 60
    
    while ($waitCount -lt $maxWait) {
        Start-Sleep -Seconds 1
        if (Test-ApiHealth) {
            Write-Log "✅ API iniciada com sucesso!"
            return $true
        }
        $waitCount++
        Write-Progress -Activity "Iniciando API" -Status "Aguardando... ($waitCount/$maxWait)" -PercentComplete (($waitCount / $maxWait) * 100)
    }
    
    Write-Log "❌ Falha ao iniciar API após ${maxWait}s"
    return $false
}

function Start-MonitorLoop {
    Write-Log "🚀 Iniciando monitoramento contínuo da CommanderAPI"
    Write-Log "URL: $ApiUrl"
    Write-Log "Intervalo de verificação: ${CheckInterval}s"
    Write-Host "`n================================================" -ForegroundColor Cyan
    Write-Host "    CommanderAPI Health Monitor" -ForegroundColor Cyan
    Write-Host "================================================" -ForegroundColor Cyan
    Write-Host "Pressione Ctrl+C para parar o monitoramento`n" -ForegroundColor Yellow
    
    $consecutiveFailures = 0
    
    while ($true) {
        try {
            if (Test-ApiHealth) {
                if ($consecutiveFailures -gt 0) {
                    Write-Log "✅ API voltou online após $consecutiveFailures falhas"
                }
                $consecutiveFailures = 0
                $status = "● $(Get-Date -Format 'HH:mm:ss') - API Online    "
                Write-Host "`r$status" -NoNewline -ForegroundColor Green
            }
            else {
                $consecutiveFailures++
                $status = "● $(Get-Date -Format 'HH:mm:ss') - API Offline ($consecutiveFailures/$MaxRetries)    "
                Write-Host "`r$status" -NoNewline -ForegroundColor Red
                
                if ($consecutiveFailures -ge $MaxRetries) {
                    Write-Host ""
                    Write-Log "❌ API offline há $consecutiveFailures tentativas. Reiniciando..."
                    
                    Stop-ApiProcesses
                    
                    if (Start-Api) {
                        $consecutiveFailures = 0
                        Write-Log "✅ Reinicialização concluída"
                    }
                    else {
                        Write-Log "❌ Falha na reinicialização. Tentando novamente em ${CheckInterval}s..."
                    }
                }
            }
            
            Start-Sleep -Seconds $CheckInterval
        }
        catch {
            Write-Log "Erro no monitoramento: $($_.Exception.Message)"
            Start-Sleep -Seconds $CheckInterval
        }
    }
}

# Tratamento de interrupção
$scriptBlock = {
    Write-Host "`n🛑 Monitor interrompido pelo usuário" -ForegroundColor Yellow
    Write-Log "🛑 Monitor interrompido pelo usuário"
    exit 0
}

Register-EngineEvent -SourceIdentifier PowerShell.Exiting -Action $scriptBlock

# Verificação inicial
Write-Log "Verificando status inicial da API..."
if (-not (Test-ApiHealth)) {
    Write-Log "API não está rodando. Iniciando..."
    Stop-ApiProcesses
    Start-Api
}

# Inicia o loop de monitoramento
Start-MonitorLoop