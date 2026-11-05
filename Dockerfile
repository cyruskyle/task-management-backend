# syntax=docker/dockerfile:1
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src/ src/
RUN mvn -B -ntp -DskipTests clean package

FROM eclipse-temurin:21-jre
RUN groupadd --system --gid 10001 spring \
	&& useradd --system --uid 10001 --gid 10001 --no-create-home spring
WORKDIR /app

COPY --from=build --chown=10001:10001 /workspace/target/task-management-backend-0.0.1-SNAPSHOT.jar /app/app.jar
USER 10001:10001

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
