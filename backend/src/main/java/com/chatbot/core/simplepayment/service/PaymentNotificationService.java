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

    // Manage active SSE connections mapped by referenceCode
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Subscribe to payment event stream for a specific reference code
     */
    public SseEmitter subscribe(String referenceCode) {
        log.info("📶 [PaymentNotificationService] Client subscribing to SSE for payment reference: {}", referenceCode);
        
        // Timeout 10 minutes (600,000 ms)
        SseEmitter emitter = new SseEmitter(600000L);
        emitters.put(referenceCode, emitter);

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
        SseEmitter emitter = emitters.get(referenceCode);
        if (emitter != null) {
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
}
