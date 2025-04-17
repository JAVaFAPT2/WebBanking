FROM bellsoft/liberica-openjdk-alpine:21 as build
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM bellsoft/liberica-runtime-container:jre-21-slim-musl
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","service.caching.CachingServiceApplication"]

# Expose the port the app runs on
EXPOSE 8083

# Add health check
HEALTHCHECK --interval=30s --timeout=3s CMD wget -q --spider http://localhost:8083/actuator/health || exit 1
