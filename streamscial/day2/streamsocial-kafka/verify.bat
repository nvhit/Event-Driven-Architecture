@echo off
echo 🔍 Verifying StreamSocial Event Taxonomy setup...
echo.

:: Check Java version
java -version >nul 2>&1
if %errorlevel%==0 (
    echo ✅ Java detected:
    java -version 2>&1 | findstr /i "version"
) else (
    echo ❌ Java not found (requires Java 17+)
)
echo.

:: Check Maven
mvn --version >nul 2>&1
if %errorlevel%==0 (
    echo ✅ Maven detected:
    mvn --version 2>&1 | findstr /i "Apache Maven"
) else (
    echo ❌ Maven not found
)
echo.

:: Check Docker (for Kafka)
docker --version >nul 2>&1
if %errorlevel%==0 (
    echo ✅ Docker detected:
    docker --version
) else (
    echo ⚠️ Docker not found (optional - needed for Kafka)
)
echo.

:: Check Node.js (for frontend)
node --version >nul 2>&1
if %errorlevel%==0 (
    echo ✅ Node.js detected: 
    node --version
) else (
    echo ❌ Node.js not found (needed for React Dashboard)
)
echo.

:: Check backend-java files
echo 📁 Checking backend files...
set /a PASS=0
set /a FAIL=0

for %%f in (
    "backend-java\pom.xml"
    "backend-java\src\main\java\com\streamsocial\eventtaxonomy\EventTaxonomyApplication.java"
    "backend-java\src\main\java\com\streamsocial\eventtaxonomy\events\BaseEvent.java"
    "backend-java\src\main\java\com\streamsocial\eventtaxonomy\events\EventType.java"
    "backend-java\src\main\java\com\streamsocial\eventtaxonomy\events\EventBus.java"
    "backend-java\src\main\java\com\streamsocial\eventtaxonomy\producer\EventProducerService.java"
    "backend-java\src\main\java\com\streamsocial\eventtaxonomy\consumer\EventConsumerService.java"
    "backend-java\src\main\java\com\streamsocial\eventtaxonomy\handlers\FeedHandler.java"
    "backend-java\src\main\java\com\streamsocial\eventtaxonomy\handlers\NotificationHandler.java"
    "backend-java\src\main\java\com\streamsocial\eventtaxonomy\controller\EventController.java"
    "backend-java\src\main\java\com\streamsocial\eventtaxonomy\websocket\EventWebSocketHandler.java"
    "backend-java\src\main\java\com\streamsocial\eventtaxonomy\config\KafkaConfig.java"
    "backend-java\src\main\java\com\streamsocial\eventtaxonomy\config\EventBusConfig.java"
    "backend-java\src\main\java\com\streamsocial\eventtaxonomy\config\CorsConfig.java"
    "backend-java\src\main\resources\application.properties"
) do (
    if exist %%f (
        echo   ✅ %%~f
        set /a PASS+=1
    ) else (
        echo   ❌ %%~f MISSING
        set /a FAIL+=1
    )
)
echo.

:: Check frontend files
echo 📁 Checking frontend files...
for %%f in (
    "frontend\package.json"
    "frontend\vite.config.js"
    "frontend\src\App.jsx"
    "frontend\src\components\EventDashboard.jsx"
    "frontend\src\components\EventPublisher.jsx"
) do (
    if exist %%f (
        echo   ✅ %%~f
    ) else (
        echo   ❌ %%~f MISSING
    )
)
echo.

:: Check docker-compose
if exist "docker-compose.yml" (
    echo ✅ docker-compose.yml found (Kafka setup ready)
) else (
    echo ⚠️ docker-compose.yml not found
)
echo.

:: Try to compile backend
echo 🔨 Verifying backend compilation...
cd backend-java
call mvn compile -q >nul 2>&1
if %errorlevel%==0 (
    echo ✅ Backend compiles successfully
) else (
    echo ⚠️ Backend compilation failed (check Java/Maven setup)
)
cd ..
echo.

echo ============================================================
echo 🎯 Event Taxonomy (10 Event Types):
echo.
echo   🟢 User Actions (6):
echo      user_registration, user_login, user_profile_update,
echo      user_follow, user_post_create, user_post_delete
echo.
echo   🔵 Content Interactions (3):
echo      content_like, content_comment, content_share
echo.
echo   🟠 System Events (1):
echo      system_notification
echo ============================================================
echo.
echo 🚀 Quick Start:
echo    1. docker-compose up -d          (start Kafka)
echo    2. cd backend-java ^&^& mvn spring-boot:run  (start API)
echo    3. cd frontend ^&^& npm run dev    (start Dashboard)
echo.
pause
