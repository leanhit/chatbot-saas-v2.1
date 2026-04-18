#!/bin/bash

echo "=== Performance Optimization Script for Chatbot SaaS v2.1 ==="
echo "Applying database, caching, and application optimizations"
echo ""

# Configuration
POSTGRES_HOST="localhost"
POSTGRES_PORT="5432"
POSTGRES_USER="traloitudong_user"
REDIS_HOST="localhost"
REDIS_PORT="6380"

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

# ========================================
# 1. Database Optimization
# ========================================

print_status "Optimizing PostgreSQL databases"

# Check if PostgreSQL is running
if ! pg_isready -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER; then
    print_error "PostgreSQL is not running or not accessible"
    exit 1
fi

# Apply optimization to all databases
databases=("traloitudong_db" "botpress_db" "odoo_db")

for db in "${databases[@]}"; do
    print_status "Optimizing database: $db"
    
    # Apply optimization script
    if PGPASSWORD="${POSTGRES_PASSWORD:-traloitudong_Admin_2025}" psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d $db -f performance/postgresql-optimization.sql; then
        print_success "Database $db optimized"
    else
        print_error "Failed to optimize database $db"
    fi
done

# ========================================
# 2. Redis Optimization
# ========================================

print_status "Optimizing Redis configuration"

# Check if Redis is running
if ! redis-cli -h $REDIS_HOST -p $REDIS_PORT ping &>/dev/null; then
    print_error "Redis is not running or not accessible"
    exit 1
fi

# Backup current Redis configuration
if [[ -f "/etc/redis/redis.conf" ]]; then
    cp /etc/redis/redis.conf /etc/redis/redis.conf.backup
    print_success "Redis configuration backed up"
fi

# Apply optimized configuration
if cp performance/redis-optimization.conf /etc/redis/redis.conf; then
    # Restart Redis
    systemctl restart redis-server
    
    # Verify Redis is running
    if redis-cli -h $REDIS_HOST -p $REDIS_PORT ping &>/dev/null; then
        print_success "Redis optimized and restarted"
    else
        print_error "Redis failed to start after optimization"
        # Restore backup
        cp /etc/redis/redis.conf.backup /etc/redis/redis.conf
        systemctl restart redis-server
    fi
else
    print_error "Failed to copy Redis configuration"
fi

# ========================================
# 3. Application Performance Tuning
# ========================================

print_status "Optimizing application performance"

# Set system limits for application
cat > /etc/security/limits.d/chatbot-saas-performance.conf << 'EOF'
# Performance limits for Chatbot SaaS v2.1
* soft nofile 1048576
* hard nofile 1048576
* soft nproc 1048576
* hard nproc 1048576
* soft memlock unlimited
* hard memlock unlimited
* soft stack unlimited
* hard stack unlimited
EOF

# Configure kernel parameters for performance
cat > /etc/sysctl.d/99-chatbot-saas-performance.conf << 'EOF'
# Performance kernel parameters for Chatbot SaaS v2.1

# Network performance
net.core.rmem_max = 134217728
net.core.wmem_max = 134217728
net.ipv4.tcp_rmem = 4096 87380 134217728
net.ipv4.tcp_wmem = 4096 65536 134217728
net.ipv4.tcp_congestion_control = bbr
net.core.netdev_max_backlog = 5000

# File system performance
fs.file-max = 2097152
fs.inotify.max_user_watches = 524288
fs.inotify.max_user_instances = 512

# Virtual memory
vm.swappiness = 10
vm.dirty_ratio = 15
vm.dirty_background_ratio = 5
vm.vfs_cache_pressure = 50

# Process scheduling
kernel.sched_migration_cost_ns = 5000000
kernel.sched_autogroup_enabled = 0
EOF

# Apply kernel parameters
sysctl -p /etc/sysctl.d/99-chatbot-saas-performance.conf

print_success "System performance limits configured"

# ========================================
# 4. Nginx Performance Optimization
# ========================================

print_status "Optimizing Nginx performance"

