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
     * Redis-based polling - check pending payments more efficiently
     * Runs every 5 seconds (faster than original 10 seconds)
     * This replaces the original scheduler
     */
    @Scheduled(fixedDelay = 5000) // 5 seconds - Redis-based polling
    public void checkPendingPaymentsWithRedis() {
        try {
            long pendingCount = redisPaymentService.getPendingPaymentsCount();
            if (pendingCount == 0) {
                log.debug("🏦 No pending payments to check");
                return;
            }

            log.debug("🏦 Checking {} pending payments via Redis...", pendingCount);
            
            // This is where we would integrate with Redis-based queue
            // For now, keep the original logic but with reduced frequency
            checkPendingPayments();
            
        } catch (Exception e) {
            log.error("❌ Error in Redis-based payment check: {}", e.getMessage(), e);
        }
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
