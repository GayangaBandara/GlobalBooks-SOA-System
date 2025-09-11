# Multi-stage build for Catalog Service (SOAP)
FROM maven:3.8.6-openjdk-11-slim AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:11-jre-slim

WORKDIR /app
COPY --from=build /app/target/*.war app.war

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/soap/catalog?wsdl || exit 1

# Run the application
CMD ["java", "-jar", "app.war"]