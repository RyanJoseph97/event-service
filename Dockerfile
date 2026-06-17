# syntax=docker/dockerfile:1.2
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /build
COPY pom.xml .
RUN --mount=type=secret,id=github_token \
    mkdir -p /root/.m2 && \
    printf '<settings><servers><server><id>github</id><username>token</username><password>%s</password></server></servers></settings>\n' \
        "$(cat /run/secrets/github_token)" > /root/.m2/settings.xml && \
    mvn dependency:go-offline -q && \
    rm -f /root/.m2/settings.xml
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
