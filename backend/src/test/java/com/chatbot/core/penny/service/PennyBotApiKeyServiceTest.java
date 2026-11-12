package com.chatbot.core.penny.service;

import com.chatbot.core.penny.model.PennyBot;
import com.chatbot.core.penny.model.PennyBotApiKey;
import com.chatbot.core.penny.model.PennyBotType;
import com.chatbot.core.penny.repository.PennyBotApiKeyRepository;
import com.chatbot.core.penny.repository.PennyBotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for per-bot public API key lifecycle and validation.
 */
@ExtendWith(MockitoExtension.class)
class PennyBotApiKeyServiceTest {

    @Mock private PennyBotApiKeyRepository apiKeyRepository;
    @Mock private PennyBotRepository pennyBotRepository;

    private PennyBotApiKeyService service;

    private final UUID botId = UUID.randomUUID();
    private final Long tenantId = 100L;
    private final String owner = "owner@test.com";

    @BeforeEach
    void setUp() {
        service = new PennyBotApiKeyService(apiKeyRepository, pennyBotRepository);
    }

    private PennyBot bot(Long tid, String ownerId) {
        return PennyBot.builder()
            .id(botId).tenantId(tid).ownerId(ownerId)
            .botName("Bot").botType(PennyBotType.SUPPORT)
            .pennyBotId("penny-1").isActive(true).isEnabled(true)
            .build();
    }

    @Test
    @DisplayName("create returns plaintext once and stores only hash")
    void create_storesHashOnly() {
        when(pennyBotRepository.findByIdAndTenantId(botId, tenantId))
            .thenReturn(Optional.of(bot(tenantId, owner)));
        when(apiKeyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = service.createApiKey(botId, tenantId, "web", owner);

        String plaintext = (String) result.get("apiKey");
        assertTrue(plaintext.startsWith("pk_live_"));

        ArgumentCaptor<PennyBotApiKey> captor = ArgumentCaptor.forClass(PennyBotApiKey.class);
        verify(apiKeyRepository).save(captor.capture());
        PennyBotApiKey saved = captor.getValue();
        assertNotEquals(plaintext, saved.getKeyHash());
        assertEquals(64, saved.getKeyHash().length()); // SHA-256 hex
        assertFalse(saved.getKeyHash().contains(plaintext));
    }

    @Test
    @DisplayName("create by non-owner -> AccessDenied")
    void create_nonOwner_denied() {
        when(pennyBotRepository.findByIdAndTenantId(botId, tenantId))
            .thenReturn(Optional.of(bot(tenantId, "other@test.com")));

        assertThrows(AccessDeniedException.class,
            () -> service.createApiKey(botId, tenantId, "web", owner));
    }

    @Test
    @DisplayName("validatePublicKey accepts correct key, rejects wrong/revoked/expired")
    void validate_keyScenarios() {
        when(pennyBotRepository.findByIdAndTenantId(botId, tenantId))
            .thenReturn(Optional.of(bot(tenantId, owner)));
        when(apiKeyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Map<String, Object> created = service.createApiKey(botId, tenantId, "web", owner);
        String plaintext = (String) created.get("apiKey");

        PennyBotApiKey active = PennyBotApiKey.builder()
            .id(UUID.randomUUID()).botId(botId).tenantId(tenantId)
            .keyHash(sha256ForTest(plaintext)).keyPrefix("pk_live_xxxx")
            .isActive(true).build();

        when(apiKeyRepository.findByBotId(botId)).thenReturn(List.of(active));
        when(apiKeyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertTrue(service.validatePublicKey(botId, plaintext));
        assertFalse(service.validatePublicKey(botId, "pk_live_wrong"));
        assertFalse(service.validatePublicKey(botId, null));

        // revoked
        active.setActive(false);
        assertFalse(service.validatePublicKey(botId, plaintext));

        // expired
        active.setActive(true);
        active.setExpiresAt(LocalDateTime.now().minusDays(1));
        assertFalse(service.validatePublicKey(botId, plaintext));
    }

    @Test
    @DisplayName("revoke deactivates key")
    void revoke_deactivates() {
        UUID keyId = UUID.randomUUID();
        when(pennyBotRepository.findByIdAndTenantId(botId, tenantId))
            .thenReturn(Optional.of(bot(tenantId, owner)));
        PennyBotApiKey key = PennyBotApiKey.builder()
            .id(keyId).botId(botId).tenantId(tenantId).isActive(true).build();
        when(apiKeyRepository.findByIdAndBotIdAndTenantId(keyId, botId, tenantId))
            .thenReturn(Optional.of(key));
        when(apiKeyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.revokeApiKey(keyId, botId, tenantId, owner);

        assertFalse(key.isActive());
    }

    @Test
    @DisplayName("hasActiveKeys reflects active non-expired keys")
    void hasActiveKeys() {
        when(apiKeyRepository.findByBotId(botId)).thenReturn(List.of());
        assertFalse(service.hasActiveKeys(botId));

        PennyBotApiKey active = PennyBotApiKey.builder()
            .id(UUID.randomUUID()).botId(botId).tenantId(tenantId)
            .keyHash("h").keyPrefix("p").isActive(true).build();
        when(apiKeyRepository.findByBotId(botId)).thenReturn(List.of(active));
        assertTrue(service.hasActiveKeys(botId));
    }

    private static String sha256ForTest(String input) {
        try {
            java.security.MessageDigest d = java.security.MessageDigest.getInstance("SHA-256");
            return java.util.HexFormat.of().formatHex(d.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
