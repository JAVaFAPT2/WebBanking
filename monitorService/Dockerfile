FROM openjdk:21-jdk-slim as builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# Consider RUN ./mvnw dependency:go-offline if you want to cache dependencies
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM openjdk:21-jre-slim
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --from=builder /app/${JAR_FILE} application.jar
EXPOSE 8081

# Add user and group for security
RUN groupadd -r monitor && useradd -r -g monitor monitor
RUN chown -R monitor:monitor /app
USER monitor

ENTRYPOINT ["java","-jar","/app/application.jar"]
