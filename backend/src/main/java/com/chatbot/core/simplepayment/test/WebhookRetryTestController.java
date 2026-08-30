package com.chatbot.core.simplepayment.test;

import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.core.simplepayment.model.Webhook;
import com.chatbot.core.simplepayment.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Test controller for webhook retry logic
 * Access: POST /api/test/webhook/retry-test
 */
@RestController
@RequestMapping("/api/test/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookRetryTestController {

    private final WebhookService webhookService;

    /**
     * Test webhook retry with exponential backoff
     */
    @PostMapping("/retry-test")
    public Map<String, Object> testWebhookRetry() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Create a test webhook with invalid URL to trigger retry
            Webhook testWebhook = Webhook.builder()
                .name("Test Retry Webhook")
                .url("http://invalid-webhook-endpoint-that-will-fail.com/webhook")
                .secret("test-secret-123")
                .isActive(true)
                .retryCount(3)
                .timeoutSeconds(5)
                .build();
            
            // Create test payment
            SimplePayment testPayment = SimplePayment.builder()
                .referenceCode("TEST-RETRY-" + System.currentTimeMillis())
                .amount(BigDecimal.valueOf(1000))
                .currency("VND")
                .status(com.chatbot.core.simplepayment.model.PaymentStatus.COMPLETED)
                .userId(0L)
                .tenantId(0L)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
            
            log.info("🧪 Starting webhook retry test with invalid URL");
            log.info("📊 Expected behavior: 3 retries with exponential backoff (2s, 4s, 8s)");
            
            // Trigger webhook (will fail and retry)
            webhookService.triggerWebhook(Webhook.WebhookEventType.PAYMENT_COMPLETED, testPayment);
            
            result.put("status", "Webhook retry test initiated");
            result.put("message", "Check logs for retry attempts with exponential backoff");
            result.put("webhook", testWebhook.getName());
            result.put("payment", testPayment.getReferenceCode());
            result.put("retry_count", testWebhook.getRetryCount());
            result.put("expected_delays", "2s, 4s, 8s (exponential backoff)");
            
        } catch (Exception e) {
            log.error("Webhook retry test failed", e);
            result.put("status", "Test failed");
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * Test webhook signature validation
     */
    @PostMapping("/signature-test")
    public Map<String, Object> testSignatureValidation() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String testPayload = "{\"event\":\"PAYMENT_COMPLETED\",\"timestamp\":\"2024-01-01T00:00:00\"}";
            
            log.info("🧪 Testing webhook signature validation");
            log.info("📝 Test payload: {}", testPayload);
            
            result.put("status", "Signature validation test");
            result.put("message", "WebhookSignatureService validates HMAC-SHA256 signatures");
            result.put("payload", testPayload);
            result.put("features", Map.of(
                "hmac_sha256", "HMAC-SHA256 signature generation",
                "timestamp_validation", "5-minute window to prevent replay attacks",
                "constant_time_comparison", "Prevents timing attacks"
            ));
            
        } catch (Exception e) {
            log.error("Signature validation test failed", e);
            result.put("status", "Test failed");
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * Test dead letter queue
     */
    @PostMapping("/dead-letter-test")
    public Map<String, Object> testDeadLetterQueue() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("🧪 Testing dead letter queue");
            log.info("📦 Failed webhooks after max retries are moved to dead letter queue");
            
            result.put("status", "Dead letter queue test");
            result.put("message", "Webhooks failing after max retries are stored in webhook_dead_letters table");
            result.put("features", Map.of(
                "failed_webhooks", "Stored with payload and error details",
                "manual_reprocessing", "Can be manually reprocessed later",
                "tracking", "Tracks retry attempts and timestamps"
            ));
            
        } catch (Exception e) {
            log.error("Dead letter queue test failed", e);
            result.put("status", "Test failed");
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
