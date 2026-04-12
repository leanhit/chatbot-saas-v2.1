#!/bin/bash

echo "=== Automated Backup Script for Chatbot SaaS v2.1 ==="
echo "Running comprehensive automated backup system"
echo ""

# Configuration
BACKUP_DIR="/backups/chatbot-saas"
DATE=$(date +%Y%m%d_%H%M%S)
LOG_FILE="$BACKUP_DIR/logs/automated_backup_$DATE.log"
RETENTION_DAYS=30
REMOTE_BACKUP_ENABLED="${REMOTE_BACKUP_ENABLED:-false}"
REMOTE_BACKUP_PATH="${REMOTE_BACKUP_PATH:-s3://chatbot-saas-backups/}"
NOTIFICATION_EMAIL="${NOTIFICATION_EMAIL:-}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}=== $1 ===${NC}"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] === $1 ===" >> "$LOG_FILE"
}

print_success() {
    echo -e "${GREEN}SUCCESS: $1${NC}"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] SUCCESS: $1" >> "$LOG_FILE"
}

print_error() {
    echo -e "${RED}ERROR: $1${NC}"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $1" >> "$LOG_FILE"
}

print_warning() {
    echo -e "${YELLOW}WARNING: $1${NC}"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] WARNING: $1" >> "$LOG_FILE"
}

# Initialize log
mkdir -p "$BACKUP_DIR/logs"
echo "=== Automated Backup Session Started: $(date) ===" >> "$LOG_FILE"

# Check if running as root
if [[ $EUID -ne 0 ]]; then
   print_error "This script must be run as root (use sudo)"
   exit 1
fi

# ========================================
# 1. Pre-Backup Checks
# ========================================

print_status "Running pre-backup checks"

# Check disk space
AVAILABLE_SPACE=$(df "$BACKUP_DIR" | awk 'NR==2 {print $4}')
REQUIRED_SPACE=5242880  # 5GB in KB

if [[ $AVAILABLE_SPACE -lt $REQUIRED_SPACE ]]; then
    print_error "Insufficient disk space for backup. Available: ${AVAILABLE_SPACE}KB, Required: ${REQUIRED_SPACE}KB"
    exit 1
fi

# Check service status
SERVICES_OK=true

if ! pg_isready -h localhost -U traloitudong_user; then
    print_error "PostgreSQL is not ready"
    SERVICES_OK=false
fi

if ! redis-cli -p 6380 ping &>/dev/null; then
    print_error "Redis is not responding"
    SERVICES_OK=false
fi

if ! curl -f http://localhost:8080/actuator/health &>/dev/null; then
    print_warning "Application is not responding - continuing with backup"
fi

if [[ "$SERVICES_OK" == "false" ]]; then
    print_error "Critical services are not ready - aborting backup"
    exit 1
fi

print_success "Pre-backup checks completed"

# ========================================
# 2. Database Backup
# ========================================

print_status "Starting database backup"

DATABASE_BACKUP_SUCCESS=true

# Run database backup script
if ./backup/backup-databases.sh >> "$LOG_FILE" 2>&1; then
    print_success "Database backup completed successfully"
else
    print_error "Database backup failed"
    DATABASE_BACKUP_SUCCESS=false
fi

# ========================================
# 3. Application Backup
# ========================================

print_status "Starting application backup"

APPLICATION_BACKUP_SUCCESS=true

# Run application backup script
if ./backup/backup-application.sh >> "$LOG_FILE" 2>&1; then
    print_success "Application backup completed successfully"
else
    print_error "Application backup failed"
    APPLICATION_BACKUP_SUCCESS=false
fi

# ========================================
# 4. System State Backup
# ========================================

print_status "Starting system state backup"

SYSTEM_BACKUP_DIR="$BACKUP_DIR/$DATE/system"
mkdir -p "$SYSTEM_BACKUP_DIR"

