# 1단계: Gradle 이미지로 Spring Boot 실행 jar를 빌드합니다.
FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# 2단계: 빌드된 jar만 가벼운 Java runtime 이미지에 복사해 실행합니다.
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
