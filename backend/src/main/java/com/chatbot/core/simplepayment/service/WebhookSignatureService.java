package com.chatbot.core.simplepayment.service;

import jakarta.servlet.http.HttpServletRequest;
import com.chatbot.shared.exceptions.WebhookValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookSignatureService {

    @Value("${simplepayment.webhook.signature-secret:dev-webhook-secret}")
    private String webhookSecret;

    /**
     * Verify webhook signature from request
     * @param request HTTP request
     * @param body Request body as string
     * @return true if signature is valid
     */
    public boolean verifySignature(HttpServletRequest request, String body) {
        try {
            String signatureHeader = request.getHeader("X-Webhook-Signature");
            if (signatureHeader == null || signatureHeader.isEmpty()) {
                log.warn("Webhook signature header missing");
                return false;
            }

            String timestampHeader = request.getHeader("X-Webhook-Timestamp");
            if (timestampHeader == null || timestampHeader.isEmpty()) {
                log.warn("Webhook timestamp header missing");
                return false;
            }

            // Verify timestamp is within 5 minutes to prevent replay attacks
            long timestamp = Long.parseLong(timestampHeader);
            long currentTime = System.currentTimeMillis() / 1000;
            if (Math.abs(currentTime - timestamp) > 300) {
                log.warn("Webhook timestamp too old or in the future: {}", timestamp);
                return false;
            }

            // Extract signature from header (format: sha256=<signature>)
            String expectedSignature = signatureHeader;
            if (signatureHeader.startsWith("sha256=")) {
                expectedSignature = signatureHeader.substring(7);
            }

            // Generate signature
            String payload = timestampHeader + "." + body;
            String computedSignature = generateHmacSha256(payload, webhookSecret);

            // Compare signatures
            boolean isValid = constantTimeEquals(expectedSignature, computedSignature);
            
            if (!isValid) {
                log.warn("Webhook signature verification failed. Expected: {}, Computed: {}", 
                    expectedSignature, computedSignature);
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    /**
     * Generate HMAC SHA256 signature
     */
    private String generateHmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8), 
            "HmacSHA256"
        );
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Constant time comparison to prevent timing attacks
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }

        return result == 0;
    }

    /**
     * Generate signature for outgoing webhooks
     */
    public String generateSignature(String payload) {
        try {
            long timestamp = System.currentTimeMillis() / 1000;
            String data = timestamp + "." + payload;
            String signature = generateHmacSha256(data, webhookSecret);
            return "sha256=" + signature;
        } catch (Exception e) {
            log.error("Error generating webhook signature", e);
            throw WebhookValidationException.signatureError("Failed to generate webhook signature: " + e.getMessage());
        }
    }
}
