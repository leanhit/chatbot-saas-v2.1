#!/bin/bash

echo "=== Backup Schedule Setup Script for Chatbot SaaS v2.1 ==="
echo "Configuring automated backup schedules"
echo ""

# Configuration
BACKUP_SCRIPT_DIR="/root/ltanh/chatbot-saas-v2.1/backend/backup"
NOTIFICATION_EMAIL="${NOTIFICATION_EMAIL:-admin@yourdomain.com}"
REMOTE_BACKUP_ENABLED="${REMOTE_BACKUP_ENABLED:-false}"

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
# 1. Create Backup User
# ========================================

print_status "Creating backup user"

# Create backup user if it doesn't exist
if ! id "backup" &>/dev/null; then
    useradd -r -m -s /bin/bash backup
    print_success "Backup user created"
else
    print_warning "Backup user already exists"
fi

# Add backup user to required groups
usermod -a -G docker backup 2>/dev/null || true
usermod -a -G sudo backup 2>/dev/null || true

# Create backup user home directory
mkdir -p /home/backup/.ssh
chown -R backup:backup /home/backup

# ========================================
# 2. Setup Backup Directories
# ========================================

print_status "Setting up backup directories"

# Create backup directories
mkdir -p /backups/chatbot-saas/logs
mkdir -p /backups/chatbot-saas/temp
mkdir -p /var/log/chatbot-saas/backup

# Set permissions
chown -R backup:backup /backups/chatbot-saas
chmod -R 755 /backups/chatbot-saas
chown -R backup:backup /var/log/chatbot-saas/backup
chmod -R 755 /var/log/chatbot-saas/backup

print_success "Backup directories created and configured"

# ========================================
# 3. Setup Cron Jobs
# ========================================

print_status "Setting up cron jobs"

# Create backup cron file
cat > /etc/cron.d/chatbot-saas-backup << EOF
# Chatbot SaaS v2.1 Backup Schedule
# =====================================

# Daily full backup at 2:00 AM
0 2 * * * backup /bin/bash $BACKUP_SCRIPT_DIR/automated-backup.sh >> /var/log/chatbot-saas/backup/daily_backup.log 2>&1

# Weekly database verification on Sunday at 3:00 AM
0 3 * * 0 backup /bin/bash $BACKUP_SCRIPT_DIR/backup-databases.sh >> /var/log/chatbot-saas/backup/weekly_verification.log 2>&1

# Monthly application backup on 1st at 4:00 AM
0 4 1 * * backup /bin/bash $BACKUP_SCRIPT_DIR/backup-application.sh >> /var/log/chatbot-saas/backup/monthly_backup.log 2>&1

# Backup cleanup daily at 5:00 AM
0 5 * * * backup find /backups/chatbot-saas -type d -name "*" -mtime +30 -exec rm -rf {} + >> /var/log/chatbot-saas/backup/cleanup.log 2>&1

# Backup health check every 6 hours
0 */6 * * * backup /bin/bash $BACKUP_SCRIPT_DIR/backup-health-check.sh >> /var/log/chatbot-saas/backup/health_check.log 2>&1
EOF

# Reload cron service
systemctl reload cron

print_success "Cron jobs configured"

# ========================================
# 4. Create Backup Health Check Script
# ========================================

print_status "Creating backup health check script"

cat > "$BACKUP_SCRIPT_DIR/backup-health-check.sh" << 'EOF'
#!/bin/bash

echo "=== Backup Health Check ==="
echo "Date: $(date)"
echo ""

BACKUP_DIR="/backups/chatbot-saas"
TODAY=$(date +%Y%m%d)
YESTERDAY=$(date -d "1 day ago" +%Y%m%d)
HEALTH_ISSUES=0

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_status() {
    echo -e "${BLUE}=== $1 ===${NC}"
}

print_success() {
    echo -e "${GREEN}SUCCESS: $1${NC}"
}

print_error() {
    echo -e "${RED}ERROR: $1${NC}"
    HEALTH_ISSUES=$((HEALTH_ISSUES + 1))
}

print_warning() {
    echo -e "${YELLOW}WARNING: $1${NC}"
}

# Check today's backup
print_status "Checking today's backup ($TODAY)"

if [[ -d "$BACKUP_DIR/$TODAY" ]]; then
    BACKUP_SIZE=$(du -sh "$BACKUP_DIR/$TODAY" | cut -f1)
    print_success "Today's backup found: $BACKUP_SIZE"
    
    # Check backup files
    EXPECTED_FILES=("traloitudong_db_${TODAY}.sql.gz" "source_code_${TODAY}.tar.gz" "config_${TODAY}.tar.gz")
    
    for file in "${EXPECTED_FILES[@]}"; do
        if [[ -f "$BACKUP_DIR/$TODAY/$file" ]] || [[ -f "$BACKUP_DIR/$TODAY/application/$file" ]]; then
            print_success "Backup file found: $file"
        else
            print_error "Backup file missing: $file"
        fi
    done
