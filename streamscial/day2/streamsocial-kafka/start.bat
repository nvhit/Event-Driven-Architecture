@echo off
echo 🚀 Starting StreamSocial Day 1 - Event Taxonomy (Spring Boot + React)...
echo.

:: Check if Docker is running for Kafka (optional)
docker info >nul 2>&1
if %errorlevel%==0 (
    echo 🐳 Docker detected. Starting Kafka...
    docker-compose up -d
    timeout /t 5 /nobreak >nul
    echo ✅ Kafka started on localhost:9092
) else (
    echo ⚠️ Docker not running. Using in-memory EventBus (no Kafka).
    echo    To enable Kafka: docker-compose up -d
)

echo.

:: Install frontend dependencies if not exists
if not exist "frontend\node_modules" (
    echo 📦 Installing frontend dependencies...
    cd frontend
    call npm install
    cd ..
)

:: Build frontend
echo 🏗️ Building frontend...
cd frontend
call npm run build
cd ..

:: Start backend server (Spring Boot)
echo 🔧 Starting Spring Boot backend on port 8080...
cd backend-java
start "StreamSocial-Backend" cmd /c "mvn spring-boot:run"
cd ..

:: Wait for server to start
echo ⏳ Waiting for server to start...
set /a count=0
:waitloop
if %count% geq 30 goto timeout
timeout /t 1 /nobreak >nul
curl -s http://localhost:8080/api/v1/events/stats >nul 2>&1
if %errorlevel%==0 goto started
set /a count+=1
goto waitloop

:timeout
echo ⚠️ Server may not have started yet, check the backend window.
goto info

:started
echo ✅ Backend started successfully!

:info
echo.
echo ============================================================
echo ✅ StreamSocial Event Taxonomy is running!
echo ============================================================
echo.
echo 🌐 Backend API:  http://localhost:8080
echo 📊 API Base:     http://localhost:8080/api/v1/events
echo 🔌 WebSocket:    ws://localhost:8080/ws
echo.
echo 📋 Available Endpoints:
echo    POST /api/v1/events/user/register
echo    POST /api/v1/events/user/login
echo    POST /api/v1/events/user/profile-update
echo    POST /api/v1/events/user/follow
echo    POST /api/v1/events/user/post-create
echo    POST /api/v1/events/user/post-delete
echo    POST /api/v1/events/content/like
echo    POST /api/v1/events/content/comment
echo    POST /api/v1/events/content/share
echo    POST /api/v1/events/system/notification
echo    GET  /api/v1/events/recent
echo    GET  /api/v1/events/stats
echo.
echo 🖥️ To start React Dashboard:
echo    cd frontend ^&^& npm run dev
echo    Open http://localhost:3000
echo.
echo 🛑 Run stop.bat to stop the server
echo ============================================================
echo.
pause
