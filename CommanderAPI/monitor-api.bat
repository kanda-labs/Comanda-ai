@echo off
setlocal enabledelayedexpansion

REM CommanderAPI Health Monitor for Windows
REM Monitora continuamente a API e reinicia se necessário

set API_URL=http://localhost:8081/health
set API_PORT=8081
set CHECK_INTERVAL=30
set MAX_RETRIES=3
set LOG_FILE=monitor.log

echo [%date% %time%] Iniciando monitoramento continuo da CommanderAPI >> %LOG_FILE%
echo.
echo ================================================
echo    CommanderAPI Health Monitor
echo ================================================
echo URL: %API_URL%
echo Intervalo: %CHECK_INTERVAL%s
echo Pressione Ctrl+C para parar o monitoramento
echo ================================================
echo.

set consecutive_failures=0

:monitor_loop
    REM Verifica se a API está online
    curl -s --connect-timeout 5 --max-time 10 "%API_URL%" >nul 2>&1
    if !errorlevel! equ 0 (
        REM API está online
        if !consecutive_failures! gtr 0 (
            echo [%date% %time%] API voltou online apos !consecutive_failures! falhas >> %LOG_FILE%
        )
        set consecutive_failures=0
        echo [%time%] API Online
    ) else (
        REM API está offline
        set /a consecutive_failures+=1
        echo [%time%] API Offline ^(!consecutive_failures!/!MAX_RETRIES!^)
        
        if !consecutive_failures! geq !MAX_RETRIES! (
            echo.
            echo [%date% %time%] API offline ha !consecutive_failures! tentativas. Reiniciando... >> %LOG_FILE%
            echo [%time%] REINICIANDO API...
            
            REM Mata processos existentes
            call :kill_processes
            
            REM Inicia a API
            call :start_api
            
            if !errorlevel! equ 0 (
                set consecutive_failures=0
                echo [%date% %time%] Reinicializacao concluida >> %LOG_FILE%
                echo [%time%] Reinicializacao concluida
            ) else (
                echo [%date% %time%] Falha na reinicializacao >> %LOG_FILE%
                echo [%time%] Falha na reinicializacao
            )
        )
    )
    
    timeout /t %CHECK_INTERVAL% >nul
    goto monitor_loop

:kill_processes
    echo [%date% %time%] Matando processos existentes... >> %LOG_FILE%
    
    REM Mata processos Java/Gradle
    taskkill /f /im java.exe >nul 2>&1
    taskkill /f /im javaw.exe >nul 2>&1
    
    REM Mata processos usando a porta 8081
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8081"') do (
        if not "%%a"=="" (
            taskkill /f /pid %%a >nul 2>&1
        )
    )
    
    timeout /t 3 >nul
    exit /b 0

:start_api
    echo [%date% %time%] Iniciando CommanderAPI... >> %LOG_FILE%
    
    REM Inicia a API em background
    start /b gradlew run > api-output.log 2>&1
    
    REM Aguarda a API ficar disponível (até 60 segundos)
    set wait_count=0
    set max_wait=60
    
    :wait_loop
        if !wait_count! geq !max_wait! (
            echo [%date% %time%] Falha ao iniciar API apos !max_wait!s >> %LOG_FILE%
            exit /b 1
        )
        
        timeout /t 1 >nul
        curl -s --connect-timeout 5 --max-time 10 "%API_URL%" >nul 2>&1
        if !errorlevel! equ 0 (
            echo [%date% %time%] API iniciada com sucesso >> %LOG_FILE%
            exit /b 0
        )
        
        set /a wait_count+=1
        echo Aguardando API... (!wait_count!/!max_wait!)
        goto wait_loop