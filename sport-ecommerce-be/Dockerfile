FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# ===== RUNTIME STAGE =====
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# copy jar từ build stage
COPY --from=build /app/target/*.jar app.jar

# expose port
EXPOSE 8080

# run app
ENTRYPOINT ["java", "-jar", "app.jar"]