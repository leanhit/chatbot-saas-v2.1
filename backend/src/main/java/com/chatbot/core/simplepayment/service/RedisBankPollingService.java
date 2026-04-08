package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.dto.PaymentEvent;
import com.chatbot.core.simplepayment.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisBankPollingService {

    private final RedisPaymentService redisPaymentService;
    private final BankApiService bankApiService;
    private final SimplePaymentService simplePaymentService;

    /**
     * DISABLED - Now using event-driven architecture
     * Payment checks are triggered by PaymentCreatedEvent events
     * This scheduler is kept as emergency backup only
     */
    // @Scheduled(fixedDelay = 5000) // DISABLED - Use event-driven instead
    public void checkPendingPaymentsWithRedis() {
        log.info("⚠️ Event-driven scheduler is disabled. Using event listeners instead.");
        // Emergency fallback - can be enabled manually if needed
        // checkPendingPayments();
    }

    /**
     * Async method to check individual payment
     */
    @Async
    public CompletableFuture<Void> checkPaymentAsync(String referenceCode) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Check with bank API
                String bankTransactionId = bankApiService.findTransactionByReference(referenceCode);
                
                if (bankTransactionId != null) {
                    // Publish completion event via Redis
                    PaymentEvent event = redisPaymentService.createStatusUpdateEvent(
                        referenceCode, PaymentStatus.COMPLETED, bankTransactionId
                    );
                    redisPaymentService.publishPaymentEvent(event);
                    
                    log.info("✅ Payment completed via Redis: {}", referenceCode);
                }
            } catch (Exception e) {
                log.error("❌ Error checking payment {} via Redis: {}", referenceCode, e.getMessage());
            }
        });
    }

    /**
     * Fallback method - now actually calls SimplePaymentService
     */
    private void checkPendingPayments() {
        log.debug("🔄 Using fallback payment check method - calling SimplePaymentService");
        try {
            simplePaymentService.checkPendingPayments();
        } catch (Exception e) {
            log.error("❌ Error in fallback payment check: {}", e.getMessage(), e);
        }
    }
}
