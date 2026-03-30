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

    /**
     * DISABLED - Now using Redis pub/sub for real-time payment processing
     * This scheduler is kept as emergency backup only
     */
    // @Scheduled(fixedDelay = 30000) // DISABLED - Use Redis instead
    public void checkPendingPayments() {
        log.debug("🏦 Running scheduled payment check...");
        
        try {
            simplePaymentService.checkPendingPayments();
        } catch (Exception e) {
            log.error("❌ Error in scheduled payment check: {}", e.getMessage(), e);
        }
    }

    /**
     * DISABLED - Payment expiration handled by Redis TTL and events
     */
    // @Scheduled(fixedDelay = 600000) // DISABLED - Use Redis TTL instead
    public void expireOldPayments() {
        log.debug("⏰ Running scheduled payment expiration...");
        
        try {
            simplePaymentService.expireOldPayments();
        } catch (Exception e) {
            log.error("❌ Error in scheduled payment expiration: {}", e.getMessage(), e);
        }
    }

    /**
     * Health check every minute
     */
    @Scheduled(fixedDelay = 60000) // 1 minute
    public void healthCheck() {
        log.debug("💓 Simple Payment Scheduler heartbeat");
    }
}
