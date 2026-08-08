#!/bin/bash
echo "🚀 Starting StreamSocial Day 1 - Event Taxonomy (Spring Boot + React)..."
echo ""

# Check if Docker is running for Kafka (optional)
if docker info > /dev/null 2>&1; then
    echo "🐳 Docker detected. Starting Kafka..."
    docker-compose up -d
    sleep 5
    echo "✅ Kafka started on localhost:9092"
else
    echo "⚠️ Docker not running. Using in-memory EventBus (no Kafka)."
    echo "   To enable Kafka: docker-compose up -d"
fi

echo ""

# Install frontend dependencies if not exists
if [ ! -d "frontend/node_modules" ]; then
    echo "📦 Installing frontend dependencies..."
    cd frontend && npm install && cd ..
fi

# Build frontend
echo "🏗️ Building frontend..."
cd frontend && npm run build && cd ..

# Start backend server (Spring Boot)
echo "🔧 Starting Spring Boot backend on port 8080..."
cd backend-java && mvn spring-boot:run -q &
BACKEND_PID=$!
cd ..

echo "Backend PID: $BACKEND_PID" > .pids

# Wait for server to start
echo "⏳ Waiting for server to start..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/v1/events/stats > /dev/null 2>&1; then
        break
    fi
    sleep 1
done

echo ""
echo "============================================================"
echo "✅ StreamSocial Event Taxonomy is running!"
echo "============================================================"
echo ""
echo "🌐 Backend API:  http://localhost:8080"
echo "📊 API Base:     http://localhost:8080/api/v1/events"
echo "🔌 WebSocket:    ws://localhost:8080/ws"
echo ""
echo "🖥️ To start React Dashboard:"
echo "   cd frontend && npm run dev"
echo "   Open http://localhost:3000"
echo ""
echo "🛑 Run ./stop.sh to stop the server"
echo "============================================================"
echo ""

# Wait for user input to keep script running
read -p "Press Enter to stop the server..."
kill $BACKEND_PID 2>/dev/null
docker-compose down 2>/dev/null
