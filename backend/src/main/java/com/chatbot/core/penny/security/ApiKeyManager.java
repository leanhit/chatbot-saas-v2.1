package com.chatbot.core.penny.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * ApiKeyManager - Secure management of API keys for external services.
 *
 * Encryption: AES-GCM with a random 96-bit nonce per encryption.
 * Stored format: "v1:" + Base64(nonce || ciphertext || tag).
 *
 * Fail-closed policy: when encryption is enabled but no secret is configured,
 * encrypt/decrypt throw IllegalStateException — plaintext is never returned
 * or persisted as a fallback.
 */
@Service
@Slf4j
public class ApiKeyManager {

    private static final String GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final String KEY_VERSION_PREFIX = "v1:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${penny.api-key.encryption.secret:#{null}}")
    private String encryptionSecretKey;

    /**
     * Optional previous encryption secret, used only for DECRYPTION during
     * key rotation. Set this to the old secret while re-encrypting stored
     * keys with the new one, then remove it once migration completes.
     */
    @Value("${penny.api-key.encryption.previous-secret:#{null}}")
    private String previousEncryptionSecretKey;

    @Value("${penny.openai.api-key:}")
    private String openaiApiKey;

    @Value("${penny.anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${penny.api-key.encryption.enabled:true}")
    private boolean encryptionEnabled;

    @Value("${penny.gemini.api-key:}")
    private String geminiApiKey;

    /**
     * Get OpenAI API key (decrypted). Returns null when not configured or
     * when decryption fails — never returns plaintext on misconfiguration.
     */
    public String getOpenAiApiKey() {
        return resolveKey(openaiApiKey, "OpenAI");
    }

    /**
     * Get Anthropic API key (decrypted).
     */
    public String getAnthropicApiKey() {
        return resolveKey(anthropicApiKey, "Anthropic");
    }

    /**
     * Alias for getAnthropicApiKey
     */
    public String getClaudeApiKey() {
        return getAnthropicApiKey();
    }

    /**
     * Get Gemini API key (decrypted).
     */
    public String getGeminiApiKey() {
        return resolveKey(geminiApiKey, "Gemini");
    }

    private String resolveKey(String configuredKey, String providerName) {
        if (!StringUtils.hasText(configuredKey)) {
            log.warn("{} API key not configured", providerName);
            return null;
        }
        if (!encryptionEnabled) {
            return configuredKey;
        }
        try {
            return decrypt(configuredKey);
        } catch (Exception e) {
            // Fail closed: treat undecryptable/misconfigured keys as absent.
            log.error("Failed to decrypt {} API key (encryption misconfiguration?): {}", providerName, e.getMessage());
            return null;
        }
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
     * Encrypt API key for storage using AES-GCM.
     *
     * @throws IllegalStateException when encryption is enabled but the secret is missing
     */
    public String encrypt(String apiKey) {
        if (!encryptionEnabled) {
            return apiKey;
        }
        SecretKeySpec secretKey = requireSecretKey();
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(GCM_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(apiKey.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return KEY_VERSION_PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Failed to encrypt API key", e);
            throw new RuntimeException("Failed to encrypt API key", e);
        }
    }

    /**
     * Decrypt API key for use.
     * Supports the versioned "v1:" AES-GCM format and falls back to the legacy
     * AES/ECB format for previously stored values.
     *
     * @throws IllegalStateException when encryption is enabled but the secret is missing
     */
    public String decrypt(String encryptedApiKey) {
        if (!encryptionEnabled) {
            return encryptedApiKey;
        }
        requireSecretKey(); // fail fast when the current secret is missing
        try {
            if (encryptedApiKey.startsWith(KEY_VERSION_PREFIX)) {
                return decryptGcmWithRotation(encryptedApiKey.substring(KEY_VERSION_PREFIX.length()));
            }
            return decryptLegacyWithRotation(encryptedApiKey);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to decrypt API key", e);
            throw new RuntimeException("Failed to decrypt API key", e);
        }
    }

    /**
     * Try GCM decryption with the current key first, then the previous key
     * (if configured for rotation).
     */
    private String decryptGcmWithRotation(String payload) throws Exception {
        Exception lastError = null;
        for (SecretKeySpec key : candidateKeys()) {
            try {
                return decryptGcm(payload, key);
            } catch (Exception e) {
                lastError = e;
            }
        }
        throw lastError != null ? lastError : new IllegalStateException("No decryption key available");
    }

    /**
     * Legacy AES/ECB values were encrypted with the raw secret bytes; try
     * current then previous secret.
     */
    private String decryptLegacyWithRotation(String encryptedApiKey) throws Exception {
        Exception lastError = null;
        for (String secret : candidateSecrets()) {
            try {
                return decryptLegacy(encryptedApiKey, secret);
            } catch (Exception e) {
                lastError = e;
            }
        }
        throw lastError != null ? lastError : new IllegalStateException("No decryption key available");
    }

    private java.util.List<SecretKeySpec> candidateKeys() {
        java.util.List<SecretKeySpec> keys = new java.util.ArrayList<>();
        keys.add(requireSecretKey());
        if (StringUtils.hasText(previousEncryptionSecretKey)) {
            keys.add(deriveKey(previousEncryptionSecretKey));
        }
        return keys;
    }

    private java.util.List<String> candidateSecrets() {
        java.util.List<String> secrets = new java.util.ArrayList<>();
        secrets.add(encryptionSecretKey);
        if (StringUtils.hasText(previousEncryptionSecretKey)) {
            secrets.add(previousEncryptionSecretKey);
        }
        return secrets;
    }

    private String decryptGcm(String payload, SecretKeySpec secretKey) throws Exception {
        byte[] combined = Base64.getDecoder().decode(payload);
        if (combined.length <= GCM_IV_LENGTH) {
            throw new IllegalArgumentException("Ciphertext too short");
        }
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(combined, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance(GCM_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
        return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    }

    /**
     * Legacy AES/ECB/PKCS5Padding format used before the v1 GCM migration.
     * The old implementation used the raw secret bytes as the AES key.
     */
    private String decryptLegacy(String encryptedApiKey, String secret) throws Exception {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalStateException(
                "Legacy encrypted API key found but secret is not a valid AES key length (16/24/32 bytes) — re-encrypt the key with the current version");
        }
        SecretKeySpec legacyKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, legacyKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedApiKey));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private SecretKeySpec requireSecretKey() {
        if (!StringUtils.hasText(encryptionSecretKey)) {
            throw new IllegalStateException(
                "penny.api-key.encryption.secret is not configured while encryption is enabled — refusing to use plaintext API keys");
        }
        return deriveKey(encryptionSecretKey);
    }

    private SecretKeySpec deriveKey(String secret) {
        try {
            // Derive a fixed 256-bit key from the configured secret.
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(secret.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(digest, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to derive encryption key", e);
        }
    }

    /**
     * Validate API key format (basic validation)
     */
    public boolean isValidApiKeyFormat(String apiKey, String provider) {
        if (!StringUtils.hasText(apiKey)) {
            return false;
        }

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
