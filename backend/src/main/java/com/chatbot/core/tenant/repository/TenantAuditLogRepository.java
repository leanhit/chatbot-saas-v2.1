package com.chatbot.core.tenant.repository;

import com.chatbot.core.tenant.model.TenantAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantAuditLogRepository extends JpaRepository<TenantAuditLog, Long> {
    // Additional query methods can be added if needed
}