# Backup system information
echo "System Information:" > "$SYSTEM_BACKUP_DIR/system_info.txt"
echo "==================" >> "$SYSTEM_BACKUP_DIR/system_info.txt"
echo "Date: $(date)" >> "$SYSTEM_BACKUP_DIR/system_info.txt"
echo "Hostname: $(hostname)" >> "$SYSTEM_BACKUP_DIR/system_info.txt"
echo "OS: $(uname -a)" >> "$SYSTEM_BACKUP_DIR/system_info.txt"
echo "Uptime: $(uptime)" >> "$SYSTEM_BACKUP_DIR/system_info.txt"
echo "" >> "$SYSTEM_BACKUP_DIR/system_info.txt"

# Backup running processes
ps aux > "$SYSTEM_BACKUP_DIR/processes.txt"

# Backup network configuration
ip addr show > "$SYSTEM_BACKUP_DIR/network.txt"
netstat -tuln > "$SYSTEM_BACKUP_DIR/connections.txt"

# Backup mounted filesystems
mount > "$SYSTEM_BACKUP_DIR/mounts.txt"

# Backup disk usage
df -h > "$SYSTEM_BACKUP_DIR/disk_usage.txt"

# Backup memory usage
free -h > "$SYSTEM_BACKUP_DIR/memory_usage.txt"

# Backup Docker state
docker ps -a > "$SYSTEM_BACKUP_DIR/docker_containers.txt"
docker images > "$SYSTEM_BACKUP_DIR/docker_images.txt"
docker volume ls > "$SYSTEM_BACKUP_DIR/docker_volumes.txt"

# Create system backup archive
if tar -czf "$BACKUP_DIR/$DATE/system_state_${DATE}.tar.gz" -C "$SYSTEM_BACKUP_DIR" .; then
    print_success "System state backup completed"
    rm -rf "$SYSTEM_BACKUP_DIR"
else
    print_error "System state backup failed"
fi

# ========================================
# 5. Backup Verification
# ========================================

print_status "Verifying backup integrity"

BACKUP_VERIFICATION_SUCCESS=true

# Verify database backups
for db in traloitudong_db botpress_db odoo_db; do
    BACKUP_FILE="$BACKUP_DIR/$DATE/${db}_${DATE}.sql.gz"
    if [[ -f "$BACKUP_FILE" ]]; then
        if gunzip -t "$BACKUP_FILE" 2>/dev/null; then
            print_success "Database backup integrity verified: $db"
        else
            print_error "Database backup integrity check failed: $db"
            BACKUP_VERIFICATION_SUCCESS=false
        fi
    else
        print_error "Database backup file not found: $db"
        BACKUP_VERIFICATION_SUCCESS=false
    fi
done

# Verify application backups
APP_BACKUP_FILES=(
    "source_code_${DATE}.tar.gz"
    "config_${DATE}.tar.gz"
    "system_state_${DATE}.tar.gz"
)

for file in "${APP_BACKUP_FILES[@]}"; do
    BACKUP_FILE="$BACKUP_DIR/$DATE/application/$file"
    if [[ -f "$BACKUP_FILE" ]]; then
        if tar -tzf "$BACKUP_FILE" &>/dev/null; then
            print_success "Application backup integrity verified: $file"
        else
            print_error "Application backup integrity check failed: $file"
            BACKUP_VERIFICATION_SUCCESS=false
        fi
    else
        print_error "Application backup file not found: $file"
        BACKUP_VERIFICATION_SUCCESS=false
    fi
done

# ========================================
# 6. Remote Backup (if enabled)
# ========================================

if [[ "$REMOTE_BACKUP_ENABLED" == "true" ]]; then
    print_status "Starting remote backup"
    
    REMOTE_BACKUP_SUCCESS=true
    
    # Check if AWS CLI is available
    if command -v aws &> /dev/null; then
        # Sync to S3
        if aws s3 sync "$BACKUP_DIR/$DATE" "$REMOTE_BACKUP_PATH/$DATE" --delete; then
            print_success "Remote backup completed successfully"
        else
            print_error "Remote backup failed"
            REMOTE_BACKUP_SUCCESS=false
        fi
    else
        print_warning "AWS CLI not available - skipping remote backup"
    fi
else
    print_status "Remote backup disabled"
fi

# ========================================
# 7. Cleanup Old Backups
# ========================================

