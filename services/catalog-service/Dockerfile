# Multi-stage build for Catalog Service (SOAP)
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
COPY --from=build /app/target/*.war app.war

# Expose port
EXPOSE 8080

# Health check (checks the WSDL endpoint)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/ws/catalog.wsdl || exit 1

# Run the application
CMD ["java", "-jar", "app.war"]
