package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.dto.PaymentEvent;
import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import com.chatbot.core.simplepayment.metrics.PaymentMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.chatbot.core.notification.websocket.NotificationWebSocketHandler;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentExpirationService {

    private final SimplePaymentRepository paymentRepository;
    private final RedisPaymentService redisPaymentService;
    private final PaymentEmailNotificationService emailNotificationService;
    private final WebhookService webhookService;
    private final PaymentAuditService paymentAuditService;
    private final PaymentMetricsService paymentMetricsService;
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    /**
     * Expire old pending payments
     */
    @Transactional("sharedTransactionManager")
    public void expireOldPayments() {
        log.info("⏰ Expiring old pending payments...");

        List<SimplePayment> expiredPayments = paymentRepository.findByStatusAndExpiresAtBefore(
                PaymentStatus.PENDING, LocalDateTime.now()
        );

        for (SimplePayment payment : expiredPayments) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);

            // Publish Redis event for real-time notification
            PaymentEvent event = redisPaymentService.createStatusUpdateEvent(
                payment.getReferenceCode(), PaymentStatus.EXPIRED, null
            );
            redisPaymentService.publishPaymentEvent(event);

            // Trigger email notification
            emailNotificationService.sendPaymentExpiredEmail(payment.getReferenceCode());

            // Trigger webhook notification
            webhookService.triggerWebhook(com.chatbot.core.simplepayment.model.Webhook.WebhookEventType.PAYMENT_EXPIRED, payment);

            // Log audit
            paymentAuditService.logPaymentAction(
                payment.getReferenceCode(),
                payment.getUserId(),
                payment.getTenantId(),
                com.chatbot.core.simplepayment.model.PaymentAuditLog.AuditAction.PAYMENT_EXPIRED,
                "PENDING",
                "EXPIRED",
                payment.getAmount(),
                "Payment expired automatically",
                null
            );

            // Track metrics
            paymentMetricsService.incrementPaymentExpired();
        }

        log.info("✅ Expired {} pending payments", expiredPayments.size());
    }

    /**
     * Expire a single pending payment
     */
    @Transactional("sharedTransactionManager")
    public void expirePayment(String referenceCode) {
        log.info("⏰ Expiring payment: {}", referenceCode);

        SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                .orElse(null);

        if (payment == null) {
            log.warn("Payment {} not found for expiration", referenceCode);
            return;
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.info("Payment {} is already in status {}, skipping expiration", referenceCode, payment.getStatus());
            return;
        }

        payment.setStatus(PaymentStatus.EXPIRED);
        paymentRepository.save(payment);

        // Publish Redis event for real-time notification
        PaymentEvent event = redisPaymentService.createStatusUpdateEvent(
            payment.getReferenceCode(), PaymentStatus.EXPIRED, null
        );
        redisPaymentService.publishPaymentEvent(event);

        // Trigger email notification
        emailNotificationService.sendPaymentExpiredEmail(payment.getReferenceCode());

        // Trigger webhook notification
        webhookService.triggerWebhook(com.chatbot.core.simplepayment.model.Webhook.WebhookEventType.PAYMENT_EXPIRED, payment);

        // Log audit
        paymentAuditService.logPaymentAction(
            payment.getReferenceCode(),
            payment.getUserId(),
            payment.getTenantId(),
            com.chatbot.core.simplepayment.model.PaymentAuditLog.AuditAction.PAYMENT_EXPIRED,
            "PENDING",
            "EXPIRED",
            payment.getAmount(),
            "Payment expired automatically",
            null
        );

        // Track metrics
        paymentMetricsService.incrementPaymentExpired();

        // Send WebSocket notification toast to the user
        try {
            Map<String, Object> wsPayload = Map.of(
                "type", "PAYMENT_FAILED",
                "data", Map.of(
                    "amount", payment.getAmount(),
                    "referenceCode", payment.getReferenceCode(),
                    "status", "EXPIRED",
                    "reason", "Yêu cầu thanh toán đã hết hạn"
                )
            );
            notificationWebSocketHandler.sendToUser(payment.getUserId(), wsPayload);
            log.info("📧 Sent PAYMENT_FAILED (EXPIRED) WebSocket notification to user ID: {}", payment.getUserId());
        } catch (Exception e) {
            log.error("❌ Failed to send WebSocket expired notification: {}", e.getMessage(), e);
        }
        
        log.info("✅ Payment {} expired successfully", referenceCode);
    }
}
