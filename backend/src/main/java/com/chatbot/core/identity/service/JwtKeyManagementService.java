package com.chatbot.core.identity.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing JWT key rotation
 * Supports automatic key rotation and maintains multiple active keys for validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtKeyManagementService {

    @Value("${jwt.rotation.enabled:false}")
    private boolean rotationEnabled;

    @Value("${jwt.rotation.interval-days:90}")
    private int rotationIntervalDays;

    private final Map<String, JwtKey> activeKeys = new ConcurrentHashMap<>();
    private String currentKeyId;

    @Data
    public static class JwtKey {
        private String keyId;
        private SecretKey secretKey;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private boolean isCurrent;
    }

    /**
     * Initialize the first key on startup
     */
    @javax.annotation.PostConstruct
    public void initialize() {
        if (!rotationEnabled) {
            log.info("JWT key rotation is disabled. Using static key from configuration.");
            return;
        }

        try {
            // Generate initial key
            JwtKey initialKey = generateNewKey();
            currentKeyId = initialKey.getKeyId();
            activeKeys.put(currentKeyId, initialKey);
            
            log.info("JWT key management initialized with key ID: {}", currentKeyId);
        } catch (Exception e) {
            log.error("Failed to initialize JWT key management: {}", e.getMessage(), e);
            // Fall back to static key if rotation fails
        }
    }

    /**
     * Scheduled task to rotate JWT keys
     * Runs daily to check if rotation is needed
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM daily
    public void scheduledKeyRotation() {
        if (!rotationEnabled) {
            return;
        }

        try {
            checkAndRotateKeys();
        } catch (Exception e) {
            log.error("Error during scheduled key rotation: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if key rotation is needed and perform rotation
     */
    public void checkAndRotateKeys() {
        if (currentKeyId == null) {
            log.warn("No current key ID set. Skipping rotation.");
            return;
        }

        JwtKey currentKey = activeKeys.get(currentKeyId);
        if (currentKey == null) {
            log.warn("Current key not found. Generating new key.");
            rotateKeys();
            return;
        }

        // Check if current key is old enough to rotate
        LocalDateTime rotationThreshold = currentKey.getCreatedAt().plusDays(rotationIntervalDays);
        if (LocalDateTime.now().isAfter(rotationThreshold)) {
            log.info("Key rotation threshold reached. Rotating keys.");
            rotateKeys();
        } else {
            log.debug("Key rotation not needed yet. Next rotation scheduled for: {}", rotationThreshold);
        }

        // Clean up expired keys
        cleanupExpiredKeys();
    }

    /**
     * Perform key rotation
     */
    public synchronized void rotateKeys() {
        try {
            String oldKeyId = currentKeyId;
            
            // Generate new key
            JwtKey newKey = generateNewKey();
            
            // Mark old current key as not current
            if (currentKeyId != null) {
                JwtKey oldKey = activeKeys.get(currentKeyId);
                if (oldKey != null) {
                    oldKey.setCurrent(false);
                }
            }

            // Set new key as current
            currentKeyId = newKey.getKeyId();
            newKey.setCurrent(true);
            activeKeys.put(currentKeyId, newKey);

            log.info("JWT key rotation completed. New key ID: {}, Previous key ID: {}", 
                    currentKeyId, oldKeyId);
            
        } catch (Exception e) {
            log.error("Failed to rotate JWT keys: {}", e.getMessage(), e);
            throw new RuntimeException("JWT key rotation failed", e);
        }
    }

    /**
     * Generate a new JWT key
     */
    private JwtKey generateNewKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
        keyGenerator.init(256);
        SecretKey secretKey = keyGenerator.generateKey();

        String keyId = generateKeyId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(rotationIntervalDays * 2); // Keep keys for 2x rotation interval

        JwtKey jwtKey = new JwtKey();
        jwtKey.setKeyId(keyId);
        jwtKey.setSecretKey(secretKey);
        jwtKey.setCreatedAt(now);
        jwtKey.setExpiresAt(expiresAt);
        jwtKey.setCurrent(false);

        return jwtKey;
    }

    /**
     * Generate a unique key ID
     */
    private String generateKeyId() {
        return "key-" + System.currentTimeMillis() + "-" + 
               Base64.getEncoder().encodeToString(java.util.UUID.randomUUID().toString().getBytes())
                  .substring(0, 8);
    }

    /**
     * Get the current signing key
     */
    public SecretKey getCurrentSigningKey() {
        if (!rotationEnabled || currentKeyId == null) {
            return null; // Fall back to static key from JwtService
        }

        JwtKey currentKey = activeKeys.get(currentKeyId);
        if (currentKey == null) {
            log.error("Current key not found for key ID: {}", currentKeyId);
            return null;
        }

        return currentKey.getSecretKey();
    }

    /**
     * Get the current key ID
     */
    public String getCurrentKeyId() {
        return currentKeyId;
    }

    /**
     * Get a key by ID for validation
     */
    public SecretKey getKeyById(String keyId) {
        if (!rotationEnabled) {
            return null; // Fall back to static key from JwtService
        }

        JwtKey key = activeKeys.get(keyId);
        return key != null ? key.getSecretKey() : null;
    }

    /**
     * Check if a key ID is valid (exists and not expired)
     */
    public boolean isValidKeyId(String keyId) {
        if (!rotationEnabled) {
            return true; // Accept any key ID if rotation is disabled
        }

        JwtKey key = activeKeys.get(keyId);
        if (key == null) {
            return false;
        }

        return LocalDateTime.now().isBefore(key.getExpiresAt());
    }

    /**
     * Clean up expired keys
     */
    private void cleanupExpiredKeys() {
        LocalDateTime now = LocalDateTime.now();
        
        activeKeys.entrySet().removeIf(entry -> {
            JwtKey key = entry.getValue();
            boolean isExpired = now.isAfter(key.getExpiresAt());
            
            if (isExpired && !key.isCurrent()) {
                log.info("Removing expired key: {}", entry.getKey());
                return true;
            }
            
            return false;
        });
    }

    /**
     * Manually trigger key rotation (for admin operations)
     */
    public void manualKeyRotation() {
        log.info("Manual key rotation triggered by admin");
        rotateKeys();
    }

    /**
     * Get information about active keys
     */
    public Map<String, JwtKey> getActiveKeys() {
        return Map.copyOf(activeKeys);
    }
}