print_status "Cleaning up old backups"

# Clean up local backups older than retention period
find "$BACKUP_DIR" -type d -name "*" -mtime +$RETENTION_DAYS -exec rm -rf {} + 2>/dev/null

if [[ $? -eq 0 ]]; then
    print_success "Old backups cleaned up successfully"
else
    print_warning "Some old backups could not be removed"
fi

# Clean up remote backups if enabled
if [[ "$REMOTE_BACKUP_ENABLED" == "true" ]] && command -v aws &> /dev/null; then
    # Clean up S3 backups older than retention period
    CUTOFF_DATE=$(date -d "$RETENTION_DAYS days ago" +%Y-%m-%d)
    if aws s3 ls "$REMOTE_BACKUP_PATH" --recursive | awk '$1 < "'$CUTOFF_DATE'" {print $4}' | xargs -I {} aws s3 rm "$REMOTE_BACKUP_PATH{}"; then
        print_success "Old remote backups cleaned up successfully"
    else
        print_warning "Some old remote backups could not be removed"
    fi
fi

# ========================================
# 8. Backup Summary
# ========================================

print_status "Generating backup summary"

# Calculate backup sizes
DATABASE_SIZE=$(du -sh "$BACKUP_DIR/$DATE" 2>/dev/null | cut -f1 | head -1)
APPLICATION_SIZE=$(du -sh "$BACKUP_DIR/$DATE/application" 2>/dev/null | cut -f1 | head -1)
TOTAL_SIZE=$(du -sh "$BACKUP_DIR/$DATE" 2>/dev/null | cut -f1 | head -1)

# Create summary report
cat > "$BACKUP_DIR/$DATE/backup_summary.txt" << EOF
Chatbot SaaS v2.1 Automated Backup Summary
========================================
Backup Date: $(date)
Backup Duration: $SECONDS seconds
Server: $(hostname)

Backup Results:
- Database Backup: $DATABASE_BACKUP_SUCCESS
- Application Backup: $APPLICATION_BACKUP_SUCCESS
- System State Backup: Completed
- Backup Verification: $BACKUP_VERIFICATION_SUCCESS
- Remote Backup: ${REMOTE_BACKUP_SUCCESS:-Disabled}

Backup Sizes:
- Database: $DATABASE_SIZE
- Application: $APPLICATION_SIZE
- Total: $TOTAL_SIZE

Backup Location: $BACKUP_DIR/$DATE
Remote Location: ${REMOTE_BACKUP_PATH:-Disabled}
Log File: $LOG_FILE

Next Scheduled Backup: $(date -d "+1 day" +"%Y-%m-%d %H:%M:%S")

Files Created:
$(ls -la "$BACKUP_DIR/$DATE" | grep -v "^total")

Backup Retention: $RETENTION_DAYS days
EOF

print_success "Backup summary created: $BACKUP_DIR/$DATE/backup_summary.txt"

# ========================================
# 9. Health Check
# ========================================

print_status "Running post-backup health check"

# Check if services are still running
HEALTH_CHECK_PASSED=true

if ! pg_isready -h localhost -U traloitudong_user; then
    print_error "PostgreSQL health check failed"
    HEALTH_CHECK_PASSED=false
fi

if ! redis-cli -p 6380 ping &>/dev/null; then
    print_error "Redis health check failed"
    HEALTH_CHECK_PASSED=false
fi

if ! curl -f http://localhost:8080/actuator/health &>/dev/null; then
    print_warning "Application health check failed"
fi

if [[ "$HEALTH_CHECK_PASSED" == "true" ]]; then
    print_success "Post-backup health check passed"
else
    print_error "Post-backup health check failed"
fi

# ========================================
# 10. Notification
# ========================================

