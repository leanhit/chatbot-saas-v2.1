package com.chatbot.core.identity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtKeyManagementServiceTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private HashOperations<String, Object, Object> hashOps;

    private JwtKeyManagementService service;

    @BeforeEach
    void setUp() {
        service = new JwtKeyManagementService(redisTemplate, new ObjectMapper().registerModule(new JavaTimeModule()));
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOps);
        // empty redis by default
        lenient().when(valueOps.get(anyString())).thenReturn(null);
        lenient().when(hashOps.entries(anyString())).thenReturn(Map.of());
    }

    private void enableRotation() {
        ReflectionTestUtils.setField(service, "rotationEnabled", true);
        ReflectionTestUtils.setField(service, "rotationIntervalDays", 90);
    }

    @SuppressWarnings("unchecked")
    private Map<String, JwtKeyManagementService.JwtKey> localKeys() {
        return (Map<String, JwtKeyManagementService.JwtKey>)
            ReflectionTestUtils.getField(service, "localActiveKeys");
    }

    // ---------- rotation disabled ----------

    @Test
    void disabledRotationReturnsNullKeys() {
        ReflectionTestUtils.setField(service, "rotationEnabled", false);
        assertNull(service.getCurrentSigningKey());
        assertNull(service.getKeyById("any"));
        assertTrue(service.isValidKeyId("any")); // accepts any when disabled
    }

    @Test
    void disabledRotationInitializeDoesNothing() {
        ReflectionTestUtils.setField(service, "rotationEnabled", false);
        service.initialize();
        assertNull(service.getCurrentKeyId());
        verifyNoInteractions(valueOps, hashOps);
    }

    @Test
    void disabledRotationScheduledRotationSkips() {
        ReflectionTestUtils.setField(service, "rotationEnabled", false);
        service.scheduledKeyRotation();
        verify(hashOps, never()).entries(anyString());
    }

    // ---------- initialize ----------

    @Test
    void initializeGeneratesInitialKeyWhenRedisEmpty() {
        enableRotation();
        service.initialize();

        assertNotNull(service.getCurrentKeyId());
        assertEquals(1, service.getActiveKeys().size());
        // saved to redis
        verify(valueOps).set(eq("jwt:keys:current"), anyString(), eq(180L), eq(TimeUnit.DAYS));
        verify(hashOps).putAll(eq("jwt:keys:active"), anyMap());
        verify(redisTemplate).expire(eq("jwt:keys:active"), eq(180L), eq(TimeUnit.DAYS));
    }

    @Test
    void initializeLoadsExistingKeysFromRedis() {
        enableRotation();
        // simulate existing key in redis hash
        JwtKeyManagementService.JwtKey existing = new JwtKeyManagementService.JwtKey();
        existing.setKeyId("key-old");
        existing.setEncodedSecretKey(java.util.Base64.getEncoder().encodeToString(new byte[32]));
        existing.setCreatedAt(LocalDateTime.now().minusDays(1));
        existing.setExpiresAt(LocalDateTime.now().plusDays(100));
        existing.setCurrent(true);
        try {
            when(hashOps.entries("jwt:keys:active"))
                .thenReturn(Map.of("key-old", new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(existing)));
            when(valueOps.get("jwt:keys:current")).thenReturn("key-old");
        } catch (Exception e) { throw new RuntimeException(e); }

        service.initialize();

        assertEquals("key-old", service.getCurrentKeyId());
        assertNotNull(service.getCurrentSigningKey());
        // no new key generated
        assertEquals(1, service.getActiveKeys().size());
    }

    // ---------- getCurrentSigningKey / getKeyById ----------

    @Test
    void getCurrentSigningKeyReturnsSecretKey() {
        enableRotation();
        service.initialize();
        SecretKey key = service.getCurrentSigningKey();
        assertNotNull(key);
        assertEquals("HmacSHA256", key.getAlgorithm());
    }

    @Test
    void getKeyByIdReturnsKeyForValidId() {
        enableRotation();
        service.initialize();
        String keyId = service.getCurrentKeyId();
        assertNotNull(service.getKeyById(keyId));
        assertNull(service.getKeyById("nonexistent"));
    }

    // ---------- rotateKeys ----------

    @Test
    void rotateKeysGeneratesNewCurrentAndKeepsOldValid() {
        enableRotation();
        service.initialize();
        String oldKeyId = service.getCurrentKeyId();

        service.rotateKeys();

        String newKeyId = service.getCurrentKeyId();
        assertNotNull(newKeyId);
        assertNotEquals(oldKeyId, newKeyId);
        // old key still retrievable for validation
        assertNotNull(service.getKeyById(oldKeyId));
        assertTrue(service.isValidKeyId(oldKeyId));
        assertTrue(service.isValidKeyId(newKeyId));
        assertEquals(2, service.getActiveKeys().size());
    }

    @Test
    void manualKeyRotationRotates() {
        enableRotation();
        service.initialize();
        String before = service.getCurrentKeyId();
        service.manualKeyRotation();
        assertNotEquals(before, service.getCurrentKeyId());
    }

    // ---------- checkAndRotateKeys ----------

    @Test
    void checkAndRotateDoesNothingWhenKeyFresh() {
        enableRotation();
        service.initialize();
        String before = service.getCurrentKeyId();
        service.checkAndRotateKeys();
        assertEquals(before, service.getCurrentKeyId());
    }

    @Test
    void checkAndRotateRotatesWhenKeyTooOld() {
        enableRotation();
        service.initialize();
        String before = service.getCurrentKeyId();
        // age the current key beyond rotation interval
        JwtKeyManagementService.JwtKey key = localKeys().get(before);
        key.setCreatedAt(LocalDateTime.now().minusDays(100));

        service.checkAndRotateKeys();

        assertNotEquals(before, service.getCurrentKeyId());
    }

    @Test
    void checkAndRotateGeneratesKeyWhenCurrentMissing() {
        enableRotation();
        service.initialize();
        // corrupt state: current key id points to nothing
        ReflectionTestUtils.setField(service, "localCurrentKeyId", "ghost-key");

        service.checkAndRotateKeys();

        assertNotNull(service.getCurrentKeyId());
        assertNotEquals("ghost-key", service.getCurrentKeyId());
    }

    // ---------- isValidKeyId ----------

    @Test
    void isValidKeyIdFalseForExpiredKey() {
        enableRotation();
        service.initialize();
        String keyId = service.getCurrentKeyId();
        JwtKeyManagementService.JwtKey key = localKeys().get(keyId);
        key.setExpiresAt(LocalDateTime.now().minusDays(1));

        assertFalse(service.isValidKeyId(keyId));
    }

    // ---------- cleanupExpiredKeys ----------

    @Test
    void cleanupRemovesExpiredNonCurrentKeys() {
        enableRotation();
        service.initialize();
        service.rotateKeys(); // now 2 keys
        String current = service.getCurrentKeyId();
        // expire the OLD key (non-current)
        String oldKeyId = service.getActiveKeys().keySet().stream()
            .filter(id -> !id.equals(current)).findFirst().orElseThrow();
        localKeys().get(oldKeyId).setExpiresAt(LocalDateTime.now().minusDays(1));

        service.checkAndRotateKeys(); // triggers cleanup

        assertFalse(service.getActiveKeys().containsKey(oldKeyId));
        assertTrue(service.getActiveKeys().containsKey(current));
    }

    // ---------- getActiveKeys ----------

    @Test
    void getActiveKeysReturnsCopy() {
        enableRotation();
        service.initialize();
        Map<String, JwtKeyManagementService.JwtKey> keys = service.getActiveKeys();
        assertThrows(UnsupportedOperationException.class, () -> keys.clear());
    }
}
