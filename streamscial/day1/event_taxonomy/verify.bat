@echo off
echo 🔍 Verifying StreamSocial setup (Spring Boot)...
echo.

:: Check Java version
java -version >nul 2>&1
if %errorlevel%==0 (
    echo ✅ Java detected:
    java -version 2>&1 | findstr /i "version"
) else (
    echo ❌ Java not found
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

:: Check backend-java files
echo 📁 Checking backend files...
set BACKEND_FILES=backend-java\pom.xml backend-java\src\main\java\com\streamsocial\eventtaxonomy\EventTaxonomyApplication.java backend-java\src\main\java\com\streamsocial\eventtaxonomy\events\BaseEvent.java backend-java\src\main\java\com\streamsocial\eventtaxonomy\events\EventType.java backend-java\src\main\java\com\streamsocial\eventtaxonomy\events\EventBus.java backend-java\src\main\java\com\streamsocial\eventtaxonomy\handlers\FeedHandler.java backend-java\src\main\java\com\streamsocial\eventtaxonomy\handlers\NotificationHandler.java backend-java\src\main\java\com\streamsocial\eventtaxonomy\controller\EventController.java backend-java\src\main\java\com\streamsocial\eventtaxonomy\websocket\EventWebSocketHandler.java backend-java\src\main\java\com\streamsocial\eventtaxonomy\config\WebSocketConfig.java backend-java\src\main\java\com\streamsocial\eventtaxonomy\config\EventBusConfig.java backend-java\src\main\java\com\streamsocial\eventtaxonomy\config\CorsConfig.java backend-java\src\main\resources\application.properties

for %%f in (%BACKEND_FILES%) do (
    if exist "%%f" (
        echo ✅ %%f
    ) else (
        echo ❌ %%f missing
    )
)
echo.

:: Check frontend files
echo 📁 Checking frontend files...
set FRONTEND_FILES=frontend\src\App.jsx frontend\src\components\EventDashboard.jsx frontend\src\components\EventPublisher.jsx frontend\package.json

for %%f in (%FRONTEND_FILES%) do (
    if exist "%%f" (
        echo ✅ %%f
    ) else (
        echo ❌ %%f missing
    )
)
echo.

:: Check if frontend is built
if exist "frontend\dist" (
    echo ✅ Frontend built
) else (
    echo ⚠️ Frontend not built (run: cd frontend ^&^& npm run build)
)
echo.

:: Try to compile backend
echo 🔨 Verifying backend compilation...
cd backend-java
call mvn compile -q >nul 2>&1
if %errorlevel%==0 (
    echo ✅ Backend compiles successfully
) else (
    echo ❌ Backend compilation failed
)
cd ..
echo.

echo 🎯 Setup verification complete!
pause
