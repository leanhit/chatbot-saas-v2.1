package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.dto.PaymentEvent;
import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.model.SimplePayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventService {

    private final BankApiService bankApiService;
    private final SimplePaymentService simplePaymentService;
    private final TaskScheduler taskScheduler;

    /**
     * Event khi payment mới được tạo
     * Processed AFTER transaction commits to ensure payment exists in database
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handlePaymentCreated(PaymentCreatedEvent event) {
        log.info("🎯 Payment created event: {}", event.getReferenceCode());
        
        // Schedule individual payment check
        schedulePaymentCheck(event.getReferenceCode(), event.getCreatedAt());
    }

    /**
     * Event-driven payment check
     */
    private void schedulePaymentCheck(String referenceCode, LocalDateTime createdAt) {
        // Check ngay lập tức
        checkSinglePayment(referenceCode);
        
        // Schedule retry với exponential backoff
        scheduleRetryWithBackoff(referenceCode, createdAt);
    }

    /**
     * Check individual payment
     */
    private void checkSinglePayment(String referenceCode) {
        try {
            // Check if payment is still PENDING before calling bank API
            SimplePayment payment = simplePaymentService.getPaymentByReference(referenceCode);
            if (payment.getStatus() != PaymentStatus.PENDING) {
                log.info("🎯 Payment {} already completed, skipping bank API check", referenceCode);
                return;
            }

            String bankTransactionId = bankApiService.findTransactionByReference(referenceCode);
            
            if (bankTransactionId != null) {
                // Complete payment directly without circular dependency
                completePaymentDirectly(referenceCode, bankTransactionId);
            }
        } catch (Exception e) {
            log.error("❌ Error checking payment {}: {}", referenceCode, e.getMessage());
        }
    }

    /**
     * Complete payment directly delegating to SimplePaymentService
     */
    private void completePaymentDirectly(String referenceCode, String bankTransactionId) {
        simplePaymentService.completePayment(referenceCode, bankTransactionId);
        log.info("✅ Payment completed via event delegation: {}", referenceCode);
    }

    /**
     * Exponential backoff retry
     */
    private void scheduleRetryWithBackoff(String referenceCode, LocalDateTime createdAt) {
        // Retry sau: 30s, 2m, 5m, 15m, 30m
        int[] retryIntervals = {30, 120, 300, 900, 1800};
        
        for (int i = 0; i < retryIntervals.length; i++) {
            // Schedule retry với delay
            scheduleRetry(referenceCode, retryIntervals[i], i + 1);
        }
    }

    /**
     * Schedule single retry using TaskScheduler (non-blocking)
     */
    private void scheduleRetry(String referenceCode, int delaySeconds, int attempt) {
        taskScheduler.schedule(() -> {
            checkSinglePayment(referenceCode);
            log.debug("🔄 Retry attempt {} for payment {}", attempt, referenceCode);
        }, Instant.now().plusSeconds(delaySeconds));
        log.debug("📅 Scheduled retry attempt {} for payment {} in {} seconds", attempt, referenceCode, delaySeconds);
    }

    /**
     * Payment created event
     */
    public static class PaymentCreatedEvent {
        private final String referenceCode;
        private final LocalDateTime createdAt;
        private final Long userId;
        private final Long tenantId;

        public PaymentCreatedEvent(String referenceCode, LocalDateTime createdAt, Long userId, Long tenantId) {
            this.referenceCode = referenceCode;
            this.createdAt = createdAt;
            this.userId = userId;
            this.tenantId = tenantId;
        }

        // Getters
        public String getReferenceCode() { return referenceCode; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public Long getUserId() { return userId; }
        public Long getTenantId() { return tenantId; }
    }
}
