# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy the built jar with a fixed name
COPY --from=build /app/target/*jar /app/app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]