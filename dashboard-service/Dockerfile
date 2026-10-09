# Stage 1: Build stage
FROM maven:3.9.11-amazoncorretto-25 AS build

# Set working directory
WORKDIR /workspace

# Copy full multi-module project
COPY . .

# Build only dashboard-service and required modules
RUN mvn -pl dashboard-service -am clean package -DskipTests

# Stage 2: Run stage
FROM amazoncorretto:25

# Set working directory
WORKDIR /app

# Copy the executable Spring Boot JAR file from the build stage
COPY --from=build /workspace/dashboard-service/target/dashboard-service-*.jar dashboard-service.jar

# Expose port 8083
EXPOSE 8083

# Set the entrypoint command for running the application
ENTRYPOINT ["java", "--enable-preview", "-jar", "dashboard-service.jar"]