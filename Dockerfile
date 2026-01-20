# =============================================================================
# Production-Grade Multi-Stage Dockerfile for Spring Boot Application
# =============================================================================
# Stage 1: Build Stage
# Purpose: Compile the application with all build dependencies
# =============================================================================
FROM eclipse-temurin:17-jdk-jammy AS builder

# Set working directory
WORKDIR /build

# Copy Maven wrapper and pom.xml first (better cache utilization)
COPY .mvn .mvn
COPY mvnw pom.xml ./

# Make Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests - they should run in CI before Docker build)
RUN ./mvnw package -DskipTests -Dcheckstyle.skip=true

# Extract Spring Boot layers for better caching
RUN java -Djarmode=layertools -jar target/calculator.jar extract --destination target/extracted

# =============================================================================
# Stage 2: Runtime Stage
# Purpose: Minimal production image with only runtime dependencies
# =============================================================================
FROM eclipse-temurin:17-jre-alpine AS runtime

# Security: Create non-root user and group
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -s /bin/sh -D appuser

# Set working directory
WORKDIR /app

# Copy layered application from builder stage (enables better Docker layer caching)
COPY --from=builder /build/target/extracted/dependencies/ ./
COPY --from=builder /build/target/extracted/spring-boot-loader/ ./
COPY --from=builder /build/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/target/extracted/application/ ./

# Security: Set ownership and switch to non-root user
RUN chown -R appuser:appgroup /app
USER appuser

# Expose only the required port
EXPOSE 8080

# Health check using wget (available in alpine)
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM optimization flags for containers
# - MaxRAMPercentage: Use 75% of available memory
# - UseG1GC: Garbage collector optimized for low latency
# - UseStringDeduplication: Reduce memory for duplicate strings
# - ExitOnOutOfMemoryError: Fast fail on OOM for container orchestration
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom"

# Run application using Spring Boot's layered launcher
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
