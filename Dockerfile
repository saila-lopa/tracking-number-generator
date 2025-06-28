# 1. Builder stage: use Gradle 8.4 with JDK21 to compile the application
FROM gradle:8.4-jdk21 AS builder
WORKDIR /app

# Copy the Gradle wrapper and build files
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle ./
COPY settings.gradle ./
COPY src ./src


# Grant execution permission to the Gradle wrapper
RUN chmod +x gradlew

# Build the application, skipping tests. To include tests, remove the -x test flag.

RUN ./gradlew clean build

# Copy the built JAR file (assuming your JAR is placed in build/libs/ directory)
RUN VERSION=$(./gradlew properties -q | grep "version:" | awk '{print $2}') && \
    cp build/libs/tracking-id-generator-$VERSION.jar /app/app.jar


# Use a runtime image for running the application
FROM gradle:8.4-jdk21
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=builder /app/app.jar /app/app.jar


# Expose the application port
EXPOSE 8080

# Create a logs directory
RUN mkdir -p /app/logs

ENTRYPOINT ["java", "-jar", "app.jar"]

