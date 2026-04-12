#!/bin/bash

echo "=== Database Backup Script for Chatbot SaaS v2.1 ==="
echo "Creating comprehensive database backups"
echo ""

# Configuration
BACKUP_DIR="/backups/chatbot-saas"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=30
POSTGRES_HOST="localhost"
POSTGRES_USER="traloitudong_user"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-traloitudong_Admin_2025}"

# Databases to backup
DATABASES=("traloitudong_db" "botpress_db" "odoo_db")

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

# Check if PostgreSQL is running
if ! pg_isready -h $POSTGRES_HOST -U $POSTGRES_USER; then
    print_error "PostgreSQL is not running or not accessible"
    exit 1
fi

# Create backup directory
mkdir -p "$BACKUP_DIR/$DATE"
mkdir -p "$BACKUP_DIR/logs"

print_status "Starting database backup for $DATE"

# Backup each database
for db in "${DATABASES[@]}"; do
    print_status "Backing up database: $db"
    
    BACKUP_FILE="$BACKUP_DIR/$DATE/${db}_${DATE}.sql"
    COMPRESSED_FILE="$BACKUP_DIR/$DATE/${db}_${DATE}.sql.gz"
    
    # Create database backup
    if PGPASSWORD="$POSTGRES_PASSWORD" pg_dump -h $POSTGRES_HOST -U $POSTGRES_USER \
        --verbose --clean --if-exists --create --format=custom \
        --compress=9 --lock-wait-timeout=300000 \
        --file="$BACKUP_FILE" "$db" 2>&1 | tee "$BACKUP_DIR/logs/${db}_${DATE}.log"; then
        
        # Compress backup
        if gzip "$BACKUP_FILE"; then
            print_success "Database $db backed up successfully: $COMPRESSED_FILE"
            
            # Verify backup
            BACKUP_SIZE=$(stat -f%z "$COMPRESSED_FILE" 2>/dev/null || stat -c%s "$COMPRESSED_FILE" 2>/dev/null)
            echo "Backup size: $BACKUP_SIZE bytes"
            
            # Test backup integrity
            if PGPASSWORD="$POSTGRES_PASSWORD" pg_restore --list "$COMPRESSED_FILE" &>/dev/null; then
                print_success "Backup integrity verified for $db"
            else
                print_error "Backup integrity check failed for $db"
            fi
        else
            print_error "Failed to compress backup for $db"
        fi
    else
        print_error "Failed to backup database $db"
    fi
done

# Create database roles backup
print_status "Backing up database roles"
ROLES_FILE="$BACKUP_DIR/$DATE/roles_${DATE}.sql"

if PGPASSWORD="$POSTGRES_PASSWORD" pg_dumpall -h $POSTGRES_HOST -U $POSTGRES_USER \
    --roles-only --file="$ROLES_FILE" 2>&1 | tee "$BACKUP_DIR/logs/roles_${DATE}.log"; then
    gzip "$ROLES_FILE"
    print_success "Database roles backed up successfully"
else
    print_error "Failed to backup database roles"
fi

# Create schema-only backup
print_status "Creating schema-only backup"
SCHEMA_FILE="$BACKUP_DIR/$DATE/schema_${DATE}.sql"

for db in "${DATABASES[@]}"; do
    if PGPASSWORD="$POSTGRES_PASSWORD" pg_dump -h $POSTGRES_HOST -U $POSTGRES_USER \
        --schema-only --no-owner --no-privileges \
        --file="$BACKUP_DIR/$DATE/schema_${db}_${DATE}.sql" "$db"; then
        gzip "$BACKUP_DIR/$DATE/schema_${db}_${DATE}.sql"
        print_success "Schema backup created for $db"
    fi
done

# Create backup manifest
print_status "Creating backup manifest"
MANIFEST_FILE="$BACKUP_DIR/$DATE/MANIFEST.txt"

cat > "$MANIFEST_FILE" << EOF
Chatbot SaaS v2.1 Database Backup Manifest
===========================================
Backup Date: $(date)
Backup Type: Full Database Backup
Server: $(hostname)
PostgreSQL Version: $(psql --version | head -n1)

Databases Backed Up:
EOF

for db in "${DATABASES[@]}"; do
    if [[ -f "$BACKUP_DIR/$DATE/${db}_${DATE}.sql.gz" ]]; then
        SIZE=$(stat -f%z "$BACKUP_DIR/$DATE/${db}_${DATE}.sql.gz" 2>/dev/null || stat -c%s "$BACKUP_DIR/$DATE/${db}_${DATE}.sql.gz" 2>/dev/null)
        echo "- $db: $(du -h "$BACKUP_DIR/$DATE/${db}_${DATE}.sql.gz" | cut -f1) ($SIZE bytes)" >> "$MANIFEST_FILE"
    fi
done

cat >> "$MANIFEST_FILE" << EOF

Backup Files:
EOF

ls -la "$BACKUP_DIR/$DATE" >> "$MANIFEST_FILE"

