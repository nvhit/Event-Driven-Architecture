#!/bin/bash
echo "🚀 Starting StreamSocial Day 1 (Spring Boot)..."

# Install frontend dependencies if not exists
if [ ! -d "frontend/node_modules" ]; then
    echo "📦 Installing frontend dependencies..."
    cd frontend && npm install && cd ..
fi

# Build frontend
echo "🏗️ Building frontend..."
cd frontend && npm run build && cd ..

# Create dist directory if it doesn't exist
mkdir -p frontend/dist

# Start backend server (Spring Boot)
echo "🔧 Starting Spring Boot backend server..."
cd backend-java && mvn spring-boot:run -q &
BACKEND_PID=$!
cd ..

echo "Backend PID: $BACKEND_PID" > .pids

# Wait for server to start
echo "⏳ Waiting for server to start..."
for i in {1..30}; do
    if curl -s http://localhost:8000/api/stats > /dev/null 2>&1; then
        break
    fi
    sleep 1
done

echo "✅ StreamSocial is running!"
echo "🌐 Open http://localhost:8000 in your browser"
echo "📊 API endpoints: http://localhost:8000/api/"
echo "🛑 Run ./stop.sh to stop the server"

# Wait for user input to keep script running
read -p "Press Enter to stop the server..."
kill $BACKEND_PID 2>/dev/null
