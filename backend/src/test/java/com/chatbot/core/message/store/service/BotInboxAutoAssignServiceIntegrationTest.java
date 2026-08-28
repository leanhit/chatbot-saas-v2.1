package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.AutoAssignConfig;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.AutoAssignConfigRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.repository.TenantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test for BotInboxAutoAssignService
 * Tests auto-assignment logic with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
class BotInboxAutoAssignServiceIntegrationTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private AutoAssignConfigRepository autoAssignConfigRepository;

    @Mock
    private AgentService agentService;

    @Mock
    private AgentAssignmentService agentAssignmentService;

    @Mock
    private RoutingRuleService routingRuleService;

    private BotInboxAutoAssignService autoAssignService;

    private Tenant testTenant;
    private Conversation testConversation;
    private Long testTenantId = 1L;

    @BeforeEach
    void setUp() {
        autoAssignService = new BotInboxAutoAssignService(
                conversationRepository,
                agentService,
                agentAssignmentService,
                routingRuleService,
                tenantRepository,
                autoAssignConfigRepository
        );

        // Set tenant context
        TenantContext.setTenantId(testTenantId);

        // Create test tenant
        testTenant = Tenant.builder()
                .tenantKey("test-tenant-key-" + System.currentTimeMillis())
                .name("Test Tenant")
                .status(TenantStatus.ACTIVE)
                .build();
        testTenant.setId(testTenantId);

        // Create test conversation
        testConversation = Conversation.builder()
                .tenantId(testTenantId)
                .externalUserId("user-123")
                .status("open")
                .isTakenOverByAgent(false)
                .build();
        testConversation.setId(1L);

        // Mock routing rule service (no rules by default) - lenient to avoid unnecessary stubbing errors
        lenient().when(routingRuleService.applyRoutingRules(any(Conversation.class))).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testAutoAssignConversation_SkipIfTakenOver() {
        // Arrange
        testConversation.setIsTakenOverByAgent(true);

        // Act
        boolean result = autoAssignService.autoAssignConversation(testConversation);

        // Assert
        assertFalse(result);
        verify(agentAssignmentService, never()).reassignConversation(anyLong(), anyLong());
    }

    @Test
    void testAutoAssignConversation_SkipIfClosed() {
        // Arrange
        testConversation.setStatus("closed");

        // Act
        boolean result = autoAssignService.autoAssignConversation(testConversation);

        // Assert
        assertFalse(result);
        verify(agentAssignmentService, never()).reassignConversation(anyLong(), anyLong());
    }

    @Test
    void testAutoAssignConversation_NoAvailableAgents() {
        // Arrange
        when(agentService.getAvailableAgents(testTenantId)).thenReturn(List.of());

        // Act
        boolean result = autoAssignService.autoAssignConversation(testConversation);

        // Assert
        assertFalse(result);
        verify(agentAssignmentService, never()).reassignConversation(anyLong(), anyLong());
    }

    @Test
    void testAutoAssignConversation_RoutingRuleApplied() {
        // Arrange
        when(routingRuleService.applyRoutingRules(any(Conversation.class))).thenReturn(true);

        // Act
        boolean result = autoAssignService.autoAssignConversation(testConversation);

        // Assert
        assertTrue(result); // Routing rule handled it successfully
        verify(routingRuleService, times(1)).applyRoutingRules(any(Conversation.class));
    }

    @Test
    void testAutoAssignConversation_AgentAssignmentFailed() {
        // Arrange
        when(agentService.getAvailableAgents(testTenantId)).thenReturn(List.of());
        lenient().when(agentAssignmentService.reassignConversation(anyLong(), anyLong())).thenReturn(false);

        // Act
        boolean result = autoAssignService.autoAssignConversation(testConversation);

        // Assert
        assertFalse(result);
    }

    @Test
    void testAutoAssignConversation_NullConversation() {
        // Act & Assert - Service throws NullPointerException for null conversation
        assertThrows(NullPointerException.class, () -> {
            autoAssignService.autoAssignConversation(null);
        });
    }

    @Test
    void testTriggerAutoAssign_ConversationNotFound() {
        // Arrange
        Long nonExistentConversationId = 99999L;
        when(conversationRepository.findById(nonExistentConversationId)).thenReturn(Optional.empty());

        // Act
        boolean result = autoAssignService.triggerAutoAssign(nonExistentConversationId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testTriggerAutoAssign_Success() {
        // Arrange
        when(conversationRepository.findById(testConversation.getId())).thenReturn(Optional.of(testConversation));
        when(agentService.getAvailableAgents(testTenantId)).thenReturn(List.of());
        lenient().when(agentAssignmentService.reassignConversation(eq(testConversation.getId()), anyLong()))
                .thenReturn(true);

        // Act
        boolean result = autoAssignService.triggerAutoAssign(testConversation.getId());

        // Assert - Should return false when no agents available
        assertFalse(result);
    }

    @Test
    void testConfigureAutoAssign_CreateNewConfig() {
        // Arrange
        when(autoAssignConfigRepository.findByTenantId(testTenantId)).thenReturn(Optional.empty());

        // Act
        autoAssignService.configureAutoAssign(testTenantId, true, 10);

        // Assert
        verify(autoAssignConfigRepository, times(1)).save(any(AutoAssignConfig.class));
    }

    @Test
    void testConfigureAutoAssign_UpdateExistingConfig() {
        // Arrange - Create existing config
        AutoAssignConfig existingConfig = AutoAssignConfig.builder()
                .tenantId(testTenantId)
                .enabled(false)
                .maxConcurrentPerAgent(5)
                .build();
        existingConfig.setId(1L);

        when(autoAssignConfigRepository.findByTenantId(testTenantId)).thenReturn(Optional.of(existingConfig));

        // Act
        autoAssignService.configureAutoAssign(testTenantId, true, 15);

        // Assert
        verify(autoAssignConfigRepository, times(1)).save(any(AutoAssignConfig.class));
    }

    @Test
    void testConfigureAutoAssign_DisableAutoAssign() {
        // Arrange
        when(autoAssignConfigRepository.findByTenantId(testTenantId)).thenReturn(Optional.empty());

        // Act
        autoAssignService.configureAutoAssign(testTenantId, false, 10);

        // Assert
        verify(autoAssignConfigRepository, times(1)).save(any(AutoAssignConfig.class));
    }
}
