#!/bin/bash

# ========================================
# CHATBOT SaaS v2.1 - Port Status Checker
# ========================================

echo "🔍 Chatbot SaaS v2.1 - Port Status Check"
echo "====================================="
echo ""

# Define colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check port
check_port() {
    local port=$1
    local service=$2
    local expected_status=$3
    
    if netstat -tulpn 2>/dev/null | grep -q ":$port "; then
        echo -e "${GREEN}✅ $service (Port $port): RUNNING${NC}"
        return 0
    else
        echo -e "${RED}❌ $service (Port $port): NOT RUNNING${NC}"
        return 1
    fi
}

# Function to check process
check_process() {
    local process=$1
    local service=$2
    
    if pgrep -f "$process" > /dev/null; then
        echo -e "${GREEN}✅ $service Process: RUNNING${NC}"
    else
        echo -e "${RED}❌ $service Process: NOT RUNNING${NC}"
    fi
}

echo "🏗️  gRPC Hub Services"
echo "-------------------"
check_port 50051 "Identity Hub"
check_port 50052 "App Hub"
check_port 50053 "Tenant Hub"
check_port 50054 "User Hub"

echo ""
echo "🌐 Web Services"
echo "---------------"
check_port 8080 "Main Application"
check_port 3001 "Botpress"
check_port 5005 "Rasa"
check_port 3005 "Odoo"
check_port 9000 "MinIO"

echo ""
echo "🗄️  Database Services"
echo "-------------------"
check_port 5432 "PostgreSQL"
check_port 6379 "Redis"

echo ""
echo "🔧 Process Status"
echo "-----------------"
check_process "java.*ChatbotApplication" "Spring Boot App"
check_process "botpress" "Botpress"
check_process "rasa" "Rasa"
check_process "odoo" "Odoo"
check_process "minio" "MinIO"
check_process "postgres" "PostgreSQL"
check_process "redis" "Redis"

echo ""
echo "📊 Port Usage Summary"
echo "===================="
echo "gRPC Hubs: 50051-50054 (Identity, App, Tenant, User)"
echo "Web APIs: 8080 (Main), 3001 (Botpress), 5005 (Rasa), 3005 (Odoo), 9000 (MinIO)"
echo "Databases: 5432 (PostgreSQL), 6379 (Redis)"

echo ""
echo "🔍 Detailed Port Information"
echo "=========================="
netstat -tulpn 2>/dev/null | grep -E "(5005[0-9]|8080|3001|5005|3005|9000|5432|6379)" | sort -k4

echo ""
echo "🚀 Quick Actions"
echo "==============="
echo "Start application: ./gradlew bootRun"
echo "Check logs: tail -f logs/app.log"
echo "Kill all Java: pkill -f java"
echo "Check specific port: netstat -tulpn | grep :PORT"

echo ""
echo "📝 Port Configuration Files"
echo "==========================="
echo "📄 GRPC_PORT_CONFIGURATION.md - Detailed port documentation"
echo "📄 PORT_DEFINITIONS.env - Environment variables"
echo "📄 application-*.properties - Spring configuration files"

echo ""
echo "⏰ Check completed at: $(date)"
echo "====================================="
