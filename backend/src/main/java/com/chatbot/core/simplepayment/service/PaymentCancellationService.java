package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCancellationService {

    private final SimplePaymentRepository paymentRepository;
    private final RedisPaymentService redisPaymentService;
    private final PaymentEmailNotificationService emailNotificationService;
    private final WebhookService webhookService;

    /**
     * Cancel a pending payment before it expires
     */
    @Transactional("sharedTransactionManager")
    public SimplePayment cancelPayment(String referenceCode, String reason) {
        log.info("🚫 Cancelling payment: {}, reason: {}", referenceCode, reason);

        SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + referenceCode));

        // Validate payment can be cancelled
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException(
                String.format("Payment cannot be cancelled. Current status: %s", payment.getStatus())
            );
        }

        // Check if payment has expired
        if (payment.getExpiresAt() != null && payment.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Payment has already expired");
        }

        // Update payment status
        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setDescription(payment.getDescription() + " [CANCELLED: " + reason + "]");
        paymentRepository.save(payment);

        // Publish Redis event for real-time notification
        var event = redisPaymentService.createStatusUpdateEvent(
            referenceCode, PaymentStatus.CANCELLED, null
        );
        redisPaymentService.publishPaymentEvent(event);

        // Trigger email notification
        emailNotificationService.sendPaymentCancelledEmail(referenceCode);

        // Trigger webhook notification
        webhookService.triggerWebhook(com.chatbot.core.simplepayment.model.Webhook.WebhookEventType.PAYMENT_CANCELLED, payment);

        log.info("✅ Payment cancelled successfully: {}", referenceCode);
        return payment;
    }

    /**
     * Check if payment can be cancelled
     */
    public boolean canCancelPayment(String referenceCode) {
        SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                .orElse(null);

        if (payment == null) {
            return false;
        }

        return payment.getStatus() == PaymentStatus.PENDING 
            && (payment.getExpiresAt() == null || payment.getExpiresAt().isAfter(LocalDateTime.now()));
    }
}
