#!/bin/bash

# Chatbot SaaS v2.1 Services Management Script
# Usage: ./start-services.sh [start|stop|restart|status]

set -e

COMPOSE_FILE="docker-compose.yml"
PROJECT_NAME="chatbot_saas"

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
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed or not in PATH"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        print_error "Docker Compose is not installed"
        exit 1
    fi
}

start_services() {
    print_status "Starting Chatbot SaaS Services"
    
    # Check if containers already exist
    if docker ps -a --format "table {{.Names}}" | grep -q "chatbot_saas"; then
        print_warning "Existing containers found. Removing old containers first..."
        docker compose down -v 2>/dev/null || docker-compose down -v
    fi
    
    # Start services
    echo "Starting services with Docker Compose..."
    if docker compose up -d; then
        print_success "All services started successfully!"
        echo ""
        show_service_info
    else
        print_error "Failed to start services"
        exit 1
    fi
}

stop_services() {
    print_status "Stopping Chatbot SaaS Services"
    
    echo "Stopping and removing containers..."
    if docker compose down -v; then
        print_success "All services stopped successfully!"
    else
        print_error "Failed to stop services"
        exit 1
    fi
}

restart_services() {
    print_status "Restarting Chatbot SaaS Services"
    stop_services
    sleep 2
    start_services
}

show_status() {
    print_status "Chatbot SaaS Services Status"
    echo ""
    
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(NAMES|chatbot_saas)" || echo "No running containers found"
    echo ""
    
    # Check service health
    echo "Service Health Checks:"
    echo "------------------------"
    
    # PostgreSQL
    if nc -z localhost 5432 2>/dev/null; then
        echo -e "PostgreSQL (5432): ${GREEN}✅ Running${NC}"
    else
        echo -e "PostgreSQL (5432): ${RED}❌ Stopped${NC}"
    fi
    
    # Redis
    if nc -z localhost 6380 2>/dev/null; then
        echo -e "Redis (6380): ${GREEN}✅ Running${NC}"
    else
        echo -e "Redis (6380): ${RED}❌ Stopped${NC}"
    fi
    
    # MinIO
    if nc -z localhost 9000 2>/dev/null; then
        echo -e "MinIO (9000): ${GREEN}✅ Running${NC}"
    else
        echo -e "MinIO (9000): ${RED}❌ Stopped${NC}"
    fi
    
    # RabbitMQ
    if nc -z localhost 5672 2>/dev/null; then
        echo -e "RabbitMQ (5672): ${GREEN}✅ Running${NC}"
    else
        echo -e "RabbitMQ (5672): ${RED}❌ Stopped${NC}"
    fi
    
    # Odoo
    if nc -z localhost 3005 2>/dev/null; then
        echo -e "Odoo (3005): ${GREEN}✅ Running${NC}"
    else
        echo -e "Odoo (3005): ${RED}❌ Stopped${NC}"
    fi
}

show_service_info() {
    echo "🌐 Service URLs:"
    echo "=================="
    echo "📊 MinIO Console: http://localhost:9090 (minioadmin/minioadmin)"
    echo "📁 MinIO API: http://localhost:9000"
    echo "💼 Odoo: http://localhost:3005"
    echo "🐰 RabbitMQ Management: http://localhost:15672 (admin/admin123)"
    echo "🗄️  PostgreSQL: localhost:5432"
    echo "🔴 Redis: localhost:6380"
    echo ""
    echo "📋 Quick Commands:"
    echo "=================="
    echo "Redis CLI: redis-cli -p 6380"
    echo "PostgreSQL: psql -h localhost -p 5432 -U traloitudong_user -d traloitudong_db"
    echo ""
}

# Main script logic
case "${1:-start}" in
    start)
        check_docker
        start_services
        ;;
    stop)
        check_docker
        stop_services
        ;;
    restart)
        check_docker
        restart_services
        ;;
    status)
        check_docker
        show_status
        ;;
    info)
        show_service_info
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status|info}"
        echo ""
        echo "Commands:"
        echo "  start   - Start all services"
        echo "  stop    - Stop all services"
        echo "  restart - Restart all services"
        echo "  status  - Show service status"
        echo "  info    - Show service URLs and commands"
        exit 1
        ;;
esac
