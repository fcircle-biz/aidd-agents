# Todo Application Production Dockerfile
# Multi-stage build for optimized production deployment

# Build stage
FROM openjdk:17-jdk-slim AS build

# Set build arguments
ARG APP_VERSION=1.0.0
ARG BUILD_DATE
ARG VCS_REF

# Labels for better container management
LABEL maintainer="system@example.com" \
      version="${APP_VERSION}" \
      description="Todo Application - Spring Boot" \
      build-date="${BUILD_DATE}" \
      vcs-ref="${VCS_REF}"

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy Maven files for dependency caching
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:17-jre-slim AS production

# Create non-root user for security
RUN groupadd -r todoapp && useradd -r -g todoapp todoapp

# Set working directory
WORKDIR /app

# Install necessary packages and clean up
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Copy the built JAR from build stage
COPY --from=build /app/target/todo-app-*.jar app.jar

# Create directories for logs and data
RUN mkdir -p /app/logs /app/data && \
    chown -R todoapp:todoapp /app

# Switch to non-root user
USER todoapp

# Set JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heapdump.hprof"

# Set Spring profiles
ENV SPRING_PROFILES_ACTIVE=prod

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health/production || exit 1

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]