package com.chatbot.core.payment.common.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentAuditLogRepository extends JpaRepository<PaymentAuditLog, Long> {

    List<PaymentAuditLog> findByPaymentReferenceCodeOrderByCreatedAtDesc(String paymentReferenceCode);

    List<PaymentAuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PaymentAuditLog> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<PaymentAuditLog> findByActionOrderByCreatedAtDesc(PaymentAuditLog.AuditAction action);

    List<PaymentAuditLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    void deleteByPaymentReferenceCode(String paymentReferenceCode);
}
