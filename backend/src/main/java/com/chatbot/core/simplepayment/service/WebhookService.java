package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.core.simplepayment.model.Webhook;
import com.chatbot.core.simplepayment.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
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
    private final RestTemplate restTemplate = new RestTemplate();

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
     * Trigger webhook for payment event
     */
    @Async
    @Transactional(readOnly = true, transactionManager = "sharedTransactionManager")
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
                webhook.recordFailure();
                webhookRepository.save(webhook);
                log.error("❌ Webhook trigger failed: {} - {}", webhook.getName(), e.getMessage());
            }
        }
    }

    /**
     * Send webhook payload
     */
    private void sendWebhook(Webhook webhook, Webhook.WebhookEventType eventType, SimplePayment payment) {
        String payload = buildPayload(eventType, payment);
        String signature = generateSignature(payload, webhook.getSecret());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Webhook-Signature", signature);
        headers.set("X-Webhook-Event", eventType.name());
        headers.set("X-Webhook-ID", UUID.randomUUID().toString());

        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            webhook.getUrl(),
            HttpMethod.POST,
            request,
            String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Webhook returned status: " + response.getStatusCode());
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
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(payload);
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
     * Generate webhook signature
     */
    private String generateSignature(String payload, String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((payload + secret).getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
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
                .orElseThrow(() -> new RuntimeException("Webhook not found: " + id));

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
                .orElseThrow(() -> new RuntimeException("Webhook not found: " + id));

        webhookRepository.delete(webhook);
        log.info("✅ Webhook deleted: {}", webhook.getName());
    }

    /**
     * Test webhook
     */
    public void testWebhook(Long webhookId) {
        log.info("🧪 Testing webhook: {}", webhookId);

        Webhook webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new RuntimeException("Webhook not found: " + webhookId));

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
}