cat >> "$MANIFEST_FILE" << EOF

Recovery Instructions:
1. Extract backup: gunzip ${db}_${DATE}.sql.gz
2. Restore database: psql -U $POSTGRES_USER -h $POSTGRES_HOST -d $db < ${db}_${DATE}.sql
3. Restore roles: psql -U $POSTGRES_USER -h $POSTGRES_HOST < roles_${DATE}.sql

Backup Retention: $RETENTION_DAYS days
Next Scheduled Backup: $(date -d "+1 day" +"%Y-%m-%d %H:%M:%S")
EOF

print_success "Backup manifest created: $MANIFEST_FILE"

# Cleanup old backups
print_status "Cleaning up old backups (older than $RETENTION_DAYS days)"

find "$BACKUP_DIR" -type d -name "*" -mtime +$RETENTION_DAYS -exec rm -rf {} + 2>/dev/null

if [[ $? -eq 0 ]]; then
    print_success "Old backups cleaned up"
else
    print_warning "Some old backups could not be removed"
fi

# Create backup summary
print_status "Backup Summary"
echo "=================="
echo "Backup Directory: $BACKUP_DIR/$DATE"
echo "Databases Backed Up: ${#DATABASES[@]}"
echo "Total Backup Size: $(du -sh "$BACKUP_DIR/$DATE" | cut -f1)"
echo "Manifest: $MANIFEST_FILE"
echo "Logs: $BACKUP_DIR/logs/"
echo ""

# Display backup files
echo "Backup Files Created:"
ls -la "$BACKUP_DIR/$DATE" | grep -v "^total"

# Test restore capability (optional)
if [[ "${TEST_RESTORE:-false}" == "true" ]]; then
    print_status "Testing restore capability"
    
    TEST_DB="test_restore_$(date +%s)"
    
    # Create test database
    if PGPASSWORD="$POSTGRES_PASSWORD" createdb -h $POSTGRES_HOST -U $POSTGRES_USER "$TEST_DB"; then
        # Test restore first database
        FIRST_DB="${DATABASES[0]}"
        TEST_BACKUP="$BACKUP_DIR/$DATE/${FIRST_DB}_${DATE}.sql.gz"
        
        if gunzip -c "$TEST_BACKUP" | PGPASSWORD="$POSTGRES_PASSWORD" psql -h $POSTGRES_HOST -U $POSTGRES_USER "$TEST_DB"; then
            print_success "Restore test passed for $FIRST_DB"
        else
            print_error "Restore test failed for $FIRST_DB"
        fi
        
        # Clean up test database
        PGPASSWORD="$POSTGRES_PASSWORD" dropdb -h $POSTGRES_HOST -U $POSTGRES_USER "$TEST_DB"
    fi
fi

# Send notification (if configured)
if [[ -n "${BACKUP_NOTIFICATION_EMAIL:-}" ]]; then
    print_status "Sending backup notification"
    
    # Create notification email
    cat > /tmp/backup_notification.txt << EOF
Subject: Chatbot SaaS Database Backup Completed - $DATE

Chatbot SaaS v2.1 database backup has been completed successfully.

Backup Details:
- Date: $(date)
- Server: $(hostname)
- Database Count: ${#DATABASES[@]}
- Total Size: $(du -sh "$BACKUP_DIR/$DATE" | cut -f1)
- Location: $BACKUP_DIR/$DATE

Backup Files:
$(ls -la "$BACKUP_DIR/$DATE" | grep -v "^total")

This is an automated notification from Chatbot SaaS backup system.
EOF
    
    # Send email (requires mail command)
    if command -v mail &> /dev/null; then
        mail -s "Chatbot SaaS Database Backup Completed - $DATE" "$BACKUP_NOTIFICATION_EMAIL" < /tmp/backup_notification.txt
        print_success "Backup notification sent to $BACKUP_NOTIFICATION_EMAIL"
    else
        print_warning "Mail command not available - notification not sent"
    fi
    
    rm -f /tmp/backup_notification.txt
fi

# Create backup status file
STATUS_FILE="$BACKUP_DIR/latest_backup_status.txt"

cat > "$STATUS_FILE" << EOF
Latest Backup Status
==================
Date: $DATE
Status: SUCCESS
Server: $(hostname)
Databases: ${DATABASES[@]}
Size: $(du -sh "$BACKUP_DIR/$DATE" | cut -f1)
Location: $BACKUP_DIR/$DATE
Next Backup: $(date -d "+1 day" +"%Y-%m-%d %H:%M:%S")
EOF

print_success "Database backup completed successfully!"
echo "Backup location: $BACKUP_DIR/$DATE"
echo "Manifest: $MANIFEST_FILE"
echo "Logs: $BACKUP_DIR/logs/"
echo ""
echo "To restore: gunzip ${DATABASES[0]}_${DATE}.sql.gz && psql -U $POSTGRES_USER -h $POSTGRES_HOST -d ${DATABASES[0]} < ${DATABASES[0]}_${DATE}.sql"
