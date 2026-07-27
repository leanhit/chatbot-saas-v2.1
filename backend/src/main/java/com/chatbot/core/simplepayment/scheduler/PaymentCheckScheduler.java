package com.chatbot.core.simplepayment.scheduler;

import com.chatbot.core.simplepayment.service.SimplePaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCheckScheduler {

    private final SimplePaymentService simplePaymentService;
    private final org.springframework.data.redis.core.RedisTemplate<String, String> redisTemplate;

    /**
     * ENABLED - Payment checking for automatic payment completion
     * Runs every 30 seconds to check pending payments
     */
    @Scheduled(fixedDelay = 30000) // ENABLED - Check every 30 seconds
    public void checkPendingPayments() {
        String lockKey = "lock:scheduler:checkPendingPayments";
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", java.time.Duration.ofSeconds(25));
        if (!Boolean.TRUE.equals(acquired)) {
            log.debug("🏦 [PaymentCheckScheduler] Lock already held by another instance. Skipping check.");
            return;
        }

        log.debug("🏦 Running scheduled payment check...");
        
        try {
            simplePaymentService.checkPendingPayments();
        } catch (Exception e) {
            log.error("❌ Error in scheduled payment check: {}", e.getMessage(), e);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    // NOTE: Payment expiration is handled by Redis TTL via PaymentTTLService.
    // DB-based expireOldPayments() removed to avoid duplicate expiration logic.

    /**
     * Health check every minute
     */
    @Scheduled(fixedDelay = 60000) // 1 minute
    public void healthCheck() {
        log.debug("💓 Simple Payment Scheduler heartbeat");
    }
}
