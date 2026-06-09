package com.chatbot.core.simplepayment.repository;

import com.chatbot.core.simplepayment.model.PaymentAuditLog;
import com.chatbot.core.simplepayment.model.PaymentAuditLog.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentAuditLogRepository extends JpaRepository<PaymentAuditLog, Long> {

    List<PaymentAuditLog> findByPaymentReferenceCodeOrderByCreatedAtDesc(String paymentReferenceCode);

    List<PaymentAuditLog> findByUserIdAndTenantIdOrderByCreatedAtDesc(Long userId, Long tenantId);

    List<PaymentAuditLog> findByActionAndCreatedAtAfter(AuditAction action, LocalDateTime since);

    @Query("SELECT a FROM PaymentAuditLog a WHERE a.paymentReferenceCode = :referenceCode AND a.action = :action ORDER BY a.createdAt DESC")
    List<PaymentAuditLog> findByPaymentReferenceCodeAndAction(@Param("referenceCode") String referenceCode, @Param("action") AuditAction action);

    @Query("SELECT COUNT(a) FROM PaymentAuditLog a WHERE a.userId = :userId AND a.createdAt >= :since")
    Long countByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(a) FROM PaymentAuditLog a WHERE a.tenantId = :tenantId AND a.createdAt >= :since")
    Long countByTenantIdAndCreatedAtAfter(@Param("tenantId") Long tenantId, @Param("since") LocalDateTime since);
}
