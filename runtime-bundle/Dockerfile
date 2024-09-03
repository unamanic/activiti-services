# Start with a base image containing Java runtime
FROM openjdk:21-jdk-slim

# Install fonts
RUN apt-get update && apt-get install -y fontconfig libfreetype6 && rm -rf /var/lib/apt/lists/*


# Add a volume pointing to /tmp
VOLUME /tmp

# The application's jar file
ARG JAR_FILE=build/libs/runtime-bundle*.jar

# Add the application's jar to the container
COPY ${JAR_FILE} app.jar

EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java","-jar","/app.jar"]