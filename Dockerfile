# Stage 1: Build runtime image using lightweight OpenJDK 21 headless
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the compiled jar from your local target directory
COPY target/analytics-engine-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Run with constrained JVM heap memory to protect the 1GB RAM server limit
ENTRYPOINT ["java", "-Xmx384m", "-Xms128m", "-jar", "app.jar"]