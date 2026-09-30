#!/bin/bash
DIR="/home/loliconhihi/Documents/Project/nhaxetuanmv"

echo "Starting Auth Service (Port 8081)..."
mvn spring-boot:run -f "$DIR/auth-service/pom.xml" > "$DIR/auth-service.log" 2>&1 &
echo $! > "$DIR/auth-service.pid"

echo "Starting Customer Service (Port 8082)..."
mvn spring-boot:run -f "$DIR/customer-service/pom.xml" > "$DIR/customer-service.log" 2>&1 &
echo $! > "$DIR/customer-service.pid"

echo "Starting Staff Service (Port 8083)..."
mvn spring-boot:run -f "$DIR/staff-service/pom.xml" > "$DIR/staff-service.log" 2>&1 &
echo $! > "$DIR/staff-service.pid"

echo "Starting Driver Service (Port 8084)..."
mvn spring-boot:run -f "$DIR/driver-service/pom.xml" > "$DIR/driver-service.log" 2>&1 &
echo $! > "$DIR/driver-service.pid"

echo "All services are starting up in the background."