# Create optimized Nginx configuration
cat > /etc/nginx/conf.d/performance.conf << 'EOF'
# Performance optimization for Chatbot SaaS v2.1

# Worker processes (auto-detect CPU cores)
worker_processes auto;
worker_rlimit_nofile 1048576;

# Events configuration
events {
    worker_connections 4096;
    use epoll;
    multi_accept on;
}

# HTTP performance settings
http {
    # Enable HTTP/2
    http2 on;

    # Keep alive settings
    keepalive_timeout 65;
    keepalive_requests 1000;
    
    # TCP settings
    tcp_nopush on;
    tcp_nodelay on;
    
    # Sendfile optimization
    sendfile on;
    sendfile_max_chunk 512k;
    
    # Buffer sizes
    client_body_buffer_size 128k;
    client_max_body_size 10MB;
    client_header_buffer_size 1k;
    large_client_header_buffers 4 4k;
    client_body_timeout 12;
    client_header_timeout 12;
    send_timeout 10;
    
    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_comp_level 6;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/json
        application/javascript
        application/xml+rss
        application/atom+xml
        image/svg+xml;
    
    # Brotli compression (if available)
    brotli on;
    brotli_comp_level 6;
    brotli_types text/plain text/css application/json application/javascript text/xml application/xml+rss text/javascript;
    
    # Open file cache
    open_file_cache max=200000 inactive=20s;
    open_file_cache_valid 30s;
    open_file_cache_min_uses 2;
    open_file_cache_errors on;
    
    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=100r/m;
    limit_req_zone $binary_remote_addr zone=login:10m rate=10r/m;
    
    # Connection limits
    limit_conn_zone $binary_remote_addr zone=conn_limit_per_ip:10m;
}
EOF

# Test Nginx configuration
if nginx -t; then
    # Reload Nginx
    systemctl reload nginx
    print_success "Nginx performance optimization applied"
else
    print_error "Nginx configuration test failed"
fi

# ========================================
# 5. Container Performance Optimization
# ========================================

print_status "Optimizing Docker container performance"

# Update Docker daemon configuration
cat > /etc/docker/daemon.json << 'EOF'
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  },
  "storage-driver": "overlay2",
  "storage-opts": [
    "overlay2.override_kernel_check=true",
    "overlay2.size=20G"
  ],
  "default-ulimits": {
    "nofile": {
      "Name": "nofile",
      "Hard": 1048576,
      "Soft": 1048576
    }
  },
  "max-concurrent-downloads": 10,
  "max-concurrent-uploads": 5,
  "live-restore": true,
  "userland-proxy": false,
  "experimental": false,
  "metrics-addr": "127.0.0.1:9323",
  "exec-opts": ["native.cgroupdriver=systemd"]
}
EOF

# Restart Docker
systemctl restart docker

print_success "Docker performance optimization applied"

# ========================================
# 6. JVM Performance Tuning
# ========================================

print_status "Creating JVM performance configuration"

# Create JVM options file
cat > /opt/chatbot-saas/jvm-options << 'EOF'
# JVM Performance Options for Chatbot SaaS v2.1

# Memory settings
-Xms512m
-Xmx1g
-XX:NewRatio=1
-XX:SurvivorRatio=8
-XX:MaxTenuringThreshold=15

# Garbage Collection
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:G1NewSizePercent=30
-XX:G1MaxNewSizePercent=40
-XX:G1MixedGCCountTarget=8
-XX:InitiatingHeapOccupancyPercent=45
-XX:G1MixedGCLiveThresholdPercent=85

# Performance tuning
-XX:+UseStringDeduplication
-XX:+OptimizeStringConcat
-XX:+UseCompressedOops
-XX:+UseCompressedClassPointers
-XX:+UseBiasedLocking
-XX:+UseFastUnorderedTimeStamps

# Container support
-XX:+UseContainerSupport
-XX:MaxRAMPercentage=75.0
-XX:InitialRAMPercentage=50.0
-XX:MinRAMPercentage=25.0

