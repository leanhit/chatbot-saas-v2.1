package com.chatbot.core.payment.transaction.service;

import com.chatbot.core.payment.common.audit.PaymentAuditLog.AuditAction;
import com.chatbot.core.payment.common.audit.PaymentAuditService;
import com.chatbot.core.payment.common.event.PaymentFailedEvent;
import com.chatbot.core.payment.common.metrics.PaymentMetricsService;
import com.chatbot.core.payment.transaction.model.PaymentStatus;
import com.chatbot.core.payment.transaction.model.SimplePayment;
import com.chatbot.core.payment.transaction.repository.SimplePaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCancellationService {

    private final SimplePaymentRepository paymentRepository;
    private final PaymentAuditService paymentAuditService;
    private final PaymentMetricsService paymentMetricsService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional("sharedTransactionManager")
    public SimplePayment cancelPayment(String referenceCode, String reason) {
        log.info("🚫 Cancelling payment: {}", referenceCode);

        SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + referenceCode));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Only pending payments can be cancelled");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        SimplePayment cancelled = paymentRepository.save(payment);

        // Publish event
        eventPublisher.publishEvent(new PaymentFailedEvent(
            this,
            referenceCode,
            payment.getUserId(),
            payment.getTenantId(),
            payment.getAmount(),
            payment.getCurrency(),
            reason,
            LocalDateTime.now(),
            payment.getTargetPackageId()
        ));

        // Log audit
        paymentAuditService.logPaymentAction(
            referenceCode,
            payment.getUserId(),
            payment.getTenantId(),
            AuditAction.PAYMENT_CANCELLED,
            "PENDING",
            "CANCELLED",
            payment.getAmount(),
            "Payment cancelled: " + reason,
            null
        );

        // Track metrics
        paymentMetricsService.incrementPaymentCancelled();

        log.info("✅ Payment cancelled: {}", referenceCode);
        return cancelled;
    }
}
