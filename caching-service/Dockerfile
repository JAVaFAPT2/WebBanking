FROM bellsoft/liberica-openjdk-alpine:21 as build
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw install -DskipTests

FROM bellsoft/liberica-runtime-container:jre-21-slim-musl
VOLUME /tmp
COPY --from=build /workspace/app/target/caching-service-0.0.1-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]

# Expose the port the app runs on
EXPOSE 8083

# Add health check
HEALTHCHECK --interval=30s --timeout=3s CMD wget -q --spider http://localhost:8083/actuator/health || exit 1
