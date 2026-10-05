FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the WAR file built by Jenkins in the previous stage
COPY target/*.war app.war

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.war"]
