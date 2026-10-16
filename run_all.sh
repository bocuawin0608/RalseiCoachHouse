#!/bin/bash
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-01102006Duc.}"
export JWT_SECRET="${JWT_SECRET:-856a9ee1487e41d4f230d5af0cbacdd01e3261d2aa92f36cd9ca6ccca5cb104b}"
export SEPAY_API_TOKEN="${SEPAY_API_TOKEN:-ADZYZ2QKFCAJGBO04FG4F8DOV55H8H71JZPCMDZSGWYP76TMLYQPSIERPXYVQUAX}"
export GOONG_API_KEY="${GOONG_API_KEY:-abkgJAy0A7N6cJnfaEQ4bwnI7MOJ7lD9eYbVh8P5}"

start_service() {
  local service_name="$1"
  local port="$2"
  local jar_file="$DIR/${service_name}-service/target/${service_name}-service-0.0.1-SNAPSHOT.jar"
  local unit_name="ralsei-${service_name}"
  
  echo "Starting ${service_name}-service (Port ${port})..."
  systemctl --user stop "$unit_name" 2>/dev/null || true
  systemctl --user reset-failed "$unit_name" 2>/dev/null || true

  if systemctl --user status >/dev/null 2>&1 && [ -f "$jar_file" ]; then
    systemd-run --user --unit="$unit_name" --collect \
      --setenv=SPRING_DATASOURCE_PASSWORD="$SPRING_DATASOURCE_PASSWORD" \
      --setenv=JWT_SECRET="$JWT_SECRET" \
      --setenv=SEPAY_API_TOKEN="$SEPAY_API_TOKEN" \
      --setenv=GOONG_API_KEY="$GOONG_API_KEY" \
      /bin/bash -c "exec java -jar '$jar_file' > '$DIR/${service_name}-service.log' 2>&1"
    systemctl --user show -p MainPID --value "$unit_name" > "$DIR/${service_name}-service.pid" 2>/dev/null || true
  elif [ -f "$jar_file" ]; then
    nohup java -jar "$jar_file" > "$DIR/${service_name}-service.log" 2>&1 < /dev/null &
    echo $! > "$DIR/${service_name}-service.pid"
  else
    nohup mvn spring-boot:run -f "$DIR/${service_name}-service/pom.xml" > "$DIR/${service_name}-service.log" 2>&1 < /dev/null &
    echo $! > "$DIR/${service_name}-service.pid"
  fi
}

start_service "auth" "8081"
start_service "customer" "8082"
start_service "staff" "8083"
start_service "driver" "8084"
start_service "payment" "8080"

echo "All services are starting up in the background."
