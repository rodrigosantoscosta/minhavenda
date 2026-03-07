# Multi-stage build: build the fat JAR with Maven, then run in a slim JRE image
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy Maven metadata first — dependencies are cached unless pom.xml changes
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN mvn -B dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Install curl for healthcheck and create a non-root user
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system appgroup \
    && useradd --system --gid appgroup appuser

COPY --from=build /workspace/target/*.jar /app/minhavenda.jar
RUN chown appuser:appgroup /app/minhavenda.jar

USER appuser

# Override at runtime via docker-compose environment section
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseStringDeduplication"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -fsS http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/minhavenda.jar"]
