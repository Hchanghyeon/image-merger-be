FROM eclipse-temurin:21-jdk
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
# JVM 옵션: 최소 힙 4GB, 최대 힙 8GB
ENTRYPOINT ["java", "-Xms4g", "-Xmx8g", "-jar", "app.jar"]
