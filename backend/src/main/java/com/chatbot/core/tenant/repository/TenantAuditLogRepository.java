package com.chatbot.core.tenant.repository;

import com.chatbot.core.tenant.model.TenantAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantAuditLogRepository extends JpaRepository<TenantAuditLog, Long> {
    Page<TenantAuditLog> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    Page<TenantAuditLog> findByTenantIdAndActionContainingIgnoreCaseOrderByCreatedAtDesc(
            Long tenantId, String action, Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional(value = "tenantTransactionManager", rollbackFor = Exception.class)
    @org.springframework.data.jpa.repository.Query("DELETE FROM TenantAuditLog l WHERE l.tenantId = :tenantId")
    void deleteByTenantId(@org.springframework.data.repository.query.Param("tenantId") Long tenantId);
}
