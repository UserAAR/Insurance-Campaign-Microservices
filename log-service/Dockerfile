# Stage 1: Build stage
FROM maven:3.9.11-amazoncorretto-25 AS build

# Set working directory
WORKDIR /workspace

# Copy full multi-module project
COPY . .

# Build only log-service and required modules
RUN mvn -pl log-service -am clean package -DskipTests

# Stage 2: Run stage
FROM amazoncorretto:25

# Set working directory
WORKDIR /app

# Copy the executable Spring Boot JAR file from the build stage
COPY --from=build /workspace/log-service/target/log-service-*.jar log-service.jar

# Expose port 8084
EXPOSE 8084

# Set the entrypoint command for running the application
ENTRYPOINT ["java", "--enable-preview", "-jar", "log-service.jar"]