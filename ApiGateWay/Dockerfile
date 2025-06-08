FROM openjdk:21-jdk-slim as builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean install -DskipTests -Dmaven.compiler.release=21

FROM openjdk:21-jdk-slim
WORKDIR /app
RUN apt-get update && apt-get install -y curl net-tools procps && rm -rf /var/lib/apt/lists/*
ARG JAR_FILE=target/*.jar
COPY --from=builder /app/${JAR_FILE} application.jar
ENV SERVER_PORT=8081
EXPOSE 8081
RUN addgroup --system spring && adduser --system --ingroup spring spring
RUN chown -R spring:spring /app
USER spring
ENTRYPOINT ["java", "-jar", "/app/application.jar", "--server.port=8081"] 