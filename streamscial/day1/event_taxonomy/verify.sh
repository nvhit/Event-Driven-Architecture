#!/bin/bash
echo "🔍 Verifying StreamSocial setup (Spring Boot)..."

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1)
if echo "$JAVA_VERSION" | grep -qE '"(17|18|19|20|21)'; then
    echo "✅ Java 17+ detected: $JAVA_VERSION"
else
    echo "❌ Java 17+ not found. Current: $JAVA_VERSION"
fi

# Check Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn --version 2>&1 | head -n 1)
    echo "✅ Maven detected: $MVN_VERSION"
else
    echo "❌ Maven not found"
fi

# Check backend-java files
BACKEND_FILES=(
    "backend-java/pom.xml"
    "backend-java/src/main/java/com/streamsocial/eventtaxonomy/EventTaxonomyApplication.java"
    "backend-java/src/main/java/com/streamsocial/eventtaxonomy/events/BaseEvent.java"
    "backend-java/src/main/java/com/streamsocial/eventtaxonomy/events/EventType.java"
    "backend-java/src/main/java/com/streamsocial/eventtaxonomy/events/EventBus.java"
    "backend-java/src/main/java/com/streamsocial/eventtaxonomy/handlers/FeedHandler.java"
    "backend-java/src/main/java/com/streamsocial/eventtaxonomy/handlers/NotificationHandler.java"
    "backend-java/src/main/java/com/streamsocial/eventtaxonomy/controller/EventController.java"
    "backend-java/src/main/java/com/streamsocial/eventtaxonomy/websocket/EventWebSocketHandler.java"
    "backend-java/src/main/java/com/streamsocial/eventtaxonomy/config/WebSocketConfig.java"
    "backend-java/src/main/java/com/streamsocial/eventtaxonomy/config/EventBusConfig.java"
    "backend-java/src/main/java/com/streamsocial/eventtaxonomy/config/CorsConfig.java"
    "backend-java/src/main/resources/application.properties"
)

echo ""
echo "📁 Checking backend files..."
for file in "${BACKEND_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ $file missing"
    fi
done

# Check frontend files
FRONTEND_FILES=(
    "frontend/src/App.jsx"
    "frontend/src/components/EventDashboard.jsx"
    "frontend/src/components/EventPublisher.jsx"
    "frontend/package.json"
)

echo ""
echo "📁 Checking frontend files..."
for file in "${FRONTEND_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ $file missing"
    fi
done

# Check if frontend is built
if [ -d "frontend/dist" ]; then
    echo "✅ Frontend built"
else
    echo "⚠️  Frontend not built (run: cd frontend && npm run build)"
fi

# Try to compile backend
echo ""
echo "🔨 Verifying backend compilation..."
cd backend-java
if mvn compile -q 2>/dev/null; then
    echo "✅ Backend compiles successfully"
else
    echo "❌ Backend compilation failed"
fi
cd ..

echo ""
echo "🎯 Setup verification complete!"
