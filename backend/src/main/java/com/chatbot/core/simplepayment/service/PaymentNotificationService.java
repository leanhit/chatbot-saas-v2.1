package com.chatbot.core.simplepayment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing real-time Server-Sent Events (SSE) subscriptions for payment status updates.
 */
@Service
@Slf4j
public class PaymentNotificationService {

    private static class SseEmitterHolder {
        private final SseEmitter emitter;
        private final long createdAt;

        public SseEmitterHolder(SseEmitter emitter) {
            this.emitter = emitter;
            this.createdAt = System.currentTimeMillis();
        }
    }

    // Manage active SSE connections mapped by referenceCode
    private final Map<String, SseEmitterHolder> emitters = new ConcurrentHashMap<>();

    /**
     * Subscribe to payment event stream for a specific reference code
     */
    public SseEmitter subscribe(String referenceCode) {
        log.info("📶 [PaymentNotificationService] Client subscribing to SSE for payment reference: {}", referenceCode);
        
        // Timeout 10 minutes (600,000 ms)
        SseEmitter emitter = new SseEmitter(600000L);
        emitters.put(referenceCode, new SseEmitterHolder(emitter));

        // Send an initial ping event to establish the connection immediately
        try {
            emitter.send(SseEmitter.event()
                    .name("ping")
                    .data("Connection established"));
        } catch (IOException e) {
            log.error("❌ [PaymentNotificationService] Failed to send initial ping to SSE client for reference: {}", referenceCode, e);
            emitter.completeWithError(e);
            emitters.remove(referenceCode);
            return emitter;
        }

        emitter.onCompletion(() -> {
            log.info("📶 [PaymentNotificationService] SSE client connection completed for reference: {}", referenceCode);
            emitters.remove(referenceCode);
        });

        emitter.onTimeout(() -> {
            log.info("📶 [PaymentNotificationService] SSE client connection timed out for reference: {}", referenceCode);
            emitters.remove(referenceCode);
        });

        emitter.onError((e) -> {
            log.error("❌ [PaymentNotificationService] SSE connection error for reference: {}", referenceCode, e.getMessage());
            emitters.remove(referenceCode);
        });

        return emitter;
    }

    /**
     * Notify subscribers that payment has completed successfully
     */
    public void notifyPaymentSuccess(String referenceCode, Object paymentInfo) {
        SseEmitterHolder holder = emitters.get(referenceCode);
        if (holder != null) {
            SseEmitter emitter = holder.emitter;
            try {
                log.info("✅ [PaymentNotificationService] Payment success event triggered. Notifying SSE client for reference: {}", referenceCode);
                emitter.send(SseEmitter.event()
                        .name("payment_completed")
                        .data(paymentInfo));
                emitter.complete();
            } catch (Exception e) {
                log.error("❌ [PaymentNotificationService] Failed to notify payment success via SSE for reference: {}", referenceCode, e);
                emitter.completeWithError(e);
            } finally {
                emitters.remove(referenceCode);
            }
        } else {
            log.debug("ℹ️ [PaymentNotificationService] No active SSE emitter found for reference: {}", referenceCode);
        }
    }

    /**
     * Periodic cleanup of stale emitters
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 60000)
    public void cleanupExpiredEmitters() {
        long now = System.currentTimeMillis();
        emitters.entrySet().removeIf(entry -> {
            // If connection exists longer than 11 minutes, complete and remove it
            if (now - entry.getValue().createdAt > 660000L) {
                log.warn("🧹 [PaymentNotificationService] Cleaning up stale SSE connection for reference: {}", entry.getKey());
                try {
                    entry.getValue().emitter.complete();
                } catch (Exception e) {
                    // Ignore
                }
                return true;
            }
            return false;
        });
    }
}
