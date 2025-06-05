FROM openjdk:21-jdk-slim as builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# Consider RUN ./mvnw dependency:go-offline if you want to cache dependencies
COPY src ./src
RUN ls -l ./src/main/resources
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --from=builder /app/${JAR_FILE} application.jar
EXPOSE 8081

# Add user and group for security
RUN groupadd -r monitor && useradd -r -g monitor monitor
RUN chown -R monitor:monitor /app
USER monitor

ENTRYPOINT ["java","-Dspring.kafka.bootstrap-servers=kafka:9092","-jar","/app/application.jar"]