# JIT compilation
-XX:+TieredCompilation
-XX:TieredStopAtLevel=4
-XX:CompileThreshold=1000
-XX:CompileCommand=exclude,java/lang/String.indexOf

# Monitoring and diagnostics
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+PrintGCDateStamps
-XX:+PrintGCApplicationStoppedTime
-XX:+UseGCLogFileRotation
-XX:NumberOfGCLogFiles=5
-XX:GCLogFileSize=10M
-Xloggc:/var/log/chatbot-saas/gc.log

# Error handling
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/log/chatbot-saas/
-XX:ErrorFile=/var/log/chatbot-saas/hs_err_pid%p.log

# Security
-Djava.security.egd=file:/dev/./urandom
-Dfile.encoding=UTF-8
-Duser.timezone=Asia/Ho_Chi_Minh

# Application specific
-Dspring.profiles.active=production
-Dlogging.file.name=/var/log/chatbot-saas/application.log
-Dmanagement.endpoints.web.exposure.include=health,info,metrics,prometheus
EOF

print_success "JVM performance configuration created"

# ========================================
# 7. Monitoring Performance Metrics
# ========================================

print_status "Setting up performance monitoring"

# Create performance monitoring script
cat > /usr/local/bin/performance-monitor.sh << 'EOF'
#!/bin/bash

# Performance Monitoring Script for Chatbot SaaS v2.1

echo "=== Performance Metrics ==="
echo "Time: $(date)"
echo ""

# System metrics
echo "=== System Metrics ==="
echo "CPU Usage: $(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1)%"
echo "Memory Usage: $(free | grep Mem | awk '{printf("%.2f%%"), $3/$2 * 100.0}')"
echo "Disk Usage: $(df -h / | awk 'NR==2 {print $5}')"
echo "Load Average: $(uptime | awk -F'load average:' '{print $2}')"
echo ""

# Database metrics
echo "=== Database Metrics ==="
if command -v psql &> /dev/null; then
    echo "PostgreSQL Connections: $(psql -t -h localhost -U traloitudong_user -d traloitudong_db -c "SELECT count(*) FROM pg_stat_activity;" 2>/dev/null | xargs)"
    echo "PostgreSQL Cache Hit Ratio: $(psql -t -h localhost -U traloitudong_user -d traloitudong_db -c "SELECT round(sum(blks_hit)*100/(sum(blks_hit)+sum(blks_read)), 2) AS cache_hit_ratio FROM pg_stat_database;" 2>/dev/null | xargs)%"
fi

# Redis metrics
echo "=== Redis Metrics ==="
if command -v redis-cli &> /dev/null; then
    echo "Redis Memory Usage: $(redis-cli -p 6380 info memory | grep used_memory_human | cut -d: -f2 | tr -d '\r')"
    echo "Redis Hit Rate: $(redis-cli -p 6380 info stats | grep keyspace_hit_rate | cut -d: -f2 | tr -d '\r')"
    echo "Redis Connections: $(redis-cli -p 6380 info clients | grep connected_clients | cut -d: -f2 | tr -d '\r')"
fi

# Application metrics
echo "=== Application Metrics ==="
if curl -s http://localhost:8080/actuator/health &>/dev/null; then
    echo "Application Status: Running"
    echo "JVM Memory: $(curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | grep 'measurements' | jq -r '.measurements[0].value' 2>/dev/null || echo "N/A")"
else
    echo "Application Status: Not responding"
fi

echo ""
echo "=== End Performance Metrics ==="
EOF

chmod +x /usr/local/bin/performance-monitor.sh

# Create cron job for performance monitoring
echo "*/5 * * * * /usr/local/bin/performance-monitor.sh >> /var/log/performance-monitor.log 2>&1" | crontab -

print_success "Performance monitoring configured"

# ========================================
# 8. Performance Testing
# ========================================

print_status "Running performance tests"

