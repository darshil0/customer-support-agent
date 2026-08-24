# Multi-stage Dockerfile for Customer Support Application

# Stage 1: Build the Spring Boot application JAR
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package application (skip tests in docker build for speed)
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root system group and user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled JAR from builder stage
COPY --from=builder /app/target/customer-support-agent-1.2.1.jar app.jar

# Set user permissions
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8000

ENV GOOGLE_API_KEY=""
ENV DB_HOST="localhost"
ENV DB_PORT="5432"
ENV DB_NAME="customer_support"
ENV DB_USER="postgres"
ENV DB_PASSWORD="postgres"

ENTRYPOINT ["java", "-jar", "app.jar"]
