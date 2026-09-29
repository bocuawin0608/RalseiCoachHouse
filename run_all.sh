#!/bin/bash
DIR="/home/loliconhihi/Documents/Project/nhaxetuanmv"

echo "Starting Auth Service (Port 8081)..."
cd "$DIR/auth-service" && nohup mvn spring-boot:run > "$DIR/auth-service.log" 2>&1 &
echo $! > "$DIR/auth-service.pid"

echo "Starting Customer Service (Port 8082)..."
cd "$DIR/customer-service" && nohup mvn spring-boot:run > "$DIR/customer-service.log" 2>&1 &
echo $! > "$DIR/customer-service.pid"

echo "Starting Staff Service (Port 8083)..."
cd "$DIR/staff-service" && nohup mvn spring-boot:run > "$DIR/staff-service.log" 2>&1 &
echo $! > "$DIR/staff-service.pid"

echo "Starting Driver Service (Port 8084)..."
cd "$DIR/driver-service" && nohup mvn spring-boot:run > "$DIR/driver-service.log" 2>&1 &
echo $! > "$DIR/driver-service.pid"

echo "All services are starting up in the background."
