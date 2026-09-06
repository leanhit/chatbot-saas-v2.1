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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentExpirationService {

    private final SimplePaymentRepository paymentRepository;
    private final PaymentAuditService paymentAuditService;
    private final PaymentMetricsService paymentMetricsService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Scheduled job to expire pending payments
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional("sharedTransactionManager")
    public void expirePendingPayments() {
        log.info("⏰ Checking for expired payments...");

        LocalDateTime now = LocalDateTime.now();
        List<SimplePayment> expiredPayments = paymentRepository.findByStatusAndExpiresAtBefore(
            PaymentStatus.PENDING, now
        );

        for (SimplePayment payment : expiredPayments) {
            try {
                expirePayment(payment);
            } catch (Exception e) {
                log.error("❌ Failed to expire payment {}: {}", payment.getReferenceCode(), e.getMessage());
            }
        }

        log.info("✅ Expired {} payments", expiredPayments.size());
    }

    @Transactional("sharedTransactionManager")
    public void expirePayment(SimplePayment payment) {
        log.info("⏰ Expiring payment: {}", payment.getReferenceCode());

        payment.setStatus(PaymentStatus.EXPIRED);
        paymentRepository.save(payment);

        // Publish event
        eventPublisher.publishEvent(new PaymentFailedEvent(
            this,
            payment.getReferenceCode(),
            payment.getUserId(),
            payment.getTenantId(),
            payment.getAmount(),
            payment.getCurrency(),
            "Payment expired",
            LocalDateTime.now(),
            payment.getTargetPackageId()
        ));

        // Log audit
        paymentAuditService.logPaymentAction(
            payment.getReferenceCode(),
            payment.getUserId(),
            payment.getTenantId(),
            AuditAction.PAYMENT_EXPIRED,
            "PENDING",
            "EXPIRED",
            payment.getAmount(),
            "Payment expired",
            null
        );

        // Track metrics
        paymentMetricsService.incrementPaymentExpired();

        log.info("✅ Payment expired: {}", payment.getReferenceCode());
    }
}
