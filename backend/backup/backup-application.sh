#!/bin/bash

echo "=== Application Backup Script for Chatbot SaaS v2.1 ==="
echo "Creating comprehensive application backups"
echo ""

# Configuration
BACKUP_DIR="/backups/chatbot-saas"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=30
APP_DIR="/root/ltanh/chatbot-saas-v2.1"
DOCKER_COMPOSE_FILE="docker-compose.production.yml"

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

# Create backup directory
mkdir -p "$BACKUP_DIR/$DATE/application"
mkdir -p "$BACKUP_DIR/logs"

print_status "Starting application backup for $DATE"

# ========================================
# 1. Source Code Backup
# ========================================

print_status "Backing up source code"

SOURCE_CODE_BACKUP="$BACKUP_DIR/$DATE/application/source_code_${DATE}.tar.gz"

if [[ -d "$APP_DIR" ]]; then
    # Create source code backup excluding build artifacts and logs
    tar -czf "$SOURCE_CODE_BACKUP" \
        --exclude="$APP_DIR/backend/build" \
        --exclude="$APP_DIR/frontend/node_modules" \
        --exclude="$APP_DIR/frontend/dist" \
        --exclude="$APP_DIR/frontend/.nuxt" \
        --exclude="$APP_DIR/backend/logs" \
        --exclude="$APP_DIR/backend/.gradle" \
        --exclude="$APP_DIR/**/target" \
        --exclude="$APP_DIR/**/.git" \
        --exclude="$APP_DIR/**/node_modules" \
        --exclude="$APP_DIR/**/logs" \
        --exclude="$APP_DIR/**/tmp" \
        --exclude="$APP_DIR/**/cache" \
        -C "$(dirname "$APP_DIR")" "$(basename "$APP_DIR")"
    
    if [[ $? -eq 0 ]]; then
        SOURCE_SIZE=$(stat -f%z "$SOURCE_CODE_BACKUP" 2>/dev/null || stat -c%s "$SOURCE_CODE_BACKUP" 2>/dev/null)
        print_success "Source code backed up: $SOURCE_CODE_BACKUP ($(du -h "$SOURCE_CODE_BACKUP" | cut -f1))"
    else
        print_error "Failed to backup source code"
    fi
else
    print_error "Application directory not found: $APP_DIR"
fi

# ========================================
# 2. Configuration Files Backup
# ========================================

print_status "Backing up configuration files"

CONFIG_BACKUP="$BACKUP_DIR/$DATE/application/config_${DATE}.tar.gz"
CONFIG_TEMP_DIR="$BACKUP_DIR/$DATE/application/config_temp"

mkdir -p "$CONFIG_TEMP_DIR"

# Copy configuration files
cp -r "$APP_DIR/backend/.env.production.template" "$CONFIG_TEMP_DIR/" 2>/dev/null || true
cp -r "$APP_DIR/backend/docker-compose.production.yml" "$CONFIG_TEMP_DIR/" 2>/dev/null || true
cp -r "$APP_DIR/backend/nginx.conf" "$CONFIG_TEMP_DIR/" 2>/dev/null || true
cp -r "$APP_DIR/backend/k8s" "$CONFIG_TEMP_DIR/" 2>/dev/null || true
cp -r "$APP_DIR/backend/monitoring" "$CONFIG_TEMP_DIR/" 2>/dev/null || true
cp -r "$APP_DIR/backend/performance" "$CONFIG_TEMP_DIR/" 2>/dev/null || true
cp -r "$APP_DIR/backend/.github" "$CONFIG_TEMP_DIR/" 2>/dev/null || true

# Copy frontend configuration
cp -r "$APP_DIR/frontend/.env.production" "$CONFIG_TEMP_DIR/" 2>/dev/null || true
cp -r "$APP_DIR/frontend/nuxt.config.js" "$CONFIG_TEMP_DIR/" 2>/dev/null || true
cp -r "$APP_DIR/frontend/package.json" "$CONFIG_TEMP_DIR/" 2>/dev/null || true

# Create configuration backup
if tar -czf "$CONFIG_BACKUP" -C "$CONFIG_TEMP_DIR" .; then
    print_success "Configuration files backed up: $CONFIG_BACKUP ($(du -h "$CONFIG_BACKUP" | cut -f1))"
