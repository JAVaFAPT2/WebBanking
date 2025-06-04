FROM openjdk:17-jdk-slim as builder
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./ 

# Download dependencies
RUN ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw clean install -DskipTests

FROM openjdk:17-jre-slim
WORKDIR /app

# Define an argument for the JAR file name, defaulting to target/*.jar
ARG JAR_FILE=target/orchestration-service-0.0.1-SNAPSHOT.jar

COPY --from=builder /app/${JAR_FILE} application.jar

# Expose the port the application will run on (defined in application.yml or default 8080)
EXPOSE 8090

# Add user and group for security
RUN groupadd -r orchestrator && useradd -r -g orchestrator orchestrator
RUN chown -R orchestrator:orchestrator /app
USER orchestrator

ENTRYPOINT ["java","-jar","/app/application.jar"] 