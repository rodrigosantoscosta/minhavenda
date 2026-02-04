# Multi-stage build: build the fat JAR with Maven, then run in a slim JRE image
FROM maven:3.9.4-eclipse-temurin-17 as build
WORKDIR /workspace

# Copy Maven metadata first for caching
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Copy source and build
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# JAR produced by the build stage
ARG JAR_FILE=target/minhavenda-1.0.0.jar
COPY --from=build /workspace/target/*.jar /app/minhavenda.jar

# Default JVM options can be overridden at runtime by setting JAVA_TOOL_OPTIONS
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -XX:+UseStringDeduplication -Dspring.profiles.active=prod"

EXPOSE 8080

# Healthcheck against actuator (ensure actuator health endpoint is enabled in prod)
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -fsS http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTIONS -jar /app/minhavenda.jar"]