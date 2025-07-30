# Use OpenJDK 24 as base
FROM eclipse-temurin:24-jdk

# Set working directory
WORKDIR /app

# Copy jar to the container
COPY target/*.jar app.jar

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
