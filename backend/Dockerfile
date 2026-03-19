# MIRROR 留空时直连 Docker Hub；镜像站可用时设置 MIRROR
ARG MIRROR=
FROM ${MIRROR}eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN apk add --no-cache maven && mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

ARG MIRROR=
FROM ${MIRROR}eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 20118
ENTRYPOINT ["java", "-jar", "-Dserver.port=20118", "app.jar"]
