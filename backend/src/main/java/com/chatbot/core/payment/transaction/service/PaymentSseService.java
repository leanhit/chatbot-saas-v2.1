package com.chatbot.core.payment.transaction.service;

import com.chatbot.core.payment.common.event.PaymentCompletedEvent;
import com.chatbot.core.payment.transaction.dto.PaymentStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSseService {

    private final Map<String, List<SseEmitter>> emittersMap = new ConcurrentHashMap<>();

    /**
     * Subscribe to payment status events via Server-Sent Events (SSE)
     */
    public SseEmitter subscribe(String referenceCode) {
        log.info("📶 [SSE] Client subscribed to payment events for reference: {}", referenceCode);

        // 15 minutes timeout for SSE stream
        SseEmitter emitter = new SseEmitter(15 * 60 * 1000L);

        emittersMap.computeIfAbsent(referenceCode, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(referenceCode, emitter));
        emitter.onTimeout(() -> removeEmitter(referenceCode, emitter));
        emitter.onError((e) -> removeEmitter(referenceCode, emitter));

        // Send initial connected event
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of(
                            "status", "CONNECTED",
                            "referenceCode", referenceCode,
                            "message", "Subscribed to payment status updates"
                    )));
        } catch (IOException e) {
            log.warn("⚠️ [SSE] Failed to send initial connection event for: {}", referenceCode);
            removeEmitter(referenceCode, emitter);
        }

        return emitter;
    }

    /**
     * Broadcast payment completion event to all listening SSE clients
     */
    public void emitPaymentCompleted(String referenceCode, PaymentStatusResponse response) {
        List<SseEmitter> emitters = emittersMap.get(referenceCode);
        if (emitters != null && !emitters.isEmpty()) {
            log.info("📶 [SSE] Broadcasting payment_completed event for: {} to {} subscribers", referenceCode, emitters.size());
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("payment_completed")
                            .data(response));
                    emitter.complete();
                } catch (Exception e) {
                    log.warn("⚠️ [SSE] Error pushing event to subscriber for: {}", referenceCode);
                }
            }
            emittersMap.remove(referenceCode);
        } else {
            log.info("ℹ️ [SSE] No active subscribers for reference: {}", referenceCode);
        }
    }

    /**
     * Listen to Spring PaymentCompletedEvent and emit SSE
     */
    @Async
    @EventListener
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("📶 [SSE EVENT] Received PaymentCompletedEvent for: {}", event.getReferenceCode());
        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setReferenceCode(event.getReferenceCode());
        response.setStatus("COMPLETED");
        response.setAmount(event.getAmount());
        response.setCurrency(event.getCurrency());
        response.setBankTransactionId(event.getBankTransactionId());
        response.setTargetPackageId(event.getTargetPackageId());
        response.setCompletedAt(event.getCompletedAt());
        response.setDescription(event.getDescription());
        response.withFormattedDates();

        emitPaymentCompleted(event.getReferenceCode(), response);
    }

    private void removeEmitter(String referenceCode, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersMap.get(referenceCode);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                emittersMap.remove(referenceCode);
            }
        }
    }
}
