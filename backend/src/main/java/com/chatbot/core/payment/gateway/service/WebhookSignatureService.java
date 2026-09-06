package com.chatbot.core.payment.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookSignatureService {

    private final ObjectMapper objectMapper;

    /**
     * Generate HMAC-SHA256 signature for webhook payload
     */
    public String generateSignature(Map<String, Object> payload, String secret) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            String signature = hmacSha256(payloadJson, secret);
            return signature;
        } catch (Exception e) {
            log.error("❌ Failed to generate signature", e);
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    /**
     * Verify webhook signature
     */
    public boolean verifySignature(Map<String, Object> payload, String signature, String secret) {
        try {
            String expectedSignature = generateSignature(payload, secret);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            log.error("❌ Failed to verify signature", e);
            return false;
        }
    }

    /**
     * HMAC-SHA256 implementation
     */
    private String hmacSha256(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacData = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmacData);
    }
}
