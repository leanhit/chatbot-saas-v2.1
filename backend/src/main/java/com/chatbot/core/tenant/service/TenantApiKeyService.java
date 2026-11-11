package com.chatbot.core.tenant.service;

import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.model.TenantApiKey;
import com.chatbot.core.tenant.repository.TenantApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * Tenant API key management. Raw keys are generated once and never stored —
 * only the SHA-256 hash is persisted.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantApiKeyService {

    private static final String KEY_PREFIX = "cbk_";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TenantApiKeyRepository apiKeyRepository;
    private final TenantAuditLogService auditLogService;
    private final TenantPermissionValidator permissionValidator;

    private String currentUserEmail() {
        try {
            return permissionValidator.getCurrentUserEmail();
        } catch (Exception e) {
            return "system";
        }
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        return tenantId;
    }

    public static String hashKey(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawKey.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash API key", e);
        }
    }

    @Transactional(value = "tenantTransactionManager", readOnly = true)
    public List<TenantApiKey> listApiKeys() {
        return apiKeyRepository.findByTenantId(requireTenantId());
    }

    /**
     * Create a new API key. Returns the entity plus the raw key (shown once).
     */
    @Transactional(value = "tenantTransactionManager")
    public Map<String, Object> createApiKey(String name, TenantApiKey.KeyRole role, LocalDateTime expiresAt) {
        Long tenantId = requireTenantId();
        String rawKey = generateRawKey();
        String prefix = rawKey.substring(0, Math.min(12, rawKey.length()));

        TenantApiKey key = TenantApiKey.builder()
                .tenantId(tenantId)
                .name(name)
                .keyPrefix(prefix)
                .keyHash(hashKey(rawKey))
                .role(role != null ? role : TenantApiKey.KeyRole.MEMBER)
                .isActive(true)
                .expiresAt(expiresAt)
                .createdBy(currentUserEmail())
                .build();
        TenantApiKey saved = apiKeyRepository.save(key);

        auditLogService.logActionAsync(tenantId, currentUserEmail(),
                "TENANT_API_KEY_CREATED", "apiKeyId=" + saved.getId() + ", name=" + name);

        return Map.of(
                "apiKey", saved,
                "rawKey", rawKey,
                "warning", "Store this key securely — it will not be shown again."
        );
    }

    @Transactional(value = "tenantTransactionManager")
    public TenantApiKey revokeApiKey(Long id) {
        Long tenantId = requireTenantId();
        TenantApiKey key = apiKeyRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("API key not found: " + id));
        key.setIsActive(false);
        TenantApiKey saved = apiKeyRepository.save(key);
        auditLogService.logActionAsync(tenantId, currentUserEmail(),
                "TENANT_API_KEY_REVOKED", "apiKeyId=" + id + ", name=" + key.getName());
        return saved;
    }

    private String generateRawKey() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return KEY_PREFIX + HexFormat.of().formatHex(bytes);
    }
}
