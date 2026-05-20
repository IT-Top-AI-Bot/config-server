# syntax=docker/dockerfile:1.7

############################
# Stage: extract layers from pre-built JAR
############################
FROM eclipse-temurin:25-jdk-alpine AS extractor
WORKDIR /application

COPY build/libs/*.jar application.jar

RUN java -Djarmode=tools -jar application.jar extract --layers --launcher --destination extracted

############################
# Stage: final runtime image
############################
FROM eclipse-temurin:25-jdk-alpine AS optimizer
WORKDIR /application

COPY --from=extractor /application/extracted/dependencies/ ./
COPY --from=extractor /application/extracted/spring-boot-loader/ ./
COPY --from=extractor /application/extracted/snapshot-dependencies/ ./
COPY --from=extractor /application/extracted/application/ ./

RUN CONFIG_SERVER_USERNAME=dummy \
    CONFIG_SERVER_PASSWORD=dummy \
    OTEL_EXPORTER_OTLP_ENDPOINT=http://dummy:4317 \
    java -Dspring.context.exit=onRefresh \
    -XX:ArchiveClassesAtExit=application.jsa \
    org.springframework.boot.loader.launch.JarLauncher

############################
# Stage: final runtime image
############################
FROM eclipse-temurin:25-jre-alpine
WORKDIR /application

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

VOLUME ["/tmp"]

COPY --from=optimizer /application/application.jsa ./
COPY --from=extractor /application/extracted/dependencies/ ./
COPY --from=extractor /application/extracted/spring-boot-loader/ ./
COPY --from=extractor /application/extracted/snapshot-dependencies/ ./
COPY --from=extractor /application/extracted/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "--sun-misc-unsafe-memory-access=allow", "-XX:SharedArchiveFile=application.jsa", "org.springframework.boot.loader.launch.JarLauncher"]
