package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.dto.PaymentEvent;
import com.chatbot.core.simplepayment.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentTTLService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final BankApiService bankApiService;
    private final RedisPaymentService redisPaymentService;

    private static final String PAYMENT_TTL_PREFIX = "payment:ttl:";
    private static final String PAYMENT_CHECK_PREFIX = "payment:check:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24); // 24 hours

    /**
     * Set TTL cho payment mới
     */
    public void setPaymentTTL(String referenceCode, LocalDateTime createdAt) {
        String key = PAYMENT_TTL_PREFIX + referenceCode;
        
        // Set TTL key với expiration time
        redisTemplate.opsForValue().set(key, "active", DEFAULT_TTL);
        
        // Schedule immediate check
        scheduleImmediateCheck(referenceCode);
        
        log.info("⏰ Set TTL for payment {}: {}", referenceCode, DEFAULT_TTL);
    }

    /**
     * Schedule immediate check
     */
    @Async
    public void scheduleImmediateCheck(String referenceCode) {
        try {
            // Check ngay lập tức
            Thread.sleep(1000); // 1 second delay
            checkPaymentWithTTL(referenceCode);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Check payment với TTL
     */
    private void checkPaymentWithTTL(String referenceCode) {
        try {
            String bankTransactionId = bankApiService.findTransactionByReference(referenceCode);
            
            if (bankTransactionId != null) {
                // Payment completed
                completePaymentWithTTL(referenceCode, bankTransactionId);
            } else {
                // Schedule next check với exponential backoff
                scheduleNextCheck(referenceCode);
            }
        } catch (Exception e) {
            log.error("❌ Error checking payment {} with TTL: {}", referenceCode, e.getMessage());
        }
    }

    /**
     * Complete payment và remove TTL
     */
    private void completePaymentWithTTL(String referenceCode, String bankTransactionId) {
        // Remove TTL key
        redisTemplate.delete(PAYMENT_TTL_PREFIX + referenceCode);
        redisTemplate.delete(PAYMENT_CHECK_PREFIX + referenceCode);
        
        // Publish completion event
        PaymentEvent event = redisPaymentService.createStatusUpdateEvent(
            referenceCode, PaymentStatus.COMPLETED, bankTransactionId
        );
        redisPaymentService.publishPaymentEvent(event);
        
        log.info("✅ Payment completed via TTL: {}", referenceCode);
    }

    /**
     * Schedule next check với backoff
     */
    private void scheduleNextCheck(String referenceCode) {
        String checkKey = PAYMENT_CHECK_PREFIX + referenceCode;
        
        // Get current attempt count
        Integer attempts = (Integer) redisTemplate.opsForValue().get(checkKey);
        if (attempts == null) attempts = 0;
        
        if (attempts >= 5) {
            // Max attempts reached - let TTL expire naturally
            log.warn("⏰ Max attempts reached for payment {}, letting TTL expire", referenceCode);
            return;
        }
        
        // Calculate next delay (exponential backoff)
        int[] delays = {30, 120, 300, 900, 1800}; // 30s, 2m, 5m, 15m, 30m
        int nextDelay = attempts < delays.length ? delays[attempts] : 1800;
        
        // Increment attempt counter
        redisTemplate.opsForValue().set(checkKey, attempts + 1, Duration.ofHours(25));
        
        // Schedule next check
        scheduleDelayedCheck(referenceCode, nextDelay);
        
        log.info("🔄 Scheduled check {} for payment {} in {} seconds", attempts + 1, referenceCode, nextDelay);
    }

    /**
     * Schedule delayed check
     */
    @Async
    private void scheduleDelayedCheck(String referenceCode, int delaySeconds) {
        try {
            Thread.sleep(delaySeconds * 1000L);
            checkPaymentWithTTL(referenceCode);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Check if payment TTL exists
     */
    public boolean hasPaymentTTL(String referenceCode) {
        String key = PAYMENT_TTL_PREFIX + referenceCode;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Remove payment TTL
     */
    public void removePaymentTTL(String referenceCode) {
        redisTemplate.delete(PAYMENT_TTL_PREFIX + referenceCode);
        redisTemplate.delete(PAYMENT_CHECK_PREFIX + referenceCode);
        log.info("🗑️ Removed TTL for payment: {}", referenceCode);
    }
}
