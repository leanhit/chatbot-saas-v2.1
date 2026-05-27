package com.chatbot.core.tenant.service;

import com.chatbot.core.tenant.model.TenantAuditLog;
import com.chatbot.core.tenant.repository.TenantAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantAuditLogService {

    private final TenantAuditLogRepository auditLogRepository;

    /**
     * Ghi audit log đồng bộ.
     *
     * @param tenantId  ID của tenant bị tác động
     * @param userEmail email của user thực hiện hành động
     * @param action    tên hành động ngắn gọn (e.g. CREATE_TENANT, UPDATE_ROLE)
     * @param details   mô tả chi tiết tùy chọn
     */
    public void logAction(Long tenantId, String userEmail, String action, String details) {
        try {
            TenantAuditLog entry = TenantAuditLog.builder()
                    .tenantId(tenantId)
                    .userEmail(userEmail)
                    .action(action)
                    .details(details)
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            // Audit log không được làm fail luồng chính
            log.error("[AuditLog] Failed to persist audit log — tenantId={}, action={}: {}",
                    tenantId, action, e.getMessage());
        }
    }

    /**
     * Ghi audit log bất đồng bộ (dùng cho các action ít quan trọng hơn).
     */
    @Async
    public void logActionAsync(Long tenantId, String userEmail, String action, String details) {
        logAction(tenantId, userEmail, action, details);
    }
}
