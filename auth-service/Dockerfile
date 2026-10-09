# Stage 1: Build stage
FROM maven:3.9.11-amazoncorretto-25 AS build

# Set working directory
WORKDIR /workspace

# Copy full multi-module project
COPY . .

# Build only auth-service and required modules
RUN mvn -pl auth-service -am clean package -DskipTests

# Stage 2: Run stage
FROM amazoncorretto:25

# Set working directory
WORKDIR /app

# Copy the executable Spring Boot JAR file from the build stage
COPY --from=build /workspace/auth-service/target/auth-service-*.jar auth-service.jar

# Expose port 8081
EXPOSE 8081

# Set the entrypoint command for running the application
ENTRYPOINT ["java", "--enable-preview", "-jar", "auth-service.jar"]