# Multi-Stage Dockerfile for Chatbot SaaS All-In-One Container (Frontend + Backend + Nginx)

# Stage 1: Build Frontend (Vue 3)
FROM node:18-alpine AS frontend-build
WORKDIR /build/frontend
COPY frontend/package*.json ./
RUN npm ci --legacy-peer-deps || npm install --legacy-peer-deps
COPY frontend/ ./
RUN npm run build

# Stage 2: Build Backend (Spring Boot 3)
FROM eclipse-temurin:21-jdk AS backend-build
WORKDIR /build/backend
COPY backend/build.gradle backend/settings.gradle backend/gradlew ./
COPY backend/gradle ./gradle
COPY backend/src ./src
RUN ./gradlew bootJar -x test --no-daemon

# Stage 3: Final Runtime Image
FROM eclipse-temurin:21-jdk

# Install Nginx, Supervisord and utilities
RUN apt-get update && apt-get install -y \
    nginx \
    supervisor \
    curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Create required directories
RUN mkdir -p /app/config /app/logs /app/uploads /var/log/supervisor

# Copy Frontend static build to Nginx root
COPY --from=frontend-build /build/frontend/dist /usr/share/nginx/html

# Copy Backend JAR
COPY --from=backend-build /build/backend/build/libs/*-0.0.1-SNAPSHOT.jar /app/app.jar

# Copy Configuration files
COPY app-deploy/nginx/nginx.conf /etc/nginx/conf.d/default.conf
COPY app-deploy/supervisor/supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY app-deploy/config/application-docker.yml /app/config/application-docker.yml

EXPOSE 80 443

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost/health || exit 1

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
