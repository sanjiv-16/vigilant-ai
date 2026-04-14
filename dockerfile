# Stage 1: Build the application using Gradle
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon

# Stage 2: Create the production image
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=builder /app/build/libs/*0.0.1.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]