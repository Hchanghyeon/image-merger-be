# Stage 1: 빌드 단계
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY . .
RUN gradle clean build --no-daemon

# Stage 2: 실행 단계
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xms4g", "-Xmx8g", "-jar", "app.jar"]
