# ── Build Stage ──────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-noble AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .
COPY query-service/build.gradle query-service/build.gradle
COPY runtime-bundle/build.gradle runtime-bundle/build.gradle
COPY connector-service/build.gradle connector-service/build.gradle
COPY modeler-backend/build.gradle modeler-backend/build.gradle
COPY connector-service/src connector-service/src

RUN chmod +x gradlew && \
    GRADLE_OPTS="-Dorg.gradle.java.installations.auto-detect=true -Dorg.gradle.java.installations.auto-download=false" \
    ./gradlew :connector-service:bootJar -x test --no-daemon

# ── Run Stage ─────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-noble
VOLUME /tmp
COPY --from=builder /app/connector-service/build/libs/connector-service*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]