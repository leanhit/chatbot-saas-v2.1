package com.chatbot.shared.penny.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * ApiKeyManager - Secure management of API keys for external services
 * 
 * Handles encryption/decryption of API keys stored in database or configuration.
 * Uses AES encryption for secure storage.
 */
@Service
@Slf4j
public class ApiKeyManager {

    private static final String AES = "AES";
    
    @Value("${penny.api-key.encryption.secret:#{null}}")
    private String encryptionSecretKey;

    @Value("${penny.openai.api-key:}")
    private String openaiApiKey;

    @Value("${penny.anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${penny.api-key.encryption.enabled:true}")
    private boolean encryptionEnabled;

    @Value("${penny.gemini.api-key:}")
    private String geminiApiKey;

    /**
     * Get OpenAI API key
     */
    public String getOpenAiApiKey() {
        if (!StringUtils.hasText(openaiApiKey)) {
            log.warn("OpenAI API key not configured");
            return null;
        }
        return encryptionEnabled ? decrypt(openaiApiKey) : openaiApiKey;
    }

    /**
     * Get Anthropic API key
     */
    public String getAnthropicApiKey() {
        if (!StringUtils.hasText(anthropicApiKey)) {
            log.warn("Anthropic API key not configured");
            return null;
        }
        return encryptionEnabled ? decrypt(anthropicApiKey) : anthropicApiKey;
    }

    /**
     * Alias for getAnthropicApiKey
     */
    public String getClaudeApiKey() {
        return getAnthropicApiKey();
    }

    /**
     * Get Gemini API key
     */
    public String getGeminiApiKey() {
        if (!StringUtils.hasText(geminiApiKey)) {
            log.warn("Gemini API key not configured");
            return null;
        }
        return encryptionEnabled ? decrypt(geminiApiKey) : geminiApiKey;
    }

    /**
     * Check if OpenAI is configured
     */
    public boolean isOpenAiConfigured() {
        return StringUtils.hasText(getOpenAiApiKey());
    }

    /**
     * Check if Anthropic is configured
     */
    public boolean isAnthropicConfigured() {
        return StringUtils.hasText(getAnthropicApiKey());
    }

    /**
     * Encrypt API key for storage
     */
    public String encrypt(String apiKey) {
        if (!encryptionEnabled) {
            return apiKey;
        }
        if (!StringUtils.hasText(encryptionSecretKey)) {
            log.warn("Encryption secret key not configured, returning plaintext");
            return apiKey;
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(encryptionSecretKey.getBytes(StandardCharsets.UTF_8), AES);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(apiKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException 
                 | IllegalBlockSizeException | BadPaddingException e) {
            log.error("Failed to encrypt API key", e);
            throw new RuntimeException("Failed to encrypt API key", e);
        }
    }

    /**
     * Decrypt API key for use
     */
    public String decrypt(String encryptedApiKey) {
        if (!encryptionEnabled) {
            return encryptedApiKey;
        }
        if (!StringUtils.hasText(encryptionSecretKey)) {
            log.warn("Encryption secret key not configured, returning plaintext");
            return encryptedApiKey;
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(encryptionSecretKey.getBytes(StandardCharsets.UTF_8), AES);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedApiKey));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException 
                 | IllegalBlockSizeException | BadPaddingException e) {
            log.error("Failed to decrypt API key", e);
            throw new RuntimeException("Failed to decrypt API key", e);
        }
    }

    /**
     * Validate API key format (basic validation)
     */
    public boolean isValidApiKeyFormat(String apiKey, String provider) {
        if (!StringUtils.hasText(apiKey)) {
            return false;
        }
        
        // Basic format validation
        switch (provider.toLowerCase()) {
            case "openai":
                return apiKey.startsWith("sk-") && apiKey.length() >= 20;
            case "anthropic":
                return apiKey.startsWith("sk-ant-") && apiKey.length() >= 20;
            default:
                return apiKey.length() >= 10;
        }
    }
}
