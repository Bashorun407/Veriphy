# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .

# Build the JAR and normalize its name
RUN mvn clean package -DskipTests && \
    mv target/*.jar target/app.jar

# Run stage
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy built JAR
COPY --from=build /app/target/app.jar app.jar

# Expose port (good practice)
EXPOSE 8080

# Start the app
ENTRYPOINT ["java","-jar","/app/app.jar"]