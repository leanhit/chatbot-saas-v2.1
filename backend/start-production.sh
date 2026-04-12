#!/bin/bash

echo "=== Production Startup Script for Chatbot SaaS v2.1 ==="
echo "Starting production services with Docker Compose"
echo ""

# Configuration
COMPOSE_FILE="docker-compose.production.yml"
PROJECT_NAME="chatbot-saas"
ENV_FILE=".env.production"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}=== $1 ===${NC}"
}

print_success() {
    echo -e "${GREEN}SUCCESS: $1${NC}"
}

print_error() {
    echo -e "${RED}ERROR: $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}WARNING: $1${NC}"
}

# Check if running as root
if [[ $EUID -ne 0 ]]; then
   print_error "This script must be run as root (use sudo)"
   exit 1
fi

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed"
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    print_error "Docker Compose is not installed"
    exit 1
fi

# Check if production environment file exists
if [[ ! -f "$ENV_FILE" ]]; then
    print_error "Production environment file '$ENV_FILE' not found"
    echo "Please create it from .env.production.template and configure with real values"
    exit 1
fi

# Check if production compose file exists
if [[ ! -f "$COMPOSE_FILE" ]]; then
    print_error "Production compose file '$COMPOSE_FILE' not found"
    exit 1
fi

# Check environment variables
print_status "Checking environment configuration"

# Check for placeholder values
if grep -q "yourdomain.com" "$ENV_FILE"; then
    print_error "Please replace 'yourdomain.com' with your actual domain in $ENV_FILE"
    exit 1
fi

if grep -q "change_me" "$ENV_FILE"; then
    print_error "Please replace all 'change_me' values with actual secrets in $ENV_FILE"
    exit 1
fi

print_success "Environment configuration validated"

# Stop existing services if running
print_status "Stopping existing services"
docker compose -f $COMPOSE_FILE -p $PROJECT_NAME down 2>/dev/null || true

# Clean up old containers
print_status "Cleaning up old containers"
docker system prune -f --volumes

# Build production images
print_status "Building production images"
docker compose -f $COMPOSE_FILE -p $PROJECT_NAME build --no-cache

if [[ $? -ne 0 ]]; then
    print_error "Failed to build production images"
    exit 1
fi

print_success "Production images built successfully"

# Start services
print_status "Starting production services"
docker compose -f $COMPOSE_FILE -p $PROJECT_NAME up -d

if [[ $? -ne 0 ]]; then
    print_error "Failed to start production services"
    exit 1
fi

print_success "Production services started successfully"

# Wait for services to be ready
print_status "Waiting for services to be ready"
sleep 30

# Check service health
print_status "Checking service health"

# Check backend health
if curl -f http://localhost:8080/actuator/health &>/dev/null; then
    print_success "Backend service is healthy"
else
    print_warning "Backend service may still be starting..."
fi

# Check database health
if docker exec chatbot_saas_postgres pg_isready -U traloitudong_user -d traloitudong_db &>/dev/null; then
    print_success "PostgreSQL database is healthy"
else
    print_warning "PostgreSQL database may still be starting..."
fi

# Check Redis health
if docker exec chatbot_saas_redis redis-cli ping &>/dev/null; then
    print_success "Redis cache is healthy"
else
    print_warning "Redis cache may still be starting..."
fi

# Check MinIO health
if curl -f http://localhost:9000/minio/health/live &>/dev/null; then
    print_success "MinIO storage is healthy"
else
    print_warning "MinIO storage may still be starting..."
fi

# Check RabbitMQ health
if docker exec chatbot_saas_rabbitmq rabbitmq-diagnostics ping &>/dev/null; then
    print_success "RabbitMQ message queue is healthy"
else
    print_warning "RabbitMQ message queue may still be starting..."
fi

# Display service URLs
print_status "Production Service URLs"
echo "==============================="
echo "Backend API: http://localhost:8080"
echo "MinIO Console: http://localhost:9090"
echo "MinIO API: http://localhost:9000"
echo "Botpress: http://localhost:3001"
echo "Odoo: http://localhost:3005"
echo "RabbitMQ Management: http://localhost:15672"
echo ""

# Display useful commands
print_status "Useful Commands"
echo "===================="
echo "View logs: docker compose -f $COMPOSE_FILE -p $PROJECT_NAME logs -f [service]"
echo "Stop services: docker compose -f $COMPOSE_FILE -p $PROJECT_NAME down"
echo "Restart services: docker compose -f $COMPOSE_FILE -p $PROJECT_NAME restart"
echo "Check status: docker compose -f $COMPOSE_FILE -p $PROJECT_NAME ps"
echo ""

# Display next steps
print_status "Next Steps"
echo "============"
echo "1. Configure Nginx reverse proxy with SSL certificates"
echo "2. Update DNS records to point to this server"
echo "3. Test all API endpoints"
echo "4. Set up monitoring and alerting"
echo "5. Configure backup strategy"
echo ""

print_status "Production startup complete!"
echo "Your Chatbot SaaS v2.1 is now running in production mode"
echo "Make sure to configure SSL certificates and DNS settings"
