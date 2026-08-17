@echo off
echo 🛑 Stopping StreamSocial...

:: Kill Java process on port 8080
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    echo Stopping process PID: %%a
    taskkill /PID %%a /F >nul 2>&1
)

:: Kill any remaining mvn/java processes related to spring-boot
taskkill /FI "WINDOWTITLE eq StreamSocial-Backend" /F >nul 2>&1

:: Stop Docker Kafka if running
docker-compose down >nul 2>&1

echo ✅ StreamSocial stopped
pause
