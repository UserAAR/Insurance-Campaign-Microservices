# Stage 1: Build stage
FROM maven:3.9.11-amazoncorretto-25 AS build

# Set working directory
WORKDIR /workspace

# Copy full multi-module project
COPY . .

# Build only discovery-server and required modules
RUN mvn -pl discovery-server -am clean package -DskipTests

# Stage 2: Run stage
FROM amazoncorretto:25

# Set working directory
WORKDIR /app

# Copy the executable Spring Boot JAR file from the build stage
COPY --from=build /workspace/discovery-server/target/discovery-server-*.jar discovery-server.jar

# Expose port 8761
EXPOSE 8761

# Set the entrypoint command for running the application
ENTRYPOINT ["java", "--enable-preview", "-jar", "discovery-server.jar"]