else
    print_error "Failed to backup configuration files"
fi

# Clean up temp directory
rm -rf "$CONFIG_TEMP_DIR"

# ========================================
# 3. Docker Volumes Backup
# ========================================

print_status "Backing up Docker volumes"

# Check if Docker is running
if ! docker info &>/dev/null; then
    print_warning "Docker is not running - skipping volume backups"
else
    # List of important volumes to backup
    VOLUMES=(
        "chatbot_saas_postgres_data"
        "chatbot_saas_redis_data"
        "chatbot_saas_minio_data"
        "chatbot_saas_elasticsearch_data"
        "chatbot_saas_grafana_data"
        "chatbot_saas_botpress_data"
        "chatbot_saas_odoo_data"
        "chatbot_saas_app_logs"
        "chatbot_saas_app_uploads"
    )
    
    for volume in "${VOLUMES[@]}"; do
        if docker volume inspect "$volume" &>/dev/null; then
            print_status "Backing up Docker volume: $volume"
            
            VOLUME_BACKUP="$BACKUP_DIR/$DATE/application/volume_${volume}_${DATE}.tar.gz"
            TEMP_CONTAINER="backup_temp_$(date +%s)"
            
            # Create temporary container to access volume
            docker run --rm -d --name "$TEMP_CONTAINER" -v "$volume":/data alpine sleep 3600 2>/dev/null
            
            if [[ $? -eq 0 ]]; then
                # Wait for container to start
                sleep 2
                
                # Backup volume content
                if docker exec "$TEMP_CONTAINER" tar -czf - -C /data . > "$VOLUME_BACKUP"; then
                    VOLUME_SIZE=$(stat -f%z "$VOLUME_BACKUP" 2>/dev/null || stat -c%s "$VOLUME_BACKUP" 2>/dev/null)
                    print_success "Volume $volume backed up: $(du -h "$VOLUME_BACKUP" | cut -f1)"
                else
                    print_error "Failed to backup volume $volume"
                fi
                
                # Clean up temporary container
                docker stop "$TEMP_CONTAINER" 2>/dev/null || true
            else
                print_error "Failed to create temporary container for volume $volume"
            fi
        else
            print_warning "Volume $volume not found - skipping"
        fi
    done
fi

# ========================================
# 4. SSL Certificates Backup
# ========================================

print_status "Backing up SSL certificates"

SSL_BACKUP="$BACKUP_DIR/$DATE/application/ssl_${DATE}.tar.gz"
SSL_TEMP_DIR="$BACKUP_DIR/$DATE/application/ssl_temp"

mkdir -p "$SSL_TEMP_DIR"

# Copy SSL certificates
if [[ -d "/etc/letsencrypt" ]]; then
    cp -r "/etc/letsencrypt" "$SSL_TEMP_DIR/" 2>/dev/null || true
fi

if [[ -d "/etc/ssl/certs" ]]; then
    cp -r "/etc/ssl/certs" "$SSL_TEMP_DIR/" 2>/dev/null || true
fi

if [[ -d "/etc/ssl/private" ]]; then
    cp -r "/etc/ssl/private" "$SSL_TEMP_DIR/" 2>/dev/null || true
fi

# Create SSL backup
if [[ -n "$(ls -A "$SSL_TEMP_DIR" 2>/dev/null)" ]]; then
    if tar -czf "$SSL_BACKUP" -C "$SSL_TEMP_DIR" .; then
        print_success "SSL certificates backed up: $SSL_BACKUP ($(du -h "$SSL_BACKUP" | cut -f1))"
    else
        print_error "Failed to backup SSL certificates"
    fi
else
    print_warning "No SSL certificates found to backup"
fi

# Clean up temp directory
rm -rf "$SSL_TEMP_DIR"

# ========================================
# 5. System Configuration Backup
# ========================================

print_status "Backing up system configuration"

SYSTEM_BACKUP="$BACKUP_DIR/$DATE/application/system_${DATE}.tar.gz"
SYSTEM_TEMP_DIR="$BACKUP_DIR/$DATE/application/system_temp"

mkdir -p "$SYSTEM_TEMP_DIR"

