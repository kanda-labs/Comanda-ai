# Build stage
FROM gradle:8.5-jdk22 AS build
WORKDIR /app

# Copy gradle configuration files
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Download dependencies (cached if unchanged)
RUN gradle dependencies --no-daemon

# Copy source code
COPY src ./src

# Build the application
RUN gradle build --no-daemon

# Runtime stage
FROM eclipse-temurin:22-jre-alpine

# Add non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Create directories with proper permissions
RUN mkdir -p /app/logs /app/data && \
    chown -R appuser:appgroup /app

# Copy the jar from the build stage
COPY --from=build /app/build/libs/*.jar /app/commander-api.jar

# Set environment variables
ENV PORT=8080
ENV HOST=0.0.0.0
ENV LOG_LEVEL=INFO
ENV DATABASE_URL=jdbc:sqlite:/app/data/data.db
ENV DATABASE_DRIVER=org.sqlite.JDBC

# Switch to non-root user
USER appuser

# Expose the port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/health || exit 1

# Set entry point
ENTRYPOINT ["java", "-jar", "/app/commander-api.jar"]

