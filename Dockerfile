# Stage 1: 빌드 단계 (Gradle과 Java 21 환경)
FROM gradle:8.2-jdk21 AS builder
WORKDIR /home/gradle/project
# 프로젝트 소스 전체를 복사 (소유권은 gradle 사용자로 설정)
COPY --chown=gradle:gradle .github .
# Gradle을 사용하여 BootJar 생성 (테스트는 생략)
RUN gradle bootJar --no-daemon

# Stage 2: 실행 단계 (Java 21 Runtime)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# 빌드 단계에서 생성된 JAR 파일 복사 (빌드 결과물 이름은 프로젝트에 맞게 조정)
COPY --from=builder /home/gradle/project/build/libs/*.jar my-app.jar
EXPOSE 8080
# JVM 옵션: 최소 힙 4GB, 최대 힙 8GB
ENTRYPOINT ["java", "-Xms4g", "-Xmx8g", "-jar", "my-app.jar"]
