package com.chatbot.core.payment.merchant.service;

import com.chatbot.core.payment.common.event.MerchantPaymentCompletedEvent;
import com.chatbot.core.payment.merchant.model.MerchantApiKey;
import com.chatbot.core.payment.merchant.model.MerchantPaymentSession;
import com.chatbot.core.payment.merchant.repository.MerchantApiKeyRepository;
import com.chatbot.core.payment.merchant.repository.MerchantPaymentSessionRepository;
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
public class MerchantWebhookDispatcher {

    private final MerchantPaymentSessionRepository sessionRepository;
    private final MerchantApiKeyRepository apiKeyRepository;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    /**
     * Event listener for MerchantPaymentCompletedEvent
     * Dispatches webhook to merchant with retry logic
     */
    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "sharedTransactionManager")
    public void handleMerchantPaymentCompletedEvent(MerchantPaymentCompletedEvent event) {
        log.info("🔔 [EVENT] Dispatching webhook for merchant payment: {}", event.getPaymentCode());

        try {
            MerchantPaymentSession session = sessionRepository.findBySessionId(event.getPaymentCode())
                    .orElseThrow(() -> new RuntimeException("Session not found: " + event.getPaymentCode()));

            MerchantApiKey merchantKey = apiKeyRepository.findById(session.getMerchantId())
                    .orElseThrow(() -> new RuntimeException("Merchant not found: " + session.getMerchantId()));

            if (merchantKey.getWebhookUrl() == null || merchantKey.getWebhookUrl().trim().isEmpty()) {
                log.warn("⚠️ No webhook URL configured for merchant: {}", merchantKey.getName());
                return;
            }

            dispatchWebhook(session, merchantKey);

        } catch (Exception e) {
            log.error("❌ Failed to dispatch webhook for session: {}", event.getPaymentCode(), e);
        }
    }

    /**
     * Dispatch webhook to merchant with retry logic
     */
    @Transactional(transactionManager = "sharedTransactionManager")
    public void dispatchWebhook(MerchantPaymentSession session, MerchantApiKey merchantKey) {
        log.info("🔔 Dispatching webhook to: {}", merchantKey.getWebhookUrl());

        try {
            // Build webhook payload
            Map<String, Object> payload = buildWebhookPayload(session);

            // Generate HMAC signature
            String signature = generateHmacSignature(payload, merchantKey.getWebhookSecret());

            // Send webhook
            String response = webClient.post()
                    .uri(merchantKey.getWebhookUrl())
                    .header("Content-Type", "application/json")
                    .header("X-Merchant-Signature", signature)
                    .header("X-Merchant-Event", "payment.completed")
                    .header("X-Merchant-Session-Id", session.getSessionId())
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("✅ Webhook sent successfully to: {}", merchantKey.getWebhookUrl());

            // Update session
            session.setWebhookStatus("SENT");
            session.setWebhookSentAt(LocalDateTime.now());
            sessionRepository.save(session);

        } catch (Exception e) {
            log.error("❌ Webhook failed: {} - {}", merchantKey.getWebhookUrl(), e.getMessage());

            // Update session with failure
            session.setWebhookStatus("FAILED");
            session.setWebhookRetryCount(session.getWebhookRetryCount() + 1);
            sessionRepository.save(session);

            // Calculate next retry time with exponential backoff
            if (session.getWebhookRetryCount() < 5) {
                scheduleRetry(session, merchantKey);
            } else {
                log.error("❌ Max retries exceeded for session: {}", session.getSessionId());
                session.setWebhookStatus("FAILED_MAX_RETRIES");
                sessionRepository.save(session);
            }
        }
    }

    /**
     * Schedule retry with exponential backoff
     */
    private void scheduleRetry(MerchantPaymentSession session, MerchantApiKey merchantKey) {
        long delayMs = (long) Math.pow(2, session.getWebhookRetryCount()) * 1000; // 2^attempt * 1 second
        long maxDelayMs = 60000; // Max 60 seconds
        delayMs = Math.min(delayMs, maxDelayMs);

        LocalDateTime nextRetryAt = LocalDateTime.now().plus(java.time.Duration.ofMillis(delayMs));
        log.info("🔄 Scheduling webhook retry for session: {} at: {}", session.getSessionId(), nextRetryAt);

        // In production, this would use a scheduled task or message queue
        // For now, we'll use a scheduled job to check for retries
    }

    /**
     * Scheduled job to retry failed webhooks
     * Runs every minute
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional(transactionManager = "sharedTransactionManager")
    public void retryFailedWebhooks() {
        log.info("🔄 Retrying failed merchant webhooks...");

        List<MerchantPaymentSession> sessionsToRetry = sessionRepository.findFailedWebhooksForRetry();

        for (MerchantPaymentSession session : sessionsToRetry) {
            try {
                MerchantApiKey merchantKey = apiKeyRepository.findById(session.getMerchantId())
                        .orElse(null);

                if (merchantKey != null && merchantKey.getWebhookUrl() != null) {
                    dispatchWebhook(session, merchantKey);
                }

            } catch (Exception e) {
                log.error("❌ Failed to retry webhook for session: {}", session.getSessionId(), e);
            }
        }

        log.info("✅ Retried {} merchant webhooks", sessionsToRetry.size());
    }

    /**
     * Build webhook payload
     */
    private Map<String, Object> buildWebhookPayload(MerchantPaymentSession session) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "payment.completed");
        payload.put("sessionId", session.getSessionId());
        payload.put("merchantOrderId", session.getMerchantOrderId());
        payload.put("amount", session.getAmount());
        payload.put("currency", session.getCurrency());
        payload.put("description", session.getDescription());
        payload.put("status", session.getStatus().name());
        payload.put("paymentReferenceCode", session.getPaymentReferenceCode());
        payload.put("bankTransactionId", session.getBankTransactionId());
        payload.put("completedAt", session.getCompletedAt());
        payload.put("metadata", session.getMetadata());
        payload.put("timestamp", LocalDateTime.now());
        return payload;
    }

    /**
     * Generate HMAC-SHA256 signature
     */
    private String generateHmacSignature(Map<String, Object> payload, String secret) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            return hmacSha256(payloadJson, secret);
        } catch (Exception e) {
            log.error("❌ Failed to generate signature", e);
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    /**
     * HMAC-SHA256 implementation
     */
    private String hmacSha256(String data, String key) throws java.security.NoSuchAlgorithmException, java.security.InvalidKeyException {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(key.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacData = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return java.util.Base64.getEncoder().encodeToString(hmacData);
    }
}