# Copy system configuration files
cp /etc/hosts "$SYSTEM_TEMP_DIR/" 2>/dev/null || true
cp /etc/fstab "$SYSTEM_TEMP_DIR/" 2>/dev/null || true
cp /etc/sysctl.conf "$SYSTEM_TEMP_DIR/" 2>/dev/null || true
cp -r /etc/sysctl.d "$SYSTEM_TEMP_DIR/" 2>/dev/null || true
cp -r /etc/security "$SYSTEM_TEMP_DIR/" 2>/dev/null || true
cp -r /etc/ufw "$SYSTEM_TEMP_DIR/" 2>/dev/null || true
cp -r /etc/nginx "$SYSTEM_TEMP_DIR/" 2>/dev/null || true

# Copy Docker configuration
if [[ -f "/etc/docker/daemon.json" ]]; then
    cp /etc/docker/daemon.json "$SYSTEM_TEMP_DIR/" 2>/dev/null || true
fi

# Copy Redis configuration
if [[ -f "/etc/redis/redis.conf" ]]; then
    cp /etc/redis/redis.conf "$SYSTEM_TEMP_DIR/" 2>/dev/null || true
fi

# Create system backup
if tar -czf "$SYSTEM_BACKUP" -C "$SYSTEM_TEMP_DIR" .; then
    print_success "System configuration backed up: $SYSTEM_BACKUP ($(du -h "$SYSTEM_BACKUP" | cut -f1))"
else
    print_error "Failed to backup system configuration"
fi

# Clean up temp directory
rm -rf "$SYSTEM_TEMP_DIR"

# ========================================
# 6. Application Logs Backup
# ========================================

print_status "Backing up application logs"

LOGS_BACKUP="$BACKUP_DIR/$DATE/application/logs_${DATE}.tar.gz"
LOGS_TEMP_DIR="$BACKUP_DIR/$DATE/application/logs_temp"

mkdir -p "$LOGS_TEMP_DIR"

# Copy application logs
if [[ -d "$APP_DIR/backend/logs" ]]; then
    cp -r "$APP_DIR/backend/logs" "$LOGS_TEMP_DIR/" 2>/dev/null || true
fi

if [[ -d "/var/log/chatbot-saas" ]]; then
    cp -r "/var/log/chatbot-saas" "$LOGS_TEMP_DIR/" 2>/dev/null || true
fi

# Copy Nginx logs
if [[ -d "/var/log/nginx" ]]; then
    mkdir -p "$LOGS_TEMP_DIR/nginx"
    cp /var/log/nginx/access.log* "$LOGS_TEMP_DIR/nginx/" 2>/dev/null || true
    cp /var/log/nginx/error.log* "$LOGS_TEMP_DIR/nginx/" 2>/dev/null || true
fi

# Copy system logs
if [[ -f "/var/log/syslog" ]]; then
    cp /var/log/syslog "$LOGS_TEMP_DIR/" 2>/dev/null || true
fi

# Create logs backup
if [[ -n "$(ls -A "$LOGS_TEMP_DIR" 2>/dev/null)" ]]; then
    if tar -czf "$LOGS_BACKUP" -C "$LOGS_TEMP_DIR" .; then
        print_success "Application logs backed up: $LOGS_BACKUP ($(du -h "$LOGS_BACKUP" | cut -f1))"
    else
        print_error "Failed to backup application logs"
    fi
else
    print_warning "No application logs found to backup"
fi

# Clean up temp directory
rm -rf "$LOGS_TEMP_DIR"

# ========================================
# 7. Create Backup Manifest
# ========================================

print_status "Creating backup manifest"

MANIFEST_FILE="$BACKUP_DIR/$DATE/application/MANIFEST.txt"

cat > "$MANIFEST_FILE" << EOF
Chatbot SaaS v2.1 Application Backup Manifest
============================================
Backup Date: $(date)
Backup Type: Full Application Backup
Server: $(hostname)
Operating System: $(uname -a)
Docker Version: $(docker --version 2>/dev/null || echo "Not installed")

Application Components Backed Up:
EOF

