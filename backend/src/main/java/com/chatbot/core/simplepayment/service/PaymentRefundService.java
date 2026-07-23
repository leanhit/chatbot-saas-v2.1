package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.exception.*;
import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import com.chatbot.core.user.repository.UserRepository;
import com.chatbot.shared.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentRefundService {

    private final SimplePaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final BalanceService balanceService;
    private final RedisPaymentService redisPaymentService;
    private final PaymentEmailNotificationService emailNotificationService;
    private final WebhookService webhookService;

    /**
     * Refund a completed payment
     */
    @Transactional("sharedTransactionManager")
    public SimplePayment refundPayment(String referenceCode, String reason, Long adminUserId) {
        log.info("💰 Refunding payment: {}, reason: {}, admin: {}", referenceCode, reason, adminUserId);

        SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new PaymentNotFoundException(referenceCode));

        // Validate payment can be refunded
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentCannotBeRefundedException(
                String.format("Payment cannot be refunded. Current status: %s", payment.getStatus())
            );
        }

        // Check if payment was already refunded
        if (payment.getDescription() != null && payment.getDescription().contains("[REFUNDED]")) {
            throw new PaymentAlreadyRefundedException(referenceCode);
        }

        // Deduct from user balance (or reverse the package upgrade)
        try {
            // If this was a package upgrade, we need to handle it differently
            if (payment.getTargetPackageId() != null && !payment.getTargetPackageId().trim().isEmpty()) {
                // For package upgrades, we should downgrade the package
                log.warn("⚠️ Refund for package upgrade not fully implemented. Manual intervention may be required.");
            } else {
                // For standard deposits, deduct from balance
                balanceService.deductUserBalance(payment.getUserId(), payment.getAmount());
                log.info("💸 Deducted {} from user {} balance for refund", payment.getAmount(), payment.getUserId());
            }
        } catch (Exception e) {
            log.error("❌ Failed to deduct balance for refund: {}", e.getMessage(), e);
            throw new PaymentException(ErrorCode.PAYMENT_ERROR, "Failed to process refund: " + e.getMessage(), e);
        }

        // Update payment status
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setDescription(payment.getDescription() + " [REFUNDED: " + reason + " by admin " + adminUserId + "]");
        paymentRepository.save(payment);

        // Publish Redis event for real-time notification
        var event = redisPaymentService.createStatusUpdateEvent(
            referenceCode, PaymentStatus.REFUNDED, null
        );
        redisPaymentService.publishPaymentEvent(event);

        // Trigger email notification
        emailNotificationService.sendPaymentRefundEmail(referenceCode);

        // Trigger webhook notification
        webhookService.triggerWebhook(com.chatbot.core.simplepayment.model.Webhook.WebhookEventType.PAYMENT_REFUNDED, payment);

        log.info("✅ Payment refunded successfully: {}", referenceCode);
        return payment;
    }

    /**
     * Check if payment can be refunded
     */
    public boolean canRefundPayment(String referenceCode) {
        SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                .orElse(null);

        if (payment == null) {
            return false;
        }

        boolean isCompleted = payment.getStatus() == PaymentStatus.COMPLETED;
        boolean notAlreadyRefunded = payment.getDescription() == null 
            || !payment.getDescription().contains("[REFUNDED]");
        
        return isCompleted && notAlreadyRefunded;
    }

    /**
     * Get refundable amount for a payment
     */
    public BigDecimal getRefundableAmount(String referenceCode) {
        SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                .orElse(null);

        if (payment == null || !canRefundPayment(referenceCode)) {
            return BigDecimal.ZERO;
        }

        return payment.getAmount();
    }
}
