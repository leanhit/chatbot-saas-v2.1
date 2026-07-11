#!/bin/bash
# ============================================================
# PostgreSQL Docker Backup Script
# Chatbot SaaS v2.1
# ============================================================
# Backup tất cả PostgreSQL databases đang chạy trên Docker
# Hỗ trợ: backup thủ công, cron job tự động, rotation
# ============================================================

set -euo pipefail

# ==================== CẤU HÌNH ====================
BACKUP_DIR="${BACKUP_DIR:-/root/chatbot-saas-v2.1/backups/postgres}"
RETENTION_DAYS="${RETENTION_DAYS:-7}"        # Giữ backup trong 7 ngày
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_FILE="${BACKUP_DIR}/backup_${TIMESTAMP}.log"

# Danh sách các database containers và thông tin kết nối
# Format: container_name|db_name|db_user
declare -a DATABASES=(
    "chatbot_saas_postgres_identity|chatbot_identity_db|chatbot_user"
    "chatbot_saas_postgres_user|chatbot_user_db|chatbot_user"
    "chatbot_saas_postgres_tenant|chatbot_tenant_db|chatbot_user"
    "chatbot_saas_postgres_app|chatbot_app_db|chatbot_user"
    "chatbot_saas_postgres_message|chatbot_message_db|chatbot_user"
    "chatbot_saas_postgres_config|chatbot_config_db|chatbot_user"
)

# ==================== HÀM TIỆN ÍCH ====================

log() {
    local msg="[$(date '+%Y-%m-%d %H:%M:%S')] $1"
    echo "$msg"
    echo "$msg" >> "$LOG_FILE"
}

log_error() {
    local msg="[$(date '+%Y-%m-%d %H:%M:%S')] ❌ ERROR: $1"
    echo "$msg" >&2
    echo "$msg" >> "$LOG_FILE"
}

log_success() {
    log "✅ $1"
}

# ==================== BACKUP FUNCTIONS ====================

backup_single_db() {
    local container="$1"
    local db_name="$2"
    local db_user="$3"
    local backup_file="${BACKUP_DIR}/${db_name}_${TIMESTAMP}.sql.gz"

    log "Đang backup: ${db_name} (container: ${container})..."

    # Kiểm tra container có đang chạy không
    if ! docker ps --format '{{.Names}}' | grep -q "^${container}$"; then
        log_error "Container ${container} không đang chạy, bỏ qua!"
        return 1
    fi

    # Chạy pg_dump trong container và nén output
    if docker exec "${container}" pg_dump \
        -U "${db_user}" \
        -d "${db_name}" \
        --no-owner \
        --no-privileges \
        --format=custom \
        --compress=6 \
        --verbose \
        2>> "$LOG_FILE" \
        > "${BACKUP_DIR}/${db_name}_${TIMESTAMP}.dump"; then

        local size=$(du -sh "${BACKUP_DIR}/${db_name}_${TIMESTAMP}.dump" | cut -f1)
        log_success "${db_name} → ${db_name}_${TIMESTAMP}.dump (${size})"
        return 0
    else
        log_error "Backup thất bại cho ${db_name}!"
        rm -f "${BACKUP_DIR}/${db_name}_${TIMESTAMP}.dump"
        return 1
    fi
}

