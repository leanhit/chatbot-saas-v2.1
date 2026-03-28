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
     * Check pending payments every 10 seconds
     */
    @Scheduled(fixedDelay = 10000) // 10 seconds
    public void checkPendingPayments() {
        log.debug("🏦 Running scheduled payment check...");
        
        try {
            simplePaymentService.checkPendingPayments();
        } catch (Exception e) {
            log.error("❌ Error in scheduled payment check: {}", e.getMessage(), e);
        }
    }

    /**
     * Expire old payments every 5 minutes
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes
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
