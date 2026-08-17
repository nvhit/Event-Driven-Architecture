#!/bin/bash
echo "🛑 Stopping StreamSocial..."

if [ -f .pids ]; then
    while read -r line; do
        if [[ $line == Backend* ]]; then
            PID=$(echo $line | cut -d' ' -f3)
            kill $PID 2>/dev/null
            echo "Stopped backend (PID: $PID)"
        fi
    done < .pids
    rm .pids
fi

# Kill any remaining Spring Boot / Java processes on port 8080
lsof -ti:8080 | xargs kill -9 2>/dev/null

# Stop Docker Kafka
docker-compose down 2>/dev/null

echo "✅ StreamSocial stopped"
