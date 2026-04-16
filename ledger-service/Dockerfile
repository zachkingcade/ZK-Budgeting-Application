# syntax=docker/dockerfile:1

FROM maven:3.9.14-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom first so dependency download can be cached
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]