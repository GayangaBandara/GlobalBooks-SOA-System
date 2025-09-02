FROM openjdk:17-jdk-slim

WORKDIR /app

COPY pom.xml .
COPY src ./src

# Build the application
RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests && \
    apt-get remove -y maven && apt-get autoremove -y && apt-get clean

EXPOSE 8085

CMD ["java", "-jar", "target/camel-orchestration-service-1.0.0.jar"]