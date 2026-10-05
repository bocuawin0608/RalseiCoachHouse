FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the WAR file prepared by Jenkins
COPY app.war app.war

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.war"]
