package com.chatbot.core.message.store.service;

import com.chatbot.core.tenant.model.TenantWebhook;
import com.chatbot.core.tenant.model.TenantWebhookDelivery;
import com.chatbot.core.tenant.repository.TenantWebhookDeliveryRepository;
import com.chatbot.core.tenant.repository.TenantWebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

/**
 * Retries failed tenant webhook deliveries with exponential backoff.
 * Max 5 attempts: 1m, 5m, 15m, 1h, 6h → then FAILED (DLQ).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryRetryService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long[] BACKOFF_MINUTES = {1, 5, 15, 60, 360};

    private final TenantWebhookDeliveryRepository deliveryRepository;
    private final TenantWebhookRepository webhookRepository;

    private final WebClient webClient = WebClient.builder().build();

    @Scheduled(fixedDelay = 120_000) // every 2 minutes
    @SchedulerLock(name = "WebhookDeliveryRetryService_retry", lockAtMostFor = "5m", lockAtLeastFor = "1m")
    @Transactional(value = "tenantTransactionManager")
    public void retryPendingDeliveries() {
        List<TenantWebhookDelivery> due = deliveryRepository.findPendingDue(LocalDateTime.now());
        if (due.isEmpty()) {
            return;
        }
        log.info("[WebhookRetry] Processing {} pending deliveries", due.size());
        for (TenantWebhookDelivery delivery : due) {
            try {
                processDelivery(delivery);
            } catch (Exception e) {
                log.error("[WebhookRetry] Error processing delivery {}: {}", delivery.getId(), e.getMessage());
            }
        }
    }

    private void processDelivery(TenantWebhookDelivery delivery) {
        TenantWebhook webhook = webhookRepository.findById(delivery.getWebhookId()).orElse(null);
        if (webhook == null || !Boolean.TRUE.equals(webhook.getIsActive())) {
            delivery.setStatus(TenantWebhookDelivery.Status.FAILED);
            delivery.setLastError("Webhook deleted or inactive");
            deliveryRepository.save(delivery);
            return;
        }

        int attempt = delivery.getAttempts() + 1;
        delivery.setAttempts(attempt);

        try {
            String signature = sign(delivery.getPayload(), webhook.getSecret());
            webClient.post()
                    .uri(webhook.getUrl())
                    .header("Content-Type", "application/json")
                    .header("X-Chatbot-Event", delivery.getEventType())
                    .header("X-Chatbot-Signature", "sha256=" + signature)
                    .header("X-Chatbot-Retry", String.valueOf(attempt))
                    .bodyValue(delivery.getPayload())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            delivery.setStatus(TenantWebhookDelivery.Status.DELIVERED);
            delivery.setDeliveredAt(LocalDateTime.now());
            delivery.setLastError(null);
            log.info("[WebhookRetry] Delivered {} to webhook {} on attempt {}",
                    delivery.getEventType(), webhook.getId(), attempt);
        } catch (Exception e) {
            delivery.setLastError(e.getMessage());
            if (attempt >= MAX_ATTEMPTS) {
                delivery.setStatus(TenantWebhookDelivery.Status.FAILED);
                log.warn("[WebhookRetry] Delivery {} permanently failed after {} attempts",
                        delivery.getId(), attempt);
            } else {
                long backoff = BACKOFF_MINUTES[Math.min(attempt - 1, BACKOFF_MINUTES.length - 1)];
                delivery.setNextRetryAt(LocalDateTime.now().plusMinutes(backoff));
                log.info("[WebhookRetry] Delivery {} attempt {} failed, next retry in {}m",
                        delivery.getId(), attempt, backoff);
            }
        }
        deliveryRepository.save(delivery);
    }

    private String sign(String body, String secret) {
        if (secret == null || secret.isBlank()) {
            return "";
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return "";
        }
    }
}
