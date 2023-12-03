# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21

WORKDIR /app

COPY pom.xml .
COPY src src

RUN mvn -DskipTests package

CMD java -jar target/*.jar
