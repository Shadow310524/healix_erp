FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY healix-core healix-core
COPY healix-security healix-security
COPY healix-persistence healix-persistence
COPY healix-rules healix-rules
COPY healix-workflows healix-workflows
COPY healix-notifications healix-notifications
COPY healix-modules healix-modules
COPY healix-app healix-app
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/healix-app/target/healix-app-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
