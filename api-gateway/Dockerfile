# Stage 1: Build stage
FROM maven:3.9.11-amazoncorretto-25 AS build

# Set working directory
WORKDIR /workspace

# Copy full multi-module project
COPY . .

# Build only api-gateway and required modules
RUN mvn -pl api-gateway -am clean package -DskipTests

# Stage 2: Run stage
FROM amazoncorretto:25

# Set working directory
WORKDIR /app

# Copy the executable Spring Boot JAR file from the build stage
COPY --from=build /workspace/api-gateway/target/api-gateway-*.jar api-gateway.jar

# Expose port 8080
EXPOSE 8080

# Set the entrypoint command for running the application
ENTRYPOINT ["java", "--enable-preview", "-jar", "api-gateway.jar"]