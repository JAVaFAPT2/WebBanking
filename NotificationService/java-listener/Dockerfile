# Stage 1: Build the application
FROM openjdk:17-jdk-slim as builder
WORKDIR /app

# Assuming this Dockerfile is in NotificationService/java-listener/
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw clean install -DskipTests

# Stage 2: Create the runtime image
FROM openjdk:17-jre-slim
WORKDIR /app

ARG JAR_FILE=target/notification-service-listener-0.0.1-SNAPSHOT.jar
COPY --from=builder /app/${JAR_FILE} application.jar

EXPOSE 8083 # From application.yml

RUN groupadd -r notificationlistener && useradd -r -g notificationlistener notificationlistener
RUN chown -R notificationlistener:notificationlistener /app
USER notificationlistener

ENTRYPOINT ["java","-jar","/app/application.jar"] 