#!/bin/bash
# Script to stop all 4 microservices

for service in auth customer staff driver; do
  if [ -f "${service}-service.pid" ]; then
    PID=$(cat "${service}-service.pid")
    echo "Stopping ${service}-service (PID: $PID)..."
    kill $PID 2>/dev/null
    rm "${service}-service.pid"
  else
    echo "PID file for ${service}-service not found."
  fi
done

echo "Attempting to kill any lingering spring-boot processes..."
pkill -f "spring-boot:run"

echo "All services stopped."
