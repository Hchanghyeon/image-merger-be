FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ARG JAR_FILE=/app/build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-Xms4g", "-Xmx8g", "-jar", "app.jar"]
