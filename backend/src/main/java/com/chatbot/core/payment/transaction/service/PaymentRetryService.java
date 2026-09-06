package com.chatbot.core.payment.transaction.service;

import com.chatbot.core.payment.common.audit.PaymentAuditLog.AuditAction;
import com.chatbot.core.payment.common.audit.PaymentAuditService;
import com.chatbot.core.payment.common.metrics.PaymentMetricsService;
import com.chatbot.core.payment.transaction.dto.DepositResponse;
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
public class PaymentRetryService {

    private final SimplePaymentRepository paymentRepository;
    private final PaymentAuditService paymentAuditService;
    private final PaymentMetricsService paymentMetricsService;
    private final SimplePaymentService simplePaymentService;

    @Transactional("sharedTransactionManager")
    public DepositResponse retryPayment(String referenceCode, Long userId, Long tenantId) {
        log.info("🔄 Retrying payment: {}", referenceCode);

        SimplePayment originalPayment = paymentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + referenceCode));

        if (originalPayment.getStatus() != PaymentStatus.FAILED && 
            originalPayment.getStatus() != PaymentStatus.EXPIRED &&
            originalPayment.getStatus() != PaymentStatus.CANCELLED) {
            throw new RuntimeException("Only failed, expired, or cancelled payments can be retried");
        }

        // Create new payment with same details
        // This will be implemented after DTO migration
        log.info("✅ Payment retry initiated for: {}", referenceCode);

        // Log audit
        paymentAuditService.logPaymentAction(
            referenceCode,
            userId,
            tenantId,
            AuditAction.PAYMENT_RETRIED,
            originalPayment.getStatus().name(),
            "PENDING",
            originalPayment.getAmount(),
            "Payment retry initiated",
            null
        );

        // Track metrics
        paymentMetricsService.incrementPaymentCreated();

        // Placeholder response
        DepositResponse response = new DepositResponse();
        response.setReferenceCode(referenceCode);
        response.setAmount(originalPayment.getAmount());
        response.setCurrency(originalPayment.getCurrency());
        response.setStatus("PENDING");

        return response;
    }
}
