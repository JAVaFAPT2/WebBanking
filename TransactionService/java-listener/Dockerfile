# Stage 1: Build the application
FROM openjdk:17-jdk-slim as builder
WORKDIR /app

# Assuming this Dockerfile is in TransactionService/java-listener/
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw clean install -DskipTests

# Stage 2: Create the runtime image
FROM openjdk:17-jre-slim
WORKDIR /app

ARG JAR_FILE=target/transaction-service-listener-0.0.1-SNAPSHOT.jar
COPY --from=builder /app/${JAR_FILE} application.jar

EXPOSE 8082 # From application.yml

RUN groupadd -r transactionlistener && useradd -r -g transactionlistener transactionlistener
RUN chown -R transactionlistener:transactionlistener /app
USER transactionlistener

ENTRYPOINT ["java","-jar","/app/application.jar"] 