# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Stage 1 - build
# The tag pins the exact Maven distribution used by .mvn/wrapper/maven-wrapper.properties
# ---------------------------------------------------------------------------
FROM maven:3.9.16-eclipse-temurin-26 AS build

WORKDIR /build

# Resolve dependencies in a layer of their own, so editing sources does not
# re-download the (large) dependency tree on every build.
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
# spring-boot:repackage also writes app.jar.original, so exclude it when copying.
RUN mvn -B -ntp clean package -DskipTests \
    && cp "$(find target -maxdepth 1 -name '*.jar' ! -name '*.original' -print -quit)" /build/app.jar

# ---------------------------------------------------------------------------
# Stage 2 - runtime
# JRE only: the JDK is ~300 MB larger and is not needed to run the jar.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:26-jre AS runtime

# Without tzdata the TZ variable below would be a silent no-op.
RUN apt-get update \
    && apt-get install -y --no-install-recommends tzdata \
    && rm -rf /var/lib/apt/lists/*

ENV TZ=Asia/Tehran

# Run as an unprivileged user: the app only needs to read its own jar.
RUN groupadd --system spring \
    && useradd --system --gid spring --home-dir /app --shell /usr/sbin/nologin spring

WORKDIR /app
COPY --from=build --chown=spring:spring /build/app.jar ./app.jar

# MaxRAMPercentage makes the heap follow the container memory limit instead of
# the host's RAM, which is the usual cause of OOMKilled Spring Boot containers.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"

EXPOSE 8080

USER spring:spring

# `exec` replaces the shell so the JVM becomes PID 1 and receives SIGTERM,
# which lets Spring Boot run its shutdown hook and stop gracefully.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]