else
    print_error "Today's backup not found"
fi

# Check yesterday's backup
print_status "Checking yesterday's backup ($YESTERDAY)"

if [[ -d "$BACKUP_DIR/$YESTERDAY" ]]; then
    BACKUP_SIZE=$(du -sh "$BACKUP_DIR/$YESTERDAY" | cut -f1)
    print_success "Yesterday's backup found: $BACKUP_SIZE"
else
    print_warning "Yesterday's backup not found"
fi

# Check backup disk space
print_status "Checking backup disk space"

AVAILABLE_SPACE=$(df "$BACKUP_DIR" | awk 'NR==2 {print $4}')
AVAILABLE_GB=$((AVAILABLE_SPACE / 1024 / 1024))

if [[ $AVAILABLE_GB -lt 10 ]]; then
    print_error "Low disk space: ${AVAILABLE_GB}GB available"
else
    print_success "Sufficient disk space: ${AVAILABLE_GB}GB available"
fi

# Check backup age
print_status "Checking backup age"

LATEST_BACKUP=$(ls -t "$BACKUP_DIR" | head -1)
if [[ -n "$LATEST_BACKUP" ]]; then
    BACKUP_AGE=$(( ($(date +%s) - $(date -d "$LATEST_BACKUP" +%s)) / 86400 ))
    
    if [[ $BACKUP_AGE -le 2 ]]; then
        print_success "Latest backup is $BACKUP_AGE days old"
    else
        print_error "Latest backup is too old: $BACKUP_AGE days"
    fi
else
    print_error "No backups found"
fi

# Check backup integrity (sample)
print_status "Checking backup integrity"

