FROM ubuntu:latest
LABEL authors="Ben Shabowski"

# Use an appropriate base image
FROM amazoncorretto:21

# Set the working directory inside the container
WORKDIR /app
#CMD ["echo", "%cd%"]
# Copy the compiled JAR file into the container
COPY /target/Wraith-1.0.0.jar /app/Wraith-1.0.0.jar

# Expose the port your Spring application runs on
EXPOSE 8080

# Command to run your Spring application when the container starts
CMD ["java", "-jar", "-Dspring.profiles.active=cert", "Wraith-1.0.0.jar"]
