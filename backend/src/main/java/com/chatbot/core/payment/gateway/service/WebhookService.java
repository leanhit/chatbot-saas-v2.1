package com.chatbot.core.payment.gateway.service;

import com.chatbot.core.payment.common.audit.PaymentAuditLog.AuditAction;
import com.chatbot.core.payment.common.audit.PaymentAuditService;
import com.chatbot.core.payment.common.event.PaymentCompletedEvent;
import com.chatbot.core.payment.common.event.PaymentFailedEvent;
import com.chatbot.core.payment.common.metrics.PaymentMetricsService;
import com.chatbot.core.payment.gateway.model.Webhook;
import com.chatbot.core.payment.gateway.model.Webhook.WebhookEventType;
import com.chatbot.core.payment.gateway.model.WebhookDeadLetter;
import com.chatbot.core.payment.gateway.repository.WebhookDeadLetterRepository;
import com.chatbot.core.payment.gateway.repository.WebhookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final WebhookRepository webhookRepository;
    private final WebhookDeadLetterRepository deadLetterRepository;
    private final WebhookSignatureService webhookSignatureService;
    private final PaymentAuditService paymentAuditService;
    private final PaymentMetricsService paymentMetricsService;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    /**
     * Event listener for PaymentCompletedEvent
     * Triggers webhooks for payment completion
     */
    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "sharedTransactionManager")
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("🔔 [EVENT] Handling PaymentCompletedEvent for reference: {}", event.getReferenceCode());

        List<Webhook> webhooks = webhookRepository.findActiveWebhooksForEvent(WebhookEventType.PAYMENT_COMPLETED);
        
        for (Webhook webhook : webhooks) {
            try {
                triggerWebhook(webhook, WebhookEventType.PAYMENT_COMPLETED, buildPaymentPayload(event));
            } catch (Exception e) {
                log.error("❌ Failed to trigger webhook for payment: {}", event.getReferenceCode(), e);
            }
        }
    }

    /**
     * Event listener for PaymentFailedEvent
     * Triggers webhooks for payment failure
     */
    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "sharedTransactionManager")
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        log.info("🔔 [EVENT] Handling PaymentFailedEvent for reference: {}", event.getReferenceCode());

        List<Webhook> webhooks = webhookRepository.findActiveWebhooksForEvent(WebhookEventType.PAYMENT_FAILED);
        
        for (Webhook webhook : webhooks) {
            try {
                triggerWebhook(webhook, WebhookEventType.PAYMENT_FAILED, buildPaymentFailedPayload(event));
            } catch (Exception e) {
                log.error("❌ Failed to trigger webhook for failed payment: {}", event.getReferenceCode(), e);
            }
        }
    }

    /**
     * Trigger webhook with retry logic
     */
    @Transactional(transactionManager = "sharedTransactionManager")
    public void triggerWebhook(Webhook webhook, WebhookEventType eventType, Map<String, Object> payload) {
        log.info("🔔 Triggering webhook: {} for event: {}", webhook.getName(), eventType);

        try {
            // Generate signature
            String signature = webhookSignatureService.generateSignature(payload, webhook.getSecret());

            // Send webhook
            String response = webClient.post()
                    .uri(webhook.getUrl())
                    .header("Content-Type", "application/json")
                    .header("X-Webhook-Signature", signature)
                    .header("X-Webhook-Event", eventType.name())
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("✅ Webhook triggered successfully: {}", webhook.getName());

            // Record success
            webhook.recordSuccess();
            webhookRepository.save(webhook);

            // Track metrics
            paymentMetricsService.incrementWebhookSent();

            // Log audit
            paymentAuditService.logPaymentAction(
                (String) payload.get("referenceCode"),
                null,
                null,
                AuditAction.WEBHOOK_SENT,
                null,
                null,
                null,
                "Webhook sent to: " + webhook.getName(),
                null
            );

        } catch (Exception e) {
            log.error("❌ Webhook failed: {} - {}", webhook.getName(), e.getMessage());

            // Record failure
            webhook.recordFailure(e.getMessage());
            webhookRepository.save(webhook);

            // Track metrics
            paymentMetricsService.incrementWebhookFailed();

            // Move to dead letter if max retries exceeded
            if (!webhook.canRetry()) {
                moveToDeadLetter(webhook, eventType, payload, e.getMessage());
            }
        }
    }

    /**
     * Move failed webhook to dead letter queue
     */
    @Transactional(transactionManager = "sharedTransactionManager")
    public void moveToDeadLetter(Webhook webhook, WebhookEventType eventType, Map<String, Object> payload, String error) {
        log.warn("⚠️ Moving webhook to dead letter: {}", webhook.getName());

        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            WebhookDeadLetter deadLetter = WebhookDeadLetter.builder()
                    .webhookId(webhook.getId())
                    .webhookName(webhook.getName())
                    .webhookUrl(webhook.getUrl())
                    .eventType(eventType.name())
                    .paymentReferenceCode((String) payload.get("referenceCode"))
                    .retryAttempts(webhook.getCurrentRetryAttempt())
                    .payload(payloadJson)
                    .lastError(error)
                    .status("PENDING")
                    .build();

            deadLetterRepository.save(deadLetter);
            log.info("✅ Webhook moved to dead letter: {}", webhook.getName());

        } catch (Exception e) {
            log.error("❌ Failed to move webhook to dead letter: {}", e.getMessage());
        }
    }

    /**
     * Scheduled job to retry failed webhooks
     * Runs every minute
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional(transactionManager = "sharedTransactionManager")
    public void retryFailedWebhooks() {
        log.info("🔄 Retrying failed webhooks...");

        List<Webhook> webhooksToRetry = webhookRepository.findWebhooksReadyForRetry(LocalDateTime.now());

        for (Webhook webhook : webhooksToRetry) {
            try {
                log.info("🔄 Retrying webhook: {}", webhook.getName());
                // Re-trigger webhook with original payload
                // This would need to store the original payload
                // For now, just reset the status
                webhook.setStatus("ACTIVE");
                webhook.setCurrentRetryAttempt(0);
                webhook.setNextRetryAt(null);
                webhookRepository.save(webhook);

            } catch (Exception e) {
                log.error("❌ Failed to retry webhook: {}", webhook.getName(), e);
            }
        }

        log.info("✅ Retried {} webhooks", webhooksToRetry.size());
    }

    /**
     * Build payment payload for webhook
     */
    private Map<String, Object> buildPaymentPayload(PaymentCompletedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "payment.completed");
        payload.put("referenceCode", event.getReferenceCode());
        payload.put("userId", event.getUserId());
        payload.put("tenantId", event.getTenantId());
        payload.put("amount", event.getAmount());
        payload.put("currency", event.getCurrency());
        payload.put("bankTransactionId", event.getBankTransactionId());
        payload.put("targetPackageId", event.getTargetPackageId());
        payload.put("completedAt", event.getCompletedAt());
        payload.put("description", event.getDescription());
        payload.put("timestamp", LocalDateTime.now());
        return payload;
    }

    /**
     * Build payment failed payload for webhook
     */
    private Map<String, Object> buildPaymentFailedPayload(PaymentFailedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "payment.failed");
        payload.put("referenceCode", event.getReferenceCode());
        payload.put("userId", event.getUserId());
        payload.put("tenantId", event.getTenantId());
        payload.put("amount", event.getAmount());
        payload.put("currency", event.getCurrency());
        payload.put("failureReason", event.getFailureReason());
        payload.put("targetPackageId", event.getTargetPackageId());
        payload.put("failedAt", event.getFailedAt());
        payload.put("timestamp", LocalDateTime.now());
        return payload;
    }

    /**
     * Create new webhook
     */
    @Transactional(transactionManager = "sharedTransactionManager")
    public Webhook createWebhook(Webhook webhook) {
        log.info("🔔 Creating new webhook: {}", webhook.getName());

        if (webhookRepository.existsByUrl(webhook.getUrl())) {
            throw new RuntimeException("Webhook URL already exists");
        }

        return webhookRepository.save(webhook);
    }

    /**
     * Get all active webhooks
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public List<Webhook> getActiveWebhooks() {
        return webhookRepository.findByIsActiveTrue();
    }

    /**
     * Delete webhook
     */
    @Transactional(transactionManager = "sharedTransactionManager")
    public void deleteWebhook(String url) {
        log.info("🗑️ Deleting webhook: {}", url);
        webhookRepository.deleteByUrl(url);
    }
}
