package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.dto.DepositRequest;
import com.chatbot.core.simplepayment.dto.DepositResponse;
import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentRetryService {

    private final SimplePaymentRepository paymentRepository;
    private final SimplePaymentService simplePaymentService;
    private final PaymentTTLService paymentTTLService;

    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * Retry a failed payment
     */
    @Transactional("sharedTransactionManager")
    public DepositResponse retryPayment(String referenceCode, Long userId, Long tenantId) {
        log.info("🔄 Retrying payment: {}", referenceCode);

        SimplePayment failedPayment = paymentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + referenceCode));

        // Validate payment can be retried
        if (failedPayment.getStatus() != PaymentStatus.FAILED && failedPayment.getStatus() != PaymentStatus.EXPIRED) {
            throw new RuntimeException(
                String.format("Payment cannot be retried. Current status: %s", failedPayment.getStatus())
            );
        }

        // Check retry attempts
        int retryCount = getRetryCount(failedPayment);
        if (retryCount >= MAX_RETRY_ATTEMPTS) {
            throw new RuntimeException(
                String.format("Maximum retry attempts (%d) reached for payment: %s", MAX_RETRY_ATTEMPTS, referenceCode)
            );
        }

        // Create new deposit request from failed payment
        DepositRequest retryRequest = new DepositRequest();
        retryRequest.setAmount(failedPayment.getAmount());
        retryRequest.setCurrency(failedPayment.getCurrency());
        retryRequest.setDescription("Retry of payment " + referenceCode);
        retryRequest.setTargetPackageId(failedPayment.getTargetPackageId());

        // Create new payment
        DepositResponse response = simplePaymentService.createDeposit(retryRequest, userId, tenantId);

        // Update original payment with retry reference
        failedPayment.setDescription(failedPayment.getDescription() + " [RETRY-" + (retryCount + 1) + ": " + response.getReferenceCode() + "]");
        paymentRepository.save(failedPayment);

        log.info("✅ Payment retry created: {} (original: {})", response.getReferenceCode(), referenceCode);
        return response;
    }

    /**
     * Get retry count for a payment
     */
    private int getRetryCount(SimplePayment payment) {
        if (payment.getDescription() == null) {
            return 0;
        }
        
        int count = 0;
        String desc = payment.getDescription();
        while (desc.contains("[RETRY-")) {
            count++;
            desc = desc.substring(desc.indexOf("[RETRY-") + 7);
        }
        return count;
    }

    /**
     * Auto-retry failed payments (scheduled job)
     */
    @Transactional("sharedTransactionManager")
    public void autoRetryFailedPayments() {
        log.info("🔄 Auto-retrying failed payments...");

        // Get failed payments from last hour
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<SimplePayment> failedPayments = paymentRepository.findByStatusAndCreatedAtAfter(
            PaymentStatus.FAILED, oneHourAgo
        );

        int retriedCount = 0;
        for (SimplePayment payment : failedPayments) {
            try {
                int retryCount = getRetryCount(payment);
                if (retryCount < MAX_RETRY_ATTEMPTS) {
                    // Create new payment with same details
                    DepositRequest retryRequest = new DepositRequest();
                    retryRequest.setAmount(payment.getAmount());
                    retryRequest.setCurrency(payment.getCurrency());
                    retryRequest.setDescription("Auto-retry of payment " + payment.getReferenceCode());
                    retryRequest.setTargetPackageId(payment.getTargetPackageId());

                    DepositResponse response = simplePaymentService.createDeposit(
                        retryRequest, payment.getUserId(), payment.getTenantId()
                    );

                    // Update original payment
                    payment.setDescription(payment.getDescription() + " [AUTO-RETRY-" + (retryCount + 1) + ": " + response.getReferenceCode() + "]");
                    paymentRepository.save(payment);

                    retriedCount++;
                    log.info("✅ Auto-retried payment: {} -> {}", payment.getReferenceCode(), response.getReferenceCode());
                }
            } catch (Exception e) {
                log.error("❌ Failed to auto-retry payment {}: {}", payment.getReferenceCode(), e.getMessage());
            }
        }

        log.info("✅ Auto-retry completed. Retried {} out of {} failed payments", retriedCount, failedPayments.size());
    }

    /**
     * Get retry statistics
     */
    public String getRetryStatistics() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<SimplePayment> failedPayments = paymentRepository.findByStatusAndCreatedAtAfter(
            PaymentStatus.FAILED, oneDayAgo
        );

        int totalFailed = failedPayments.size();
        int retried = (int) failedPayments.stream()
            .filter(p -> p.getDescription() != null && p.getDescription().contains("[RETRY-"))
            .count();

        return String.format("Retry Statistics (24h): Failed=%d, Retried=%d, Success Rate=%.2f%%",
            totalFailed, retried, totalFailed > 0 ? (retried * 100.0 / totalFailed) : 0);
    }
}
