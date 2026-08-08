@echo off
echo 🚀 Starting StreamSocial Day 1 (Spring Boot)...

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

:: Create dist directory if it doesn't exist
if not exist "frontend\dist" mkdir frontend\dist

:: Start backend server (Spring Boot)
echo 🔧 Starting Spring Boot backend server...
cd backend-java
start "StreamSocial-Backend" cmd /c "mvn spring-boot:run"
cd ..

:: Wait for server to start
echo ⏳ Waiting for server to start...
set /a count=0
:waitloop
if %count% geq 30 goto timeout
timeout /t 1 /nobreak >nul
curl -s http://localhost:8000/api/stats >nul 2>&1
if %errorlevel%==0 goto started
set /a count+=1
goto waitloop

:timeout
echo ⚠️ Server may not have started yet, check the backend window.
goto info

:started
echo ✅ Server started successfully!

:info
echo.
echo ✅ StreamSocial is running!
echo 🌐 Open http://localhost:8000 in your browser
echo 📊 API endpoints: http://localhost:8000/api/
echo 🛑 Run stop.bat to stop the server
echo.
pause
