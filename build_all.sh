#!/bin/bash
set -e

SERVICES=("customer-service" "driver-service" "payment-service" "staff-service")

for SVC in "${SERVICES[@]}"; do
  echo "Building $SVC..."
  (
    cd $SVC
    mvn clean package -DskipTests
    cp target/*-0.0.1-SNAPSHOT.jar temp.jar
    cat << 'DOCKER' > Dockerfile.fast
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY temp.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
DOCKER
    docker build -f Dockerfile.fast -t ralsei/$SVC:latest .
    rm temp.jar Dockerfile.fast
    kind load docker-image ralsei/$SVC:latest --name local
  ) &
done

wait
echo "All microservices built and loaded successfully!"
