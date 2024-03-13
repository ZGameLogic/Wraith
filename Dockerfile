FROM ubuntu:latest
LABEL authors="Ben Shabowski"

FROM arm64v8/openjdk:21-jdk-oracle

WORKDIR /app
COPY /target/Wraith-1.0.0.jar /app/Wraith-1.0.0.jar

EXPOSE 2002

CMD ["java", "-jar", "-Dspring.profiles.active=cluster", "Wraith-1.0.0.jar"]