if [[ -n "$NOTIFICATION_EMAIL" ]]; then
    print_status "Sending backup notification"
    
    # Determine notification status
    if [[ "$DATABASE_BACKUP_SUCCESS" == "true" && "$APPLICATION_BACKUP_SUCCESS" == "true" && "$BACKUP_VERIFICATION_SUCCESS" == "true" && "$HEALTH_CHECK_PASSED" == "true" ]]; then
        SUBJECT="Chatbot SaaS Backup SUCCESS - $DATE"
        STATUS="SUCCESS"
    else
        SUBJECT="Chatbot SaaS Backup FAILURE - $DATE"
        STATUS="FAILURE"
    fi
    
    # Create notification email
    cat > /tmp/backup_notification.txt << EOF
Subject: $SUBJECT

Chatbot SaaS v2.1 Automated Backup $STATUS
==========================================

Backup Details:
- Date: $(date)
- Server: $(hostname)
- Duration: $SECONDS seconds
- Total Size: $TOTAL_SIZE

Results:
- Database Backup: $DATABASE_BACKUP_SUCCESS
- Application Backup: $APPLICATION_BACKUP_SUCCESS
- Backup Verification: $BACKUP_VERIFICATION_SUCCESS
- Health Check: $HEALTH_CHECK_PASSED
- Remote Backup: ${REMOTE_BACKUP_SUCCESS:-Disabled}

Backup Location: $BACKUP_DIR/$DATE
Log File: $LOG_FILE

Files Created:
$(ls -la "$BACKUP_DIR/$DATE" | grep -v "^total")

Next Scheduled Backup: $(date -d "+1 day" +"%Y-%m-%d %H:%M:%S")

This is an automated notification from Chatbot SaaS backup system.
EOF
    
    # Send email
    if command -v mail &> /dev/null; then
        mail -s "$SUBJECT" "$NOTIFICATION_EMAIL" < /tmp/backup_notification.txt
        print_success "Backup notification sent to $NOTIFICATION_EMAIL"
    else
        print_warning "Mail command not available - notification not sent"
    fi
    
    rm -f /tmp/backup_notification.txt
fi

# ========================================
# 11. Final Status
# ========================================

print_status "Automated backup completed"

# Determine overall success
OVERALL_SUCCESS=true

if [[ "$DATABASE_BACKUP_SUCCESS" != "true" ]]; then
    OVERALL_SUCCESS=false
fi

if [[ "$APPLICATION_BACKUP_SUCCESS" != "true" ]]; then
    OVERALL_SUCCESS=false
fi

if [[ "$BACKUP_VERIFICATION_SUCCESS" != "true" ]]; then
    OVERALL_SUCCESS=false
fi

if [[ "$HEALTH_CHECK_PASSED" != "true" ]]; then
    OVERALL_SUCCESS=false
fi

# Update status file
STATUS_FILE="$BACKUP_DIR/latest_backup_status.txt"

cat > "$STATUS_FILE" << EOF
Latest Automated Backup Status
==========================
Date: $DATE
Status: $OVERALL_SUCCESS
Server: $(hostname)
Duration: $SECONDS seconds
Database: $DATABASE_BACKUP_SUCCESS
Application: $APPLICATION_BACKUP_SUCCESS
Verification: $BACKUP_VERIFICATION_SUCCESS
Health Check: $HEALTH_CHECK_PASSED
Remote: ${REMOTE_BACKUP_SUCCESS:-Disabled}
Size: $TOTAL_SIZE
Location: $BACKUP_DIR/$DATE
Next Backup: $(date -d "+1 day" +"%Y-%m-%d %H:%M:%S")
EOF

# Final message
echo "========================================" >> "$LOG_FILE"
echo "Automated backup session completed: $(date)" >> "$LOG_FILE"
echo "Overall Status: $OVERALL_SUCCESS" >> "$LOG_FILE"
echo "Duration: $SECONDS seconds" >> "$LOG_FILE"
echo "========================================" >> "$LOG_FILE"

if [[ "$OVERALL_SUCCESS" == "true" ]]; then
    print_success "Automated backup completed successfully!"
    echo "Backup location: $BACKUP_DIR/$DATE"
    echo "Total size: $TOTAL_SIZE"
    echo "Duration: $SECONDS seconds"
    echo "Log file: $LOG_FILE"
    exit 0
else
    print_error "Automated backup completed with errors!"
    echo "Check log file for details: $LOG_FILE"
    exit 1
fi
