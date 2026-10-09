# Stage 1: Build stage
FROM maven:3.9.11-amazoncorretto-25 AS build

# Set working directory
WORKDIR /workspace

# Copy full multi-module project
COPY . .

# Build only config-server and required modules
RUN mvn -pl config-server -am clean package -DskipTests

# Stage 2: Run stage
FROM amazoncorretto:25

# Set working directory
WORKDIR /app

# Copy the executable Spring Boot JAR file from the build stage
COPY --from=build /workspace/config-server/target/config-server-*.jar config-server.jar

# Expose port 8888
EXPOSE 8888

# Set the entrypoint command for running the application
ENTRYPOINT ["java", "--enable-preview", "-jar", "config-server.jar"]