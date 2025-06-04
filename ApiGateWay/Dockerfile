FROM openjdk:17-jdk-slim as builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean install -DskipTests

FROM openjdk:17-jre-slim
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --from=builder /app/${JAR_FILE} application.jar
EXPOSE 8080 
# Add user and group for security
RUN groupadd -r spring && useradd -r -g spring spring
RUN chown -R spring:spring /app
USER spring
ENTRYPOINT ["java","-jar","/app/application.jar"] 