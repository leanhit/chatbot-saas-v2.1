package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.core.simplepayment.model.Webhook;
import com.chatbot.core.simplepayment.model.WebhookDeadLetter;
import com.chatbot.core.simplepayment.repository.WebhookRepository;
import com.chatbot.core.simplepayment.repository.WebhookDeadLetterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final WebhookRepository webhookRepository;
    private final WebhookDeadLetterRepository webhookDeadLetterRepository;
    private final WebhookSignatureService webhookSignatureService;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    /**
     * Create new webhook
     */
    @Transactional("sharedTransactionManager")
    public Webhook createWebhook(Webhook webhook) {
        log.info("🪝 Creating webhook: {}", webhook.getName());

        // Check if URL already exists
        if (webhookRepository.existsByUrl(webhook.getUrl())) {
            throw new IllegalArgumentException("Webhook URL already exists: " + webhook.getUrl());
        }

        // Generate secret if not provided
        if (webhook.getSecret() == null || webhook.getSecret().isBlank()) {
            webhook.setSecret(generateSecret());
        }

        Webhook saved = webhookRepository.save(webhook);
        log.info("✅ Webhook created: {}", saved.getName());
        return saved;
    }

    /**
     * Trigger webhook for payment event with exponential backoff retry
     */
    @Async
    @Transactional(transactionManager = "sharedTransactionManager")
    public void triggerWebhook(Webhook.WebhookEventType eventType, SimplePayment payment) {
        log.info("🪝 Triggering webhooks for event: {}, payment: {}", eventType, payment.getReferenceCode());

        List<Webhook> webhooks = webhookRepository.findActiveWebhooksForEvent(eventType);

        for (Webhook webhook : webhooks) {
            try {
                sendWebhook(webhook, eventType, payment);
                webhook.recordSuccess();
                webhookRepository.save(webhook);
                log.info("✅ Webhook triggered successfully: {}", webhook.getName());
            } catch (Exception e) {
                String errorMessage = e.getMessage();
                webhook.recordFailure(errorMessage);
                webhookRepository.save(webhook);
                log.error("❌ Webhook trigger failed: {} - Attempt: {}/{} - Error: {}", 
                    webhook.getName(), webhook.getCurrentRetryAttempt(), webhook.getRetryCount(), errorMessage);
                
                // Schedule retry if still within retry limit
                if (webhook.canRetry()) {
                    scheduleWebhookRetry(webhook, eventType, payment);
                } else {
                    log.error("🚨 Webhook failed after max retries: {}, moving to dead letter queue", webhook.getName());
                    moveToDeadLetterQueue(webhook, eventType, payment, errorMessage);
                }
            }
        }
    }

    /**
     * Schedule webhook retry with exponential backoff
     */
    @Async
    public void scheduleWebhookRetry(Webhook webhook, Webhook.WebhookEventType eventType, SimplePayment payment) {
        log.info("⏰ Scheduling webhook retry: {}, attempt: {}, next retry at: {}", 
            webhook.getName(), webhook.getCurrentRetryAttempt(), webhook.getNextRetryAt());
        
        // In production, use a proper scheduler like Quartz or Spring @Scheduled
        // For now, we'll use a simple delay
        try {
            long delayMs = java.time.Duration.between(
                LocalDateTime.now(), 
                webhook.getNextRetryAt()
            ).toMillis();
            
            if (delayMs > 0) {
                Thread.sleep(delayMs);
            }
            
            // Retry the webhook
            triggerWebhook(eventType, payment);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Webhook retry interrupted: {}", webhook.getName(), e);
        }
    }

    /**
     * Send webhook payload with signature validation
     */
    private void sendWebhook(Webhook webhook, Webhook.WebhookEventType eventType, SimplePayment payment) {
        String payload = buildPayload(eventType, payment);
        String signature = webhookSignatureService.generateSignature(payload);

        try {
            String response = webClient.post()
                .uri(webhook.getUrl())
                .headers(h -> {
                    h.setContentType(MediaType.APPLICATION_JSON);
                    h.set("X-Webhook-Signature", signature);
                    h.set("X-Webhook-Event", eventType.name());
                    h.set("X-Webhook-ID", UUID.randomUUID().toString());
                })
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (Exception e) {
            throw new RuntimeException("Webhook request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Build webhook payload
     */
    private String buildPayload(Webhook.WebhookEventType eventType, SimplePayment payment) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", eventType.name());
        payload.put("timestamp", LocalDateTime.now().toString());
        payload.put("data", buildPaymentData(payment));
        
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build webhook payload", e);
        }
    }

    /**
     * Build payment data for webhook
     */
    private Map<String, Object> buildPaymentData(SimplePayment payment) {
        Map<String, Object> data = new HashMap<>();
        data.put("referenceCode", payment.getReferenceCode());
        data.put("amount", payment.getAmount());
        data.put("currency", payment.getCurrency());
        data.put("status", payment.getStatus().name());
        data.put("userId", payment.getUserId());
        data.put("tenantId", payment.getTenantId());
        data.put("targetPackageId", payment.getTargetPackageId());
        data.put("createdAt", payment.getCreatedAt().toString());
        data.put("completedAt", payment.getCompletedAt() != null ? payment.getCompletedAt().toString() : null);
        return data;
    }

    /**
     * Generate random secret
     */
    private String generateSecret() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Get all active webhooks
     */
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
    public List<Webhook> getActiveWebhooks() {
        return webhookRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    /**
     * Update webhook
     */
    @Transactional("sharedTransactionManager")
    public Webhook updateWebhook(Long id, Webhook webhook) {
        log.info("🔄 Updating webhook: {}", id);

        Webhook existing = webhookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook not found: " + id));

        existing.setName(webhook.getName());
        existing.setUrl(webhook.getUrl());
        existing.setSecret(webhook.getSecret());
        existing.setIsActive(webhook.getIsActive());
        existing.setEventTypes(webhook.getEventTypes());
        existing.setRetryCount(webhook.getRetryCount());
        existing.setTimeoutSeconds(webhook.getTimeoutSeconds());
        existing.setDescription(webhook.getDescription());

        Webhook updated = webhookRepository.save(existing);
        log.info("✅ Webhook updated: {}", updated.getName());
        return updated;
    }

    /**
     * Delete webhook
     */
    @Transactional("sharedTransactionManager")
    public void deleteWebhook(Long id) {
        log.info("🗑️ Deleting webhook: {}", id);

        Webhook webhook = webhookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook not found: " + id));

        webhookRepository.delete(webhook);
        log.info("✅ Webhook deleted: {}", webhook.getName());
    }

    /**
     * Test webhook
     */
    public void testWebhook(Long webhookId) {
        log.info("🧪 Testing webhook: {}", webhookId);

        Webhook webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook not found: " + webhookId));

        // Create test payment
        SimplePayment testPayment = SimplePayment.builder()
                .referenceCode("TEST-" + UUID.randomUUID().toString().substring(0, 8))
                .amount(java.math.BigDecimal.valueOf(1000))
                .currency("VND")
                .status(com.chatbot.core.simplepayment.model.PaymentStatus.COMPLETED)
                .userId(0L)
                .tenantId(0L)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        try {
            sendWebhook(webhook, Webhook.WebhookEventType.PAYMENT_COMPLETED, testPayment);
            log.info("✅ Webhook test successful: {}", webhook.getName());
        } catch (Exception e) {
            log.error("❌ Webhook test failed: {} - {}", webhook.getName(), e.getMessage());
            throw new RuntimeException("Webhook test failed: " + e.getMessage());
        }
    }

    /**
     * Move failed webhook to dead letter queue
     */
    @Transactional("sharedTransactionManager")
    private void moveToDeadLetterQueue(Webhook webhook, Webhook.WebhookEventType eventType, SimplePayment payment, String errorMessage) {
        try {
            String payload = buildPayload(eventType, payment);
            
            WebhookDeadLetter deadLetter = WebhookDeadLetter.builder()
                .webhookId(webhook.getId())
                .webhookName(webhook.getName())
                .webhookUrl(webhook.getUrl())
                .eventType(eventType.name())
                .paymentReferenceCode(payment.getReferenceCode())
                .retryAttempts(webhook.getCurrentRetryAttempt())
                .payload(payload)
                .lastError(errorMessage)
                .status("PENDING")
                .build();
            
            webhookDeadLetterRepository.save(deadLetter);
            log.info("📦 Webhook moved to dead letter queue: {}, payment: {}", webhook.getName(), payment.getReferenceCode());
            
        } catch (Exception e) {
            log.error("Failed to move webhook to dead letter queue: {}", webhook.getName(), e);
        }
    }
}
