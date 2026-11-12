package com.chatbot.core.penny.service;

import com.chatbot.core.penny.model.PennyBot;
import com.chatbot.core.penny.model.PennyBotApiKey;
import com.chatbot.core.penny.repository.PennyBotApiKeyRepository;
import com.chatbot.core.penny.repository.PennyBotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages per-bot public API keys for the public chat endpoint.
 * Keys are stored as SHA-256 hashes; plaintext is returned only once at creation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PennyBotApiKeyService {

    private static final String KEY_PREFIX = "pk_live_";
    private static final int KEY_RANDOM_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PennyBotApiKeyRepository apiKeyRepository;
    private final PennyBotRepository pennyBotRepository;

    /**
     * Create a new API key for a bot. Returns a map containing the entity
     * metadata and the plaintext key (shown only once).
     */
    @Transactional
    public Map<String, Object> createApiKey(UUID botId, Long tenantId, String name, String createdBy) {
        PennyBot bot = pennyBotRepository.findByIdAndTenantId(botId, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Bot not found: " + botId));
        if (!bot.getOwnerId().equals(createdBy)) {
            throw new AccessDeniedException("Not authorized to manage API keys for this bot");
        }

        byte[] random = new byte[KEY_RANDOM_BYTES];
        SECURE_RANDOM.nextBytes(random);
        String plaintext = KEY_PREFIX + HexFormat.of().formatHex(random);

        PennyBotApiKey entity = PennyBotApiKey.builder()
            .id(UUID.randomUUID())
            .botId(botId)
            .tenantId(tenantId)
            .keyHash(sha256(plaintext))
            .keyPrefix(plaintext.substring(0, Math.min(12, plaintext.length())))
            .name(name)
            .isActive(true)
            .createdBy(createdBy)
            .build();

        apiKeyRepository.save(entity);
        log.info("🔑 Created public API key {} for bot {}", entity.getKeyPrefix(), botId);

        return Map.of(
            "id", entity.getId().toString(),
            "botId", botId.toString(),
            "keyPrefix", entity.getKeyPrefix(),
            "name", name != null ? name : "",
            "apiKey", plaintext // shown once only
        );
    }

    /**
     * List API keys for a bot (metadata only, no hashes/plaintext).
     */
    public List<PennyBotApiKey> listApiKeys(UUID botId, Long tenantId, String ownerId) {
        PennyBot bot = pennyBotRepository.findByIdAndTenantId(botId, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Bot not found: " + botId));
        if (!bot.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Not authorized to view API keys for this bot");
        }
        return apiKeyRepository.findByBotIdAndTenantIdAndIsActiveTrue(botId, tenantId);
    }

    /**
     * Revoke (deactivate) an API key.
     */
    @Transactional
    public void revokeApiKey(UUID keyId, UUID botId, Long tenantId, String ownerId) {
        PennyBot bot = pennyBotRepository.findByIdAndTenantId(botId, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Bot not found: " + botId));
        if (!bot.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Not authorized to revoke API keys for this bot");
        }

        PennyBotApiKey key = apiKeyRepository.findByIdAndBotIdAndTenantId(keyId, botId, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("API key not found: " + keyId));
        key.setActive(false);
        apiKeyRepository.save(key);
        log.info("🔒 Revoked public API key {} for bot {}", key.getKeyPrefix(), botId);
    }

    /**
     * Validate a plaintext API key against a bot's active keys.
     * Used by the public chat endpoint (no tenant context available).
     * Returns true and records last_used_at on success.
     */
    @Transactional
    public boolean validatePublicKey(UUID botId, String plaintextKey) {
        if (plaintextKey == null || plaintextKey.isBlank()) {
            return false;
        }
        String hash = sha256(plaintextKey);
        LocalDateTime now = LocalDateTime.now();

        List<PennyBotApiKey> keys = apiKeyRepository.findByBotId(botId);
        for (PennyBotApiKey key : keys) {
            if (!key.isActive()) {
                continue;
            }
            if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(now)) {
                continue;
            }
            if (constantTimeEquals(key.getKeyHash(), hash)) {
                key.setLastUsedAt(now);
                apiKeyRepository.save(key);
                return true;
            }
        }
        return false;
    }

    /**
     * Whether the bot has any active per-bot API keys configured.
     */
    public boolean hasActiveKeys(UUID botId) {
        return apiKeyRepository.findByBotId(botId).stream()
            .anyMatch(k -> k.isActive()
                && (k.getExpiresAt() == null || k.getExpiresAt().isAfter(LocalDateTime.now())));
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(aBytes, bBytes);
    }
}
