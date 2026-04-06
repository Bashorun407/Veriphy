# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .

# Build and normalize jar name
RUN mvn clean package -DskipTests && \
    mv target/*.jar target/app.jar

# Run stage
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY --from=build /app/target/app.jar app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]