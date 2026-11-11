package com.chatbot.core.message.store.service;

import com.chatbot.core.tenant.model.TenantWebhook;
import com.chatbot.core.tenant.model.TenantWebhookDelivery;
import com.chatbot.core.tenant.repository.TenantWebhookDeliveryRepository;
import com.chatbot.core.tenant.repository.TenantWebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * Dispatches conversation lifecycle events to tenant-configured outbound
 * webhooks. Best-effort: failures are logged and queued for retry
 * by WebhookDeliveryRetryService (exponential backoff, max 5 attempts).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationWebhookDispatcher {

    private final TenantWebhookRepository webhookRepository;
    private final TenantWebhookDeliveryRepository deliveryRepository;

    private final WebClient webhookWebClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build();

    /**
     * Async dispatch — safe to call from transactional flows.
     */
    @Async
    public void dispatch(Long tenantId, String eventType, Long conversationId,
                         Map<String, Object> payload) {
        try {
            List<TenantWebhook> webhooks = webhookRepository.findByTenantIdAndIsActiveTrue(tenantId);
            if (webhooks.isEmpty()) {
                return;
            }
            for (TenantWebhook webhook : webhooks) {
                if (!webhook.subscribesTo(eventType)) {
                    continue;
                }
                deliver(webhook, eventType, conversationId, payload);
            }
        } catch (Exception e) {
            log.warn("[WebhookDispatch] Failed to dispatch {} for tenant {}: {}",
                    eventType, tenantId, e.getMessage());
        }
    }

    private void deliver(TenantWebhook webhook, String eventType, Long conversationId,
                         Map<String, Object> payload) {
        try {
            String body = buildBody(webhook, eventType, conversationId, payload);
            String signature = sign(body, webhook.getSecret());

            webhookWebClient.post()
                    .uri(webhook.getUrl())
                    .header("Content-Type", "application/json")
                    .header("X-Chatbot-Event", eventType)
                    .header("X-Chatbot-Signature", "sha256=" + signature)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .subscribe(
                            resp -> log.debug("[WebhookDispatch] Delivered {} to {}", eventType, webhook.getUrl()),
                            err -> {
                                log.warn("[WebhookDispatch] Delivery failed {} -> {}: {}",
                                        eventType, webhook.getUrl(), err.getMessage());
                                queueForRetry(webhook, eventType, conversationId, body, err.getMessage());
                            });
        } catch (Exception e) {
            log.warn("[WebhookDispatch] Error delivering {} to {}: {}",
                    eventType, webhook.getUrl(), e.getMessage());
        }
    }

    private void queueForRetry(TenantWebhook webhook, String eventType, Long conversationId,
                               String body, String error) {
        try {
            deliveryRepository.save(TenantWebhookDelivery.builder()
                    .tenantId(webhook.getTenantId())
                    .webhookId(webhook.getId())
                    .eventType(eventType)
                    .conversationId(conversationId)
                    .payload(body)
                    .status(TenantWebhookDelivery.Status.PENDING)
                    .attempts(0)
                    .nextRetryAt(LocalDateTime.now().plusMinutes(1))
                    .lastError(error)
                    .build());
        } catch (Exception e) {
            log.error("[WebhookDispatch] Failed to queue retry for webhook {}: {}", webhook.getId(), e.getMessage());
        }
    }

    private String buildBody(TenantWebhook webhook, String eventType, Long conversationId,
                             Map<String, Object> payload) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"event\":\"").append(eventType).append("\",");
        sb.append("\"conversationId\":").append(conversationId).append(",");
        sb.append("\"tenantId\":").append(webhook.getTenantId()).append(",");
        sb.append("\"timestamp\":\"").append(LocalDateTime.now()).append("\",");
        sb.append("\"data\":{");
        if (payload != null) {
            boolean first = true;
            for (Map.Entry<String, Object> e : payload.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(e.getKey()).append("\":");
                Object v = e.getValue();
                if (v == null) {
                    sb.append("null");
                } else if (v instanceof Number || v instanceof Boolean) {
                    sb.append(v);
                } else {
                    sb.append("\"").append(String.valueOf(v).replace("\"", "\\\"")).append("\"");
                }
            }
        }
        sb.append("}}");
        return sb.toString();
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
            log.warn("[WebhookDispatch] Failed to sign payload: {}", e.getMessage());
            return "";
        }
    }
}
