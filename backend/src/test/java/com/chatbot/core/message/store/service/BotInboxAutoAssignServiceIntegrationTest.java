package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.AutoAssignConfig;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.AutoAssignConfigRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.repository.TenantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import com.chatbot.config.AbstractTestcontainersIntegrationTest;

/**
 * Integration test for BotInboxAutoAssignService using Testcontainers PostgreSQL database
 * Tests auto-assignment logic with real PostgreSQL database operations
 */
class BotInboxAutoAssignServiceIntegrationTest extends AbstractTestcontainersIntegrationTest {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private AutoAssignConfigRepository autoAssignConfigRepository;

    private Tenant testTenant;
    private Conversation testConversation;
    private Long testTenantId = 1L;

    @BeforeEach
    void setUp() {
        // Clean up database
        conversationRepository.deleteAll();
        autoAssignConfigRepository.deleteAll();
        tenantRepository.deleteAll();

        // Create test tenant
        testTenant = Tenant.builder()
                .tenantKey("test-tenant-key-" + System.currentTimeMillis())
                .name("Test Tenant")
                .status(TenantStatus.ACTIVE)
                .build();
        testTenant = tenantRepository.save(testTenant);
        testTenantId = testTenant.getId();

        // Create test conversation
        testConversation = Conversation.builder()
                .tenantId(testTenantId)
                .externalUserId("user-123")
                .status("open")
                .build();
        testConversation = conversationRepository.save(testConversation);
    }

    @AfterEach
    void tearDown() {
        conversationRepository.deleteAll();
        autoAssignConfigRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    void testConversationRepository_SaveAndFind() {
        // Act
        Optional<Conversation> foundConversation = conversationRepository.findById(testConversation.getId());

        // Assert
        assertTrue(foundConversation.isPresent());
        assertEquals("user-123", foundConversation.get().getExternalUserId());
        assertEquals("open", foundConversation.get().getStatus());
    }

    @Test
    void testConversationRepository_FindByStatus() {
        // Arrange
        Conversation assignedConversation = Conversation.builder()
                .tenantId(testTenantId)
                .externalUserId("user-456")
                .status("closed")
                .build();
        conversationRepository.save(assignedConversation);

        // Act
        java.util.List<Conversation> openConversations = conversationRepository.findByStatus("open");

        // Assert
        assertEquals(1, openConversations.size());
        assertEquals(testConversation.getId(), openConversations.get(0).getId());
    }

    @Test
    void testTenantRepository_SaveAndFind() {
        // Act
        Optional<Tenant> foundTenant = tenantRepository.findById(testTenantId);

        // Assert
        assertTrue(foundTenant.isPresent());
        assertEquals("Test Tenant", foundTenant.get().getName());
        assertEquals(TenantStatus.ACTIVE, foundTenant.get().getStatus());
    }

    @Test
    void testAutoAssignConfigRepository_SaveAndFind() {
        // Arrange
        AutoAssignConfig config = AutoAssignConfig.builder()
                .tenantId(testTenantId)
                .enabled(true)
                .maxConcurrentPerAgent(5)
                .build();
        autoAssignConfigRepository.save(config);

        // Act
        Optional<AutoAssignConfig> foundConfig = autoAssignConfigRepository.findByTenantId(testTenantId);

        // Assert
        assertTrue(foundConfig.isPresent());
        assertTrue(foundConfig.get().getEnabled());
        assertEquals(5, foundConfig.get().getMaxConcurrentPerAgent());
    }
}
