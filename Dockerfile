FROM --platform=linux/arm64 eclipse-temurin:25-jre-alpine
LABEL authors="Ben Shabowski"

ARG SPRING_PROFILES_ACTIVE
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

WORKDIR /app
COPY /target/Wraith-1.0.0.jar /app/Wraith-1.0.0.jar

EXPOSE 2002

CMD ["java", "-jar", "Wraith-1.0.0.jar"]
