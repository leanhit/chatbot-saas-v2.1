package com.chatbot.core.identity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing JWT key rotation
 * Supports automatic key rotation and maintains multiple active keys for validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtKeyManagementService {

    private static final String REDIS_KEY_PREFIX = "jwt:keys:";
    private static final String REDIS_CURRENT_KEY_ID = REDIS_KEY_PREFIX + "current";
    private static final String REDIS_KEYS_HASH = REDIS_KEY_PREFIX + "active";
    private static final long REDIS_TTL_DAYS = 180; // 6 months TTL

    @Value("${jwt.rotation.enabled:false}")
    private boolean rotationEnabled;

    @Value("${jwt.rotation.interval-days:90}")
    private int rotationIntervalDays;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // Local cache for performance
    private final Map<String, JwtKey> localActiveKeys = new ConcurrentHashMap<>();
    private String localCurrentKeyId;

    @Data
    public static class JwtKey {
        private String keyId;
        private String encodedSecretKey; // Store as Base64 string for Redis serialization
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private boolean isCurrent;

        // Transient field for runtime use
        private transient SecretKey secretKey;
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
            // Load keys from Redis
            loadKeysFromRedis();

            // If no keys in Redis, generate initial key
            if (localCurrentKeyId == null || localActiveKeys.isEmpty()) {
                JwtKey initialKey = generateNewKey();
                localCurrentKeyId = initialKey.getKeyId();
                localActiveKeys.put(localCurrentKeyId, initialKey);
                saveKeysToRedis();
                log.info("JWT key management initialized with new key ID: {}", localCurrentKeyId);
            } else {
                log.info("JWT key management loaded from Redis with current key ID: {}", localCurrentKeyId);
            }
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
    @SchedulerLock(name = "JwtKeyManagementService_scheduledKeyRotation", lockAtMostFor = "2h", lockAtLeastFor = "1h")
    public void scheduledKeyRotation() {
        if (!rotationEnabled) {
            return;
        }

        try {
            // Reload from Redis to ensure we have the latest state
            loadKeysFromRedis();
            checkAndRotateKeys();
        } catch (Exception e) {
            log.error("Error during scheduled key rotation: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if key rotation is needed and perform rotation
     */
    public void checkAndRotateKeys() {
        if (localCurrentKeyId == null) {
            log.warn("No current key ID set. Skipping rotation.");
            return;
        }

        JwtKey currentKey = localActiveKeys.get(localCurrentKeyId);
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
            String oldKeyId = localCurrentKeyId;
            
            // Generate new key
            JwtKey newKey = generateNewKey();
            
            // Mark old current key as not current
            if (localCurrentKeyId != null) {
                JwtKey oldKey = localActiveKeys.get(localCurrentKeyId);
                if (oldKey != null) {
                    oldKey.setCurrent(false);
                }
            }

            // Set new key as current
            localCurrentKeyId = newKey.getKeyId();
            newKey.setCurrent(true);
            localActiveKeys.put(localCurrentKeyId, newKey);

            // Save to Redis for cluster-wide synchronization
            saveKeysToRedis();

            log.info("JWT key rotation completed. New key ID: {}, Previous key ID: {}", 
                    localCurrentKeyId, oldKeyId);
            
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
        jwtKey.setEncodedSecretKey(Base64.getEncoder().encodeToString(secretKey.getEncoded()));
        jwtKey.setSecretKey(secretKey); // Set transient field for immediate use
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
        if (!rotationEnabled || localCurrentKeyId == null) {
            return null; // Fall back to static key from JwtService
        }

        JwtKey currentKey = localActiveKeys.get(localCurrentKeyId);
        if (currentKey == null) {
            log.error("Current key not found for key ID: {}", localCurrentKeyId);
            return null;
        }

        // Decode secret key if not already decoded
        if (currentKey.getSecretKey() == null && currentKey.getEncodedSecretKey() != null) {
            currentKey.setSecretKey(decodeSecretKey(currentKey.getEncodedSecretKey()));
        }

        return currentKey.getSecretKey();
    }

    /**
     * Get the current key ID
     */
    public String getCurrentKeyId() {
        return localCurrentKeyId;
    }

    /**
     * Get a key by ID for validation
     */
    public SecretKey getKeyById(String keyId) {
        if (!rotationEnabled) {
            return null; // Fall back to static key from JwtService
        }

        JwtKey key = localActiveKeys.get(keyId);
        if (key == null) {
            // Try loading from Redis if not in local cache
            loadKeysFromRedis();
            key = localActiveKeys.get(keyId);
        }
        
        if (key == null) {
            return null;
        }

        // Decode secret key if not already decoded
        if (key.getSecretKey() == null && key.getEncodedSecretKey() != null) {
            key.setSecretKey(decodeSecretKey(key.getEncodedSecretKey()));
        }
        
        return key.getSecretKey();
    }

    /**
     * Check if a key ID is valid (exists and not expired)
     */
    public boolean isValidKeyId(String keyId) {
        if (!rotationEnabled) {
            return true; // Accept any key ID if rotation is disabled
        }

        JwtKey key = localActiveKeys.get(keyId);
        if (key == null) {
            // Try loading from Redis if not in local cache
            loadKeysFromRedis();
            key = localActiveKeys.get(keyId);
        }
        
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
        
        localActiveKeys.entrySet().removeIf(entry -> {
            JwtKey key = entry.getValue();
            boolean isExpired = now.isAfter(key.getExpiresAt());
            
            if (isExpired && !key.isCurrent()) {
                log.info("Removing expired key: {}", entry.getKey());
                return true;
            }
            
            return false;
        });
        
        // Save cleaned state to Redis
        saveKeysToRedis();
    }

    /**
     * Manually trigger key rotation (for admin operations)
     */
    public void manualKeyRotation() {
        log.info("Manual key rotation triggered by admin");
        loadKeysFromRedis();
        rotateKeys();
    }

    /**
     * Get information about active keys
     */
    public Map<String, JwtKey> getActiveKeys() {
        return Map.copyOf(localActiveKeys);
    }

    /**
     * Load keys from Redis into local cache
     */
    private void loadKeysFromRedis() {
        try {
            // Load current key ID
            String currentKeyId = redisTemplate.opsForValue().get(REDIS_CURRENT_KEY_ID);
            if (currentKeyId != null) {
                localCurrentKeyId = currentKeyId;
            }

            // Load all active keys
            Map<Object, Object> redisKeys = redisTemplate.opsForHash().entries(REDIS_KEYS_HASH);
            localActiveKeys.clear();
            
            for (Map.Entry<Object, Object> entry : redisKeys.entrySet()) {
                String keyId = (String) entry.getKey();
                String keyJson = (String) entry.getValue();
                
                try {
                    JwtKey jwtKey = objectMapper.readValue(keyJson, JwtKey.class);
                    localActiveKeys.put(keyId, jwtKey);
                } catch (Exception e) {
                    log.error("Failed to deserialize JWT key {}: {}", keyId, e.getMessage());
                }
            }
            
            log.debug("Loaded {} JWT keys from Redis", localActiveKeys.size());
        } catch (Exception e) {
            log.error("Failed to load JWT keys from Redis: {}", e.getMessage());
        }
    }

    /**
     * Save keys to Redis for cluster-wide synchronization
     */
    private void saveKeysToRedis() {
        try {
            // Save current key ID
            if (localCurrentKeyId != null) {
                redisTemplate.opsForValue().set(REDIS_CURRENT_KEY_ID, localCurrentKeyId, 
                    REDIS_TTL_DAYS, TimeUnit.DAYS);
            }

            // Save all active keys
            Map<String, String> redisKeys = new java.util.HashMap<>();
            for (Map.Entry<String, JwtKey> entry : localActiveKeys.entrySet()) {
                try {
                    String keyJson = objectMapper.writeValueAsString(entry.getValue());
                    redisKeys.put(entry.getKey(), keyJson);
                } catch (Exception e) {
                    log.error("Failed to serialize JWT key {}: {}", entry.getKey(), e.getMessage());
                }
            }
            
            if (!redisKeys.isEmpty()) {
                redisTemplate.opsForHash().putAll(REDIS_KEYS_HASH, redisKeys);
                redisTemplate.expire(REDIS_KEYS_HASH, REDIS_TTL_DAYS, TimeUnit.DAYS);
            }
            
            log.debug("Saved {} JWT keys to Redis", localActiveKeys.size());
        } catch (Exception e) {
            log.error("Failed to save JWT keys to Redis: {}", e.getMessage());
        }
    }

    /**
     * Decode secret key from Base64 string
     */
    private SecretKey decodeSecretKey(String encodedKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
            return new javax.crypto.spec.SecretKeySpec(keyBytes, "HmacSHA256");
        } catch (Exception e) {
            log.error("Failed to decode secret key: {}", e.getMessage());
            return null;
        }
    }
}