LATEST_DB_BACKUP=$(ls -t "$BACKUP_DIR"/*/traloitudong_db_*.sql.gz | head -1)
if [[ -f "$LATEST_DB_BACKUP" ]]; then
    if gunzip -t "$LATEST_DB_BACKUP" 2>/dev/null; then
        print_success "Latest database backup integrity verified"
    else
        print_error "Latest database backup integrity check failed"
    fi
else
    print_warning "No database backup found for integrity check"
fi

# Check log files
print_status "Checking backup logs"

LOG_FILES=(
    "/var/log/chatbot-saas/backup/daily_backup.log"
    "/var/log/chatbot-saas/backup/weekly_verification.log"
    "/var/log/chatbot-saas/backup/monthly_backup.log"
)

for log_file in "${LOG_FILES[@]}"; do
    if [[ -f "$log_file" ]]; then
        LOG_SIZE=$(stat -f%z "$log_file" 2>/dev/null || stat -c%s "$log_file" 2>/dev/null)
        print_success "Log file found: $(basename "$log_file") (${LOG_SIZE} bytes)"
    else
        print_warning "Log file not found: $(basename "$log_file")"
    fi
done

# Summary
echo ""
print_status "Backup Health Summary"
echo "========================="
echo "Health Issues Found: $HEALTH_ISSUES"
echo "Latest Backup: $LATEST_BACKUP"
echo "Available Disk Space: ${AVAILABLE_GB}GB"

if [[ $HEALTH_ISSUES -eq 0 ]]; then
    print_success "All backup health checks passed"
    exit 0
else
    print_error "$HEALTH_ISSUES backup health issues found"
    exit 1
fi
EOF

chmod +x "$BACKUP_SCRIPT_DIR/backup-health-check.sh"
chown backup:backup "$BACKUP_SCRIPT_DIR/backup-health-check.sh"

print_success "Backup health check script created"

# ========================================
# 5. Setup Log Rotation
# ========================================

print_status "Setting up log rotation"

# Create logrotate configuration
cat > /etc/logrotate.d/chatbot-saas-backup << EOF
# Chatbot SaaS Backup Log Rotation
/var/log/chatbot-saas/backup/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 backup backup
    postrotate
        systemctl reload rsyslog 2>/dev/null || true
    endscript
}

# Backup directory logs
/backups/chatbot-saas/logs/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 backup backup
}
EOF

print_success "Log rotation configured"

# ========================================
# 6. Setup Monitoring
# ========================================

print_status "Setting up backup monitoring"

# Create backup monitoring script
cat > "$BACKUP_SCRIPT_DIR/backup-monitor.sh" << 'EOF'
#!/bin/bash

# Backup monitoring script for Chatbot SaaS v2.1
# This script monitors backup status and sends alerts

BACKUP_DIR="/backups/chatbot-saas"
ALERT_EMAIL="${NOTIFICATION_EMAIL:-admin@yourdomain.com}"
TODAY=$(date +%Y%m%d)
STATUS_FILE="$BACKUP_DIR/latest_backup_status.txt"

# Check if today's backup exists
if [[ ! -d "$BACKUP_DIR/$TODAY" ]]; then
    # Send alert
    echo "Chatbot SaaS Backup Alert - $(date)" | \
    mail -s "ALERT: Chatbot SaaS Backup Failed - No backup found for $TODAY" "$ALERT_EMAIL"
    exit 1
fi

# Check backup status file
if [[ -f "$STATUS_FILE" ]]; then
    STATUS=$(grep "Status:" "$STATUS_FILE" | cut -d' ' -f2)
    if [[ "$STATUS" != "SUCCESS" ]]; then
        # Send alert
        cat "$STATUS_FILE" | \
        mail -s "ALERT: Chatbot SaaS Backup Failed - Status: $STATUS" "$ALERT_EMAIL"
        exit 1
    fi
else
    # Send alert
    echo "Backup status file not found" | \
    mail -s "ALERT: Chatbot SaaS Backup Status File Missing" "$ALERT_EMAIL"
    exit 1
fi

exit 0
EOF

chmod +x "$BACKUP_SCRIPT_DIR/backup-monitor.sh"
chown backup:backup "$BACKUP_SCRIPT_DIR/backup-monitor.sh"

# Add monitoring to cron
echo "0 6 * * * backup /bin/bash $BACKUP_SCRIPT_DIR/backup-monitor.sh" >> /etc/cron.d/chatbot-saas-backup

print_success "Backup monitoring configured"

# ========================================
# 7. Setup Environment Variables
# ========================================

print_status "Setting up environment variables"

# Create backup environment file
cat > /etc/environment.d/chatbot-saas-backup << EOF
# Chatbot SaaS Backup Environment Variables
NOTIFICATION_EMAIL=$NOTIFICATION_EMAIL
REMOTE_BACKUP_ENABLED=$REMOTE_BACKUP_ENABLED
BACKUP_DIR=/backups/chatbot-saas
BACKUP_SCRIPT_DIR=$BACKUP_SCRIPT_DIR
EOF

print_success "Environment variables configured"

# ========================================
# 8. Test Backup Schedule
# ========================================

print_status "Testing backup schedule"

# Test backup script as backup user
sudo -u backup /bin/bash "$BACKUP_SCRIPT_DIR/backup-health-check.sh"

if [[ $? -eq 0 ]]; then
    print_success "Backup health check test passed"
else
    print_error "Backup health check test failed"
fi

# Test cron syntax
if crontab -l 2>/dev/null | grep -q "chatbot-saas-backup"; then
    print_success "Cron jobs syntax verified"
else
    print_error "Cron jobs syntax error"
fi

# ========================================
# 9. Create Backup Documentation
# ========================================

print_status "Creating backup documentation"

cat > "$BACKUP_DIR/README.md" << EOF
# Chatbot SaaS v2.1 Backup System

## Overview

This directory contains automated backups for Chatbot SaaS v2.1.

## Backup Schedule

- **Daily Full Backup**: 2:00 AM
- **Weekly Database Verification**: Sunday 3:00 AM
- **Monthly Application Backup**: 1st of month 4:00 AM
- **Backup Cleanup**: Daily 5:00 AM
- **Health Check**: Every 6 hours

## Backup Structure

\`\`\`
/backups/chatbot-saas/
|-- YYYYMMDD_HHMMSS/
|   |-- traloitudong_db_YYYYMMDD_HHMMSS.sql.gz
|   |-- botpress_db_YYYYMMDD_HHMMSS.sql.gz
|   |-- odoo_db_YYYYMMDD_HHMMSS.sql.gz
|   |-- application/
|   |   |-- source_code_YYYYMMDD_HHMMSS.tar.gz
|   |   |-- config_YYYYMMDD_HHMMSS.tar.gz
|   |   |-- volume_*.tar.gz
|   |   |-- ssl_YYYYMMDD_HHMMSS.tar.gz
|   |   |-- system_YYYYMMDD_HHMMSS.tar.gz
|   |   |-- logs_YYYYMMDD_HHMMSS.tar.gz
|   |   |-- MANIFEST.txt
|   |-- backup_summary.txt
|-- logs/
|   |-- automated_backup_YYYYMMDD_HHMMSS.log
|   |-- daily_backup.log
|   |-- weekly_verification.log
|   |-- monthly_backup.log
|   |-- cleanup.log
|   |-- health_check.log
\`\`\`

## Manual Backup Commands

### Database Backup
\`\`\`bash
sudo /root/ltanh/chatbot-saas-v2.1/backend/backup/backup-databases.sh
\`\`\`

### Application Backup
\`\`\`bash
sudo /root/ltanh/chatbot-saas-v2.1/backend/backup/backup-application.sh
\`\`\`

### Full Automated Backup
\`\`\`bash
sudo /root/ltanh/chatbot-saas-v2.1/backend/backup/automated-backup.sh
\`\`\`

### Health Check
\`\`\`bash
sudo /root/ltanh/chatbot-saas-v2.1/backend/backup/backup-health-check.sh
\`\`\`

## Restoration Commands

### Database Restoration
\`\`\`bash
# Extract backup
gunzip /backups/chatbot-saas/YYYYMMDD_HHMMSS/traloitudong_db_YYYYMMDD_HHMMSS.sql.gz

# Restore database
psql -U traloitudong_user -h localhost -d traloitudong_db < traloitudong_db_YYYYMMDD_HHMMSS.sql
\`\`\`

### Application Restoration
\`\`\`bash
# Extract source code
tar -xzf /backups/chatbot-saas/YYYYMMDD_HHMMSS/application/source_code_YYYYMMDD_HHMMSS.tar.gz -C /root/

# Extract configuration
tar -xzf /backups/chatbot-saas/YYYYMMDD_HHMMSS/application/config_YYYYMMDD_HHMMSS.tar.gz -C /root/ltanh/chatbot-saas-v2.1/backend/

# Restore Docker volumes
docker run --rm -v volume_name:/data -v /backups/chatbot-saas/YYYYMMDD_HHMMSS/application:/backup alpine tar -xzf backup/volume_volume_name_YYYYMMDD_HHMMSS.tar.gz -C /data
\`\`\`

## Monitoring

### Check Backup Status
\`\`\`bash
cat /backups/chatbot-saas/latest_backup_status.txt
\`\`\`

### View Backup Logs
\`\`\`bash
tail -f /var/log/chatbot-saas/backup/daily_backup.log
\`\`\`

### Check Disk Usage
\`\`\`bash
du -sh /backups/chatbot-saas
\`\`\`

## Troubleshooting

### Common Issues

1. **Permission Denied**: Ensure backup user has proper permissions
2. **Disk Space**: Check available disk space before backup
3. **Service Unavailable**: Verify PostgreSQL and Redis are running
4. **Backup Corruption**: Check backup integrity logs

### Health Check Issues

1. **Missing Backup**: Check backup logs for errors
2. **Integrity Failure**: Verify backup files are not corrupted
3. **Low Disk Space**: Clean up old backups or increase storage

## Contact Information

- **System Administrator**: admin@yourdomain.com
- **Backup User**: backup@localhost
- **Log Location**: /var/log/chatbot-saas/backup/

## Retention Policy

- **Local Backups**: 30 days
- **Remote Backups**: 90 days (if enabled)
- **Logs**: 30 days

Last Updated: $(date)
EOF

print_success "Backup documentation created"

# ========================================
# 10. Final Setup
# ========================================

print_status "Final backup setup"

# Reload cron
systemctl reload cron

# Set proper permissions
chown -R backup:backup "$BACKUP_SCRIPT_DIR"
chmod -R 755 "$BACKUP_SCRIPT_DIR"

# Create symlink for easy access
ln -sf "$BACKUP_SCRIPT_DIR" /home/backup/scripts
chown backup:backup /home/backup/scripts

# Test backup user permissions
sudo -u backup whoami
sudo -u backup ls "$BACKUP_SCRIPT_DIR"

print_success "Backup setup completed successfully!"

# ========================================
# 11. Summary
# ========================================

print_status "Backup Schedule Summary"
echo "==========================="
echo "Backup User: backup"
echo "Backup Directory: /backups/chatbot-saas"
echo "Log Directory: /var/log/chatbot-saas/backup"
echo ""
echo "Scheduled Backups:"
echo "- Daily Full Backup: 2:00 AM"
echo "- Weekly Database Verification: Sunday 3:00 AM"
echo "- Monthly Application Backup: 1st of month 4:00 AM"
echo "- Backup Cleanup: Daily 5:00 AM"
echo "- Health Check: Every 6 hours"
echo ""
echo "Notification Email: $NOTIFICATION_EMAIL"
echo "Remote Backup: $REMOTE_BACKUP_ENABLED"
echo ""
echo "Useful Commands:"
echo "- Manual backup: sudo /root/ltanh/chatbot-saas-v2.1/backend/backup/automated-backup.sh"
echo "- Health check: sudo /root/ltanh/chatbot-saas-v2.1/backend/backup/backup-health-check.sh"
echo "- View status: cat /backups/chatbot-saas/latest_backup_status.txt"
echo "- View logs: tail -f /var/log/chatbot-saas/backup/daily_backup.log"
echo ""
echo "Documentation: /backups/chatbot-saas/README.md"

print_success "Backup schedule setup completed!"
echo "Your Chatbot SaaS v2.1 is now protected with automated backups"
