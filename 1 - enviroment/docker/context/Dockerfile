FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY domain/pom.xml domain/
COPY application/pom.xml application/
COPY infrastructure/pom.xml infrastructure/
COPY web/pom.xml web/
COPY boot/pom.xml boot/

RUN mvn dependency:go-offline -B

COPY domain/src domain/src
COPY application/src application/src
COPY infrastructure/src infrastructure/src
COPY web/src web/src
COPY boot/src boot/src

RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/boot/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]