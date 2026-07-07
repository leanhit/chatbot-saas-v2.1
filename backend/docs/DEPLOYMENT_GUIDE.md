# Deployment Guide

This guide outlines the steps to build and deploy the Chatbot SaaS v2.1 Backend.

## Prerequisites

Ensure the deployment environment has the following installed:
- **Java 21 (JDK)**
- **PostgreSQL 15+**
- **Redis 7+**
- **Docker & Docker Compose** (for infrastructure)

## 1. Infrastructure Setup

Create a `.env` file for the infrastructure:
```env
POSTGRES_USER=chatbot
POSTGRES_PASSWORD=secret
POSTGRES_DB=chatbot_db
```

Use the provided `docker-compose.yml` to spin up dependencies:
```bash
docker-compose up -d postgres redis
```
Wait for the containers to be fully healthy.

## 2. Configuration

Create an `application-prod.properties` (or `application-prod.yml`) in `backend/src/main/resources/`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/chatbot_db
spring.datasource.username=chatbot
spring.datasource.password=secret

spring.data.redis.host=localhost
spring.data.redis.port=6379

jwt.secret=YOUR_STRONG_SECRET_KEY_HERE
jwt.expirationMs=86400000

# Tracing
management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans
management.tracing.sampling.probability=0.1
```

## 3. Build & Package

Run the Gradle build command to produce the executable JAR:
```bash
cd backend
./gradlew clean bootJar
```
The compiled JAR will be located in `backend/build/libs/windzo-0.0.1-SNAPSHOT.jar`.

## 4. Database Migration

The application uses **Flyway** for automated database migrations. When you start the Spring Boot application, Flyway will automatically execute any pending SQL scripts located in `src/main/resources/db/migration`.

## 5. Running the Application

Execute the JAR file, specifying the active profile:
```bash
java -jar build/libs/windzo-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 6. Verification
- **Actuator Health:** `GET http://localhost:8080/actuator/health`
- **Swagger UI:** `GET http://localhost:8080/swagger-ui/index.html`
