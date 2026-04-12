#!/bin/bash

echo "=== Monitoring & Logging Startup Script for Chatbot SaaS v2.1 ==="
echo "Starting Prometheus, Grafana, and ELK stack"
echo ""

# Configuration
MONITORING_COMPOSE="docker-compose.monitoring.yml"
LOGGING_COMPOSE="docker-compose.logging.yml"
PROJECT_NAME="chatbot-saas"

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

# Check if monitoring compose file exists
if [[ ! -f "$MONITORING_COMPOSE" ]]; then
    print_error "Monitoring compose file '$MONITORING_COMPOSE' not found"
    exit 1
fi

# Check if logging compose file exists
if [[ ! -f "$LOGGING_COMPOSE" ]]; then
    print_error "Logging compose file '$LOGGING_COMPOSE' not found"
    exit 1
fi

# Create necessary directories
print_status "Creating necessary directories"
mkdir -p monitoring/grafana/dashboards/json
mkdir -p monitoring/grafana/plugins
mkdir -p monitoring/rules
mkdir -p logging/elasticsearch
mkdir -p logging/kibana
mkdir -p logging/logstash/patterns
mkdir -p logging/filebeat/modules.d
mkdir -p logging/apm-server

# Set proper permissions
chown -R 1000:1000 monitoring/grafana
chown -R 1000:1000 logging/elasticsearch
chown -R 1000:1000 logging/kibana

# Start monitoring stack
print_status "Starting monitoring stack (Prometheus + Grafana)"
docker compose -f $MONITORING_COMPOSE -p $PROJECT_NAME-monitoring up -d

if [[ $? -ne 0 ]]; then
    print_error "Failed to start monitoring stack"
    exit 1
fi

print_success "Monitoring stack started successfully"

# Wait for monitoring services to be ready
print_status "Waiting for monitoring services to be ready"
sleep 30

# Check monitoring service health
print_status "Checking monitoring service health"

# Check Prometheus
if curl -f http://localhost:9090/-/healthy &>/dev/null; then
    print_success "Prometheus is healthy"
else
    print_warning "Prometheus may still be starting..."
fi

# Check Grafana
if curl -f http://localhost:3000/api/health &>/dev/null; then
    print_success "Grafana is healthy"
else
    print_warning "Grafana may still be starting..."
fi

# Check Node Exporter
if curl -f http://localhost:9100/metrics &>/dev/null; then
    print_success "Node Exporter is healthy"
else
    print_warning "Node Exporter may still be starting..."
fi

# Start logging stack
print_status "Starting logging stack (ELK)"
docker compose -f $LOGGING_COMPOSE -p $PROJECT_NAME-logging up -d

if [[ $? -ne 0 ]]; then
    print_error "Failed to start logging stack"
    exit 1
fi

print_success "Logging stack started successfully"

# Wait for logging services to be ready
print_status "Waiting for logging services to be ready"
sleep 60

# Check logging service health
print_status "Checking logging service health"

# Check Elasticsearch
if curl -f http://localhost:9200/_cluster/health &>/dev/null; then
    print_success "Elasticsearch is healthy"
else
    print_warning "Elasticsearch may still be starting..."
fi

# Check Kibana
if curl -f http://localhost:5601/api/status &>/dev/null; then
    print_success "Kibana is healthy"
else
    print_warning "Kibana may still be starting..."
fi

# Check Logstash
if curl -f http://localhost:9600/_node/stats &>/dev/null; then
    print_success "Logstash is healthy"
else
    print_warning "Logstash may still be starting..."
fi

# Display service URLs
print_status "Monitoring & Logging Service URLs"
echo "======================================="
echo "Prometheus: http://localhost:9090"
echo "Grafana: http://localhost:3000 (admin/Admin_2025!)"
echo "Alertmanager: http://localhost:9093"
echo "Node Exporter: http://localhost:9100"
echo "cAdvisor: http://localhost:8080"
echo ""
echo "Elasticsearch: http://localhost:9200"
echo "Kibana: http://localhost:5601"
echo "APM Server: http://localhost:8200"
echo ""

# Display useful commands
print_status "Useful Commands"
echo "===================="
echo "View monitoring logs: docker compose -f $MONITORING_COMPOSE -p $PROJECT_NAME-monitoring logs -f [service]"
echo "View logging logs: docker compose -f $LOGGING_COMPOSE -p $PROJECT_NAME-logging logs -f [service]"
echo "Stop monitoring: docker compose -f $MONITORING_COMPOSE -p $PROJECT_NAME-monitoring down"
echo "Stop logging: docker compose -f $LOGGING_COMPOSE -p $PROJECT_NAME-logging down"
echo "Check Prometheus targets: curl http://localhost:9090/api/v1/targets"
echo "Check Elasticsearch health: curl http://localhost:9200/_cluster/health"
echo ""

# Display next steps
print_status "Next Steps"
echo "============"
echo "1. Configure Grafana dashboards"
echo "2. Set up alert notification channels"
echo "3. Configure Kibana index patterns"
echo "4. Set up APM agents in application"
echo "5. Configure log shipping from application"
echo ""

# Display security notes
print_status "Security Notes"
echo "================"
echo "1. Change default Grafana password"
echo "2. Configure authentication for Kibana"
echo "3. Set up firewall rules for monitoring ports"
echo "4. Configure SSL/TLS for monitoring services"
echo "5. Set up backup for Elasticsearch data"
echo ""

print_status "Monitoring & Logging startup complete!"
echo "Your Chatbot SaaS v2.1 now has comprehensive monitoring and logging capabilities"
echo "Access Grafana at http://localhost:3000 to start creating dashboards"
