# Stage 1: Build the fat JAR using Gradle wrapper
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Copy Gradle wrapper and version catalog
COPY gradle ./gradle
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .
COPY gradle/libs.versions.toml ./gradle/libs.versions.toml

# Copy source
COPY src ./src

# Make gradlew executable
RUN chmod +x ./gradlew

# Build fat JAR (includes Springdoc)
RUN ./gradlew clean bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy the fat JAR from builder stage
COPY --from=builder /app/build/libs/CountryRoutingPWC-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