backup_all_databases() {
    local success=0
    local failed=0
    local total=${#DATABASES[@]}

    log "=========================================="
    log "BẮT ĐẦU BACKUP - ${TIMESTAMP}"
    log "Tổng số databases: ${total}"
    log "Thư mục backup: ${BACKUP_DIR}"
    log "=========================================="

    for db_info in "${DATABASES[@]}"; do
        IFS='|' read -r container db_name db_user <<< "$db_info"
        if backup_single_db "$container" "$db_name" "$db_user"; then
            ((success++)) || true
        else
            ((failed++)) || true
        fi
    done

    log "=========================================="
    log "KẾT QUẢ: ${success}/${total} thành công, ${failed} thất bại"
    log "=========================================="

    if [ "$failed" -gt 0 ]; then
        return 1
    fi
    return 0
}

# ==================== ROTATION ====================

cleanup_old_backups() {
    log "Dọn dẹp backup cũ hơn ${RETENTION_DAYS} ngày..."

    local count=$(find "$BACKUP_DIR" -name "*.dump" -o -name "*.sql.gz" -o -name "*.log" | \
                  xargs -I{} find {} -mtime +${RETENTION_DAYS} 2>/dev/null | wc -l)

    find "$BACKUP_DIR" -name "*.dump" -mtime +${RETENTION_DAYS} -delete 2>/dev/null
    find "$BACKUP_DIR" -name "*.sql.gz" -mtime +${RETENTION_DAYS} -delete 2>/dev/null
    find "$BACKUP_DIR" -name "*.log" -mtime +${RETENTION_DAYS} -delete 2>/dev/null

    log "Đã xóa ${count} file backup cũ."
}

# ==================== RESTORE ====================

restore_db() {
    local dump_file="$1"
    local container="$2"
    local db_name="$3"
    local db_user="$4"

    if [ ! -f "$dump_file" ]; then
        log_error "File backup không tồn tại: ${dump_file}"
        return 1
    fi

    echo ""
    echo "⚠️  CẢNH BÁO: Bạn sắp RESTORE database!"
    echo "   File:      ${dump_file}"
    echo "   Database:  ${db_name}"
    echo "   Container: ${container}"
    echo ""
    read -p "Bạn có chắc chắn? (yes/no): " confirm
    if [ "$confirm" != "yes" ]; then
        echo "Đã hủy restore."
        return 0
    fi

    log "Đang restore ${db_name} từ ${dump_file}..."

    # Drop và tạo lại database
    docker exec "${container}" psql -U "${db_user}" -d postgres \
        -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname='${db_name}' AND pid <> pg_backend_pid();" 2>/dev/null

    docker exec "${container}" dropdb -U "${db_user}" --if-exists "${db_name}" 2>> "$LOG_FILE"
    docker exec "${container}" createdb -U "${db_user}" "${db_name}" 2>> "$LOG_FILE"

    # Restore từ dump file
    cat "$dump_file" | docker exec -i "${container}" pg_restore \
        -U "${db_user}" \
        -d "${db_name}" \
        --no-owner \
        --no-privileges \
        --verbose \
        2>> "$LOG_FILE"

    if [ $? -eq 0 ]; then
        log_success "Restore ${db_name} thành công!"
    else
        log_error "Restore ${db_name} có thể có lỗi, kiểm tra log!"
    fi
}

# ==================== LIST BACKUPS ====================

list_backups() {
    echo ""
    echo "📦 Danh sách backup hiện có:"
    echo "============================================"

    if [ ! -d "$BACKUP_DIR" ] || [ -z "$(ls -A "$BACKUP_DIR"/*.dump 2>/dev/null)" ]; then
        echo "Không có backup nào."
        return
    fi

    printf "%-45s %-10s %-20s\n" "FILE" "SIZE" "DATE"
    echo "--------------------------------------------"

    for f in "$BACKUP_DIR"/*.dump; do
        local fname=$(basename "$f")
        local fsize=$(du -sh "$f" | cut -f1)
        local fdate=$(stat -c '%y' "$f" | cut -d'.' -f1)
        printf "%-45s %-10s %-20s\n" "$fname" "$fsize" "$fdate"
    done
    echo ""
}

# ==================== USAGE ====================

usage() {
    cat << EOF

📋 PostgreSQL Docker Backup Tool - Chatbot SaaS v2.1
=====================================================

Sử dụng:
  $0 backup              Backup tất cả databases
  $0 backup-single <name> Backup 1 database (identity|user|tenant|app|message|config)
  $0 restore <file> <container> <db_name> <db_user>
                          Restore từ file backup
  $0 list                 Liệt kê các backup hiện có
  $0 cleanup              Xóa backup cũ (> ${RETENTION_DAYS} ngày)
  $0 help                 Hiển thị hướng dẫn

Ví dụ:
  $0 backup
  $0 backup-single identity
  $0 list
  $0 restore backups/postgres/chatbot_identity_db_20260710.dump \\
      chatbot_saas_postgres_identity chatbot_identity_db chatbot_user

Biến môi trường:
  BACKUP_DIR       Thư mục lưu backup (mặc định: /root/chatbot-saas-v2.1/backups/postgres)
  RETENTION_DAYS   Số ngày giữ backup (mặc định: 7)

EOF
}

# ==================== BACKUP SINGLE ====================

backup_single() {
    local name="$1"
    local found=false

    for db_info in "${DATABASES[@]}"; do
        IFS='|' read -r container db_name db_user <<< "$db_info"
        if echo "$container" | grep -qi "${name}"; then
            backup_single_db "$container" "$db_name" "$db_user"
            found=true
            break
        fi
    done

    if [ "$found" = false ]; then
        log_error "Không tìm thấy database: ${name}"
        echo "Databases có sẵn: identity, user, tenant, app, message, config"
        return 1
    fi
}

# ==================== MAIN ====================

main() {
    # Tạo thư mục backup nếu chưa có
    mkdir -p "$BACKUP_DIR"

    # Tạo log file
    touch "$LOG_FILE"

    case "${1:-help}" in
        backup)
            backup_all_databases
            cleanup_old_backups
            ;;
        backup-single)
            if [ -z "${2:-}" ]; then
                log_error "Thiếu tên database. Ví dụ: $0 backup-single identity"
                exit 1
            fi
            backup_single "$2"
            ;;
        restore)
            if [ -z "${2:-}" ] || [ -z "${3:-}" ] || [ -z "${4:-}" ] || [ -z "${5:-}" ]; then
                log_error "Thiếu tham số. Xem: $0 help"
                exit 1
            fi
            restore_db "$2" "$3" "$4" "$5"
            ;;
        list)
            list_backups
            ;;
        cleanup)
            cleanup_old_backups
            ;;
        help|--help|-h)
            usage
            ;;
        *)
            log_error "Lệnh không hợp lệ: $1"
            usage
            exit 1
            ;;
    esac
}

main "$@"