# Test database performance
echo "Testing database performance..."
PGPASSWORD="${POSTGRES_PASSWORD:-traloitudong_Admin_2025}" psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER -d traloitudong_db -c "
SELECT 
    'Database Performance Test' as test_name,
    NOW() as test_time,
    (SELECT COUNT(*) FROM pg_stat_activity) as active_connections,
    (SELECT round(sum(blks_hit)*100/(sum(blks_hit)+sum(blks_read)), 2) FROM pg_stat_database) as cache_hit_ratio;
" 2>/dev/null

# Test Redis performance
echo "Testing Redis performance..."
redis-cli -h $REDIS_HOST -p $REDIS_PORT ping
redis-cli -h $REDIS_HOST -p $REDIS_PORT info memory | grep used_memory_human

# Test application health
echo "Testing application health..."
if curl -f http://localhost:8080/actuator/health &>/dev/null; then
    echo "Application health check: PASSED"
else
    echo "Application health check: FAILED"
fi

# ========================================
# 9. Performance Baseline
# ========================================

print_status "Creating performance baseline"

# Create baseline file
cat > /var/log/performance-baseline.log << EOF
Performance Baseline - $(date)
=====================================
System:
- CPU Cores: $(nproc)
- Memory: $(free -h | grep Mem | awk '{print $2}')
- Disk: $(df -h / | awk 'NR==2 {print $2}')

Database:
- PostgreSQL Version: $(psql --version | head -n1)
- PostgreSQL Connections: $(PGPASSWORD="${POSTGRES_PASSWORD:-traloitudong_Admin_2025}" psql -t -h localhost -U traloitudong_user -d traloitudong_db -c "SELECT count(*) FROM pg_stat_activity;" 2>/dev/null | xargs)

Cache:
- Redis Version: $(redis-cli -p 6380 info server | grep redis_version | cut -d: -f2 | tr -d '\r')
- Redis Memory: $(redis-cli -p 6380 info memory | grep used_memory_human | cut -d: -f2 | tr -d '\r')

Application:
- Java Version: $(java -version 2>&1 | head -n1)
- Application Status: $(curl -s http://localhost:8080/actuator/health | jq -r '.status' 2>/dev/null || echo "Unknown")
EOF

print_success "Performance baseline created"

# ========================================
# 10. Summary and Next Steps
# ========================================

print_status "Performance Optimization Summary"
echo "=================================="
echo "1. Database Optimization:"
echo "   - PostgreSQL configuration optimized"
echo "   - Indexes created for performance"
echo "   - Materialized views created"
echo "   - Auto-vacuum tuned"
echo ""
echo "2. Cache Optimization:"
echo "   - Redis configuration optimized"
echo "   - Memory settings tuned"
echo "   - Persistence configured"
echo ""
echo "3. Application Optimization:"
echo "   - System limits increased"
echo "   - Kernel parameters tuned"
echo "   - JVM options configured"
echo ""
echo "4. Web Server Optimization:"
echo "   - Nginx performance tuned"
echo "   - Compression enabled"
echo "   - Caching configured"
echo ""
echo "5. Container Optimization:"
echo "   - Docker daemon optimized"
echo "   - Resource limits set"
echo ""
echo "6. Monitoring:"
echo "   - Performance monitoring script created"
echo "   - Baseline metrics recorded"
echo "   - Automated monitoring scheduled"
echo ""

# Display useful commands
print_status "Useful Commands"
echo "===================="
echo "View performance metrics: /usr/local/bin/performance-monitor.sh"
echo "Monitor database: psql -h localhost -U traloitudong_user -d traloitudong_db -c 'SELECT * FROM pg_stat_activity;'"
echo "Monitor Redis: redis-cli -p 6380 info"
echo "View application metrics: curl http://localhost:8080/actuator/metrics"
echo "Check GC logs: tail -f /var/log/chatbot-saas/gc.log"
echo ""

print_success "Performance optimization completed!"
echo "Your Chatbot SaaS v2.1 is now optimized for production workloads"
echo "Monitor performance regularly and adjust settings as needed"
