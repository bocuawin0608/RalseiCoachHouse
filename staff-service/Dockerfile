FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# We need to copy the parent pom as well to build the module
COPY pom.xml .
COPY staff-service staff-service

RUN mvn -pl staff-service -am clean package -DskipTests -B

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/staff-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
