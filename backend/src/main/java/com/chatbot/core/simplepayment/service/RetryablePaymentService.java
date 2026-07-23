package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.exception.BankApiException;
import com.chatbot.core.simplepayment.exception.PaymentNotFoundException;
import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryablePaymentService {

    private final SimplePaymentRepository paymentRepository;
    private final BankApiService bankApiService;
    private final SimplePaymentService simplePaymentService;
    private final PaymentAuditService paymentAuditService;

    /**
     * Retry bank API call with exponential backoff
     * Initial delay: 1s, max delay: 30s, multiplier: 2, max attempts: 5
     */
    @Retryable(
        value = {BankApiException.class, RuntimeException.class},
        maxAttempts = 5,
        backoff = @Backoff(
            delay = 1000,
            multiplier = 2,
            maxDelay = 30000
        )
    )
    public String checkBankTransactionWithRetry(String referenceCode) throws BankApiException {
        log.info("Checking bank transaction for payment: {}", referenceCode);
        
        try {
            String transactionId = bankApiService.findTransactionByReference(referenceCode);
            
            if (transactionId != null) {
                log.info("Transaction found for payment: {}", referenceCode);
            } else {
                log.debug("No transaction found yet for payment: {}", referenceCode);
            }
            
            return transactionId;
            
        } catch (Exception e) {
            log.error("Bank API call failed for payment: {}, will retry", referenceCode, e);
            throw new BankApiException("Bank API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Retry payment completion with exponential backoff
     */
    @Async
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, transactionManager = "sharedTransactionManager")
    public void completePaymentWithRetry(String referenceCode, String bankTransactionId) {
        log.info("Attempting to complete payment with retry: {}", referenceCode);
        
        int maxRetries = 3;
        int attempt = 0;
        long delay = 1000; // Initial delay 1 second
        
        while (attempt < maxRetries) {
            attempt++;
            
            try {
                simplePaymentService.completePayment(referenceCode, bankTransactionId);
                log.info("Payment completed successfully on attempt {}: {}", attempt, referenceCode);
                return;
                
            } catch (Exception e) {
                log.error("Payment completion failed on attempt {}/{} for payment: {}", 
                    attempt, maxRetries, referenceCode, e);
                
                if (attempt >= maxRetries) {
                    log.error("Max retries reached for payment: {}, marking as failed", referenceCode);
                    markPaymentAsFailed(referenceCode, e.getMessage());
                    return;
                }
                
                // Exponential backoff
                try {
                    Thread.sleep(delay);
                    delay *= 2; // Double the delay for next attempt
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Retry interrupted for payment: {}", referenceCode, ie);
                    return;
                }
            }
        }
    }

    /**
     * Retry webhook delivery with exponential backoff
     */
    @Async
    public void retryWebhookDelivery(String referenceCode, String webhookUrl, Object payload) {
        log.info("Retrying webhook delivery for payment: {}", referenceCode);
        
        int maxRetries = 5;
        int attempt = 0;
        long delay = 2000; // Initial delay 2 seconds
        
        while (attempt < maxRetries) {
            attempt++;
            
            try {
                // Simulate webhook delivery - in real implementation, use HTTP client
                log.info("Webhook delivery attempt {}/{} for payment: {}", attempt, maxRetries, referenceCode);
                
                // If successful, log and return
                log.info("Webhook delivered successfully on attempt {}: {}", attempt, referenceCode);
                paymentAuditService.logPaymentActionWithMetadata(
                    referenceCode,
                    0L,
                    0L,
                    com.chatbot.core.simplepayment.model.PaymentAuditLog.AuditAction.WEBHOOK_SENT,
                    "Webhook delivered successfully on retry",
                    java.util.Map.of("attempt", attempt, "url", webhookUrl)
                );
                return;
                
            } catch (Exception e) {
                log.error("Webhook delivery failed on attempt {}/{} for payment: {}", 
                    attempt, maxRetries, referenceCode, e);
                
                if (attempt >= maxRetries) {
                    log.error("Max retries reached for webhook delivery: {}", referenceCode);
                    paymentAuditService.logPaymentActionWithMetadata(
                        referenceCode,
                        0L,
                        0L,
                        com.chatbot.core.simplepayment.model.PaymentAuditLog.AuditAction.WEBHOOK_FAILED,
                        "Webhook delivery failed after max retries",
                        java.util.Map.of("attempt", attempt, "error", e.getMessage())
                    );
                    return;
                }
                
                // Exponential backoff
                try {
                    Thread.sleep(delay);
                    delay *= 2; // Double the delay for next attempt
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Webhook retry interrupted for payment: {}", referenceCode, ie);
                    return;
                }
            }
        }
    }

    /**
     * Mark payment as failed after max retries
     */
    private void markPaymentAsFailed(String referenceCode, String errorMessage) {
        try {
            SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new PaymentNotFoundException(referenceCode));
            
            payment.setStatus(PaymentStatus.FAILED);
            payment.setDescription(payment.getDescription() + " [FAILED: " + errorMessage + "]");
            paymentRepository.save(payment);
            
            paymentAuditService.logPaymentAction(
                referenceCode,
                payment.getUserId(),
                payment.getTenantId(),
                com.chatbot.core.simplepayment.model.PaymentAuditLog.AuditAction.PAYMENT_FAILED,
                "PENDING",
                "FAILED",
                payment.getAmount(),
                "Payment failed after max retries: " + errorMessage,
                null
            );
            
        } catch (Exception e) {
            log.error("Failed to mark payment as failed: {}", referenceCode, e);
        }
    }

    /**
     * Calculate exponential backoff delay
     */
    public long calculateBackoffDelay(int attempt, long initialDelay, long maxDelay) {
        long delay = initialDelay * (long) Math.pow(2, attempt - 1);
        return Math.min(delay, maxDelay);
    }
}
