package com.chatbot.core.tenant.controller;

import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.model.TenantAuditLog;
import com.chatbot.core.tenant.repository.TenantAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Read-only audit log endpoint for tenant admins.
 */
@RestController
@RequestMapping("/api/tenant/audit-logs")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final TenantAuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        }
        int safeSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TenantAuditLog> result;
        if (action != null && !action.isBlank()) {
            result = auditLogRepository.findByTenantIdAndActionContainingIgnoreCaseOrderByCreatedAtDesc(
                    tenantId, action, pageable);
        } else {
            result = auditLogRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable);
        }
        return ResponseEntity.ok(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "number", result.getNumber()
        ));
    }
}
