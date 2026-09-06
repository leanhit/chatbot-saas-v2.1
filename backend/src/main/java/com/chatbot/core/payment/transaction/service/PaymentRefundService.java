package com.chatbot.core.payment.transaction.service;

import com.chatbot.core.payment.common.audit.PaymentAuditLog.AuditAction;
import com.chatbot.core.payment.common.audit.PaymentAuditService;
import com.chatbot.core.payment.common.metrics.PaymentMetricsService;
import com.chatbot.core.payment.transaction.model.PaymentStatus;
import com.chatbot.core.payment.transaction.model.SimplePayment;
import com.chatbot.core.payment.transaction.repository.SimplePaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentRefundService {

    private final SimplePaymentRepository paymentRepository;
    private final PaymentAuditService paymentAuditService;
    private final PaymentMetricsService paymentMetricsService;

    @Transactional("sharedTransactionManager")
    public SimplePayment refundPayment(String referenceCode, String reason, Long adminUserId) {
        log.info("💰 Refunding payment: {} by admin: {}", referenceCode, adminUserId);

        SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + referenceCode));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException("Only completed payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        SimplePayment refunded = paymentRepository.save(payment);

        // Log audit
        paymentAuditService.logPaymentAction(
            referenceCode,
            payment.getUserId(),
            payment.getTenantId(),
            AuditAction.PAYMENT_REFUNDED,
            "COMPLETED",
            "REFUNDED",
            payment.getAmount(),
            "Payment refunded by admin: " + adminUserId + " - " + reason,
            null
        );

        // Track metrics
        paymentMetricsService.incrementPaymentRefunded();

        log.info("✅ Payment refunded: {}", referenceCode);
        return refunded;
    }
}