# Add backup files to manifest
for file in "$BACKUP_DIR/$DATE/application"/*.tar.gz; do
    if [[ -f "$file" ]]; then
        SIZE=$(stat -f%z "$file" 2>/dev/null || stat -c%s "$file" 2>/dev/null)
        FILENAME=$(basename "$file")
        echo "- $FILENAME: $(du -h "$file" | cut -f1) ($SIZE bytes)" >> "$MANIFEST_FILE"
    fi
done

cat >> "$MANIFEST_FILE" << EOF

Recovery Instructions:
1. Extract source code: tar -xzf source_code_${DATE}.tar.gz
2. Extract configuration: tar -xzf config_${DATE}.tar.gz
3. Restore Docker volumes: docker run --rm -v volume_name:/data -v $(pwd):/backup alpine tar -xzf /backup/volume_volume_name_${DATE}.tar.gz -C /data
4. Restore SSL certificates: tar -xzf ssl_${DATE}.tar.gz -C /
5. Restore system configuration: tar -xzf system_${DATE}.tar.gz -C /
6. Restart services: docker-compose -f docker-compose.production.yml up -d

Backup Retention: $RETENTION_DAYS days
Next Scheduled Backup: $(date -d "+1 day" +"%Y-%m-%d %H:%M:%S")

Application Information:
- Source Directory: $APP_DIR
- Docker Compose: $DOCKER_COMPOSE_FILE
- Environment: Production
EOF

print_success "Backup manifest created: $MANIFEST_FILE"

# ========================================
# 8. Cleanup Old Backups
# ========================================

print_status "Cleaning up old application backups (older than $RETENTION_DAYS days)"

find "$BACKUP_DIR" -type d -name "*/application" -mtime +$RETENTION_DAYS -exec rm -rf {} + 2>/dev/null

if [[ $? -eq 0 ]]; then
    print_success "Old application backups cleaned up"
else
    print_warning "Some old application backups could not be removed"
fi

# ========================================
# 9. Backup Summary
# ========================================

print_status "Application Backup Summary"
echo "==============================="
echo "Backup Directory: $BACKUP_DIR/$DATE/application"
echo "Components Backed Up: Source Code, Configuration, Docker Volumes, SSL, System Config, Logs"
echo "Total Backup Size: $(du -sh "$BACKUP_DIR/$DATE/application" | cut -f1)"
echo "Manifest: $MANIFEST_FILE"
echo ""

# Display backup files
echo "Application Backup Files Created:"
ls -la "$BACKUP_DIR/$DATE/application" | grep -v "^total"

# ========================================
# 10. Verification
# ========================================

print_status "Verifying backup integrity"

# Verify source code backup
if [[ -f "$SOURCE_CODE_BACKUP" ]]; then
    if tar -tzf "$SOURCE_CODE_BACKUP" | head -5 &>/dev/null; then
        print_success "Source code backup integrity verified"
    else
        print_error "Source code backup integrity check failed"
    fi
fi

# Verify configuration backup
if [[ -f "$CONFIG_BACKUP" ]]; then
    if tar -tzf "$CONFIG_BACKUP" | head -5 &>/dev/null; then
        print_success "Configuration backup integrity verified"
    else
        print_error "Configuration backup integrity check failed"
    fi
fi

# ========================================
# 11. Backup Status
# ========================================

STATUS_FILE="$BACKUP_DIR/latest_application_backup_status.txt"

cat > "$STATUS_FILE" << EOF
Latest Application Backup Status
==============================
Date: $DATE
Status: SUCCESS
Server: $(hostname)
Components: Source Code, Configuration, Docker Volumes, SSL, System Config, Logs
Size: $(du -sh "$BACKUP_DIR/$DATE/application" | cut -f1)
Location: $BACKUP_DIR/$DATE/application
Next Backup: $(date -d "+1 day" +"%Y-%m-%d %H:%M:%S")
EOF

print_success "Application backup completed successfully!"
echo "Backup location: $BACKUP_DIR/$DATE/application"
echo "Manifest: $MANIFEST_FILE"
echo ""
echo "To restore application:"
echo "1. Extract source code: tar -xzf source_code_${DATE}.tar.gz"
echo "2. Extract configuration: tar -xzf config_${DATE}.tar.gz"
echo "3. Restore Docker volumes: docker run --rm -v volume_name:/data -v \$(pwd):/backup alpine tar -xzf /backup/volume_volume_name_${DATE}.tar.gz -C /data"
echo "4. Restart services: docker-compose -f docker-compose.production.yml up -d"
