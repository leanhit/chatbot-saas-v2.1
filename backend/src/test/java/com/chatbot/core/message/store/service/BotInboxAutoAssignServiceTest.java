package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Agent;
import com.chatbot.core.message.store.model.AutoAssignConfig;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.AutoAssignConfigRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test for BotInboxAutoAssignService - CRITICAL level testing
 * Tests auto-assignment configuration persistence and conversation routing
 */
@ExtendWith(MockitoExtension.class)
class BotInboxAutoAssignServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private AgentService agentService;

    @Mock
    private AgentAssignmentService agentAssignmentService;

    @Mock
    private RoutingRuleService routingRuleService;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private AutoAssignConfigRepository autoAssignConfigRepository;

    @InjectMocks
    private BotInboxAutoAssignService botInboxAutoAssignService;

    private Conversation testConversation;
    private Tenant testTenant;
    private Agent testAgent;

    @BeforeEach
    void setUp() {
        testTenant = new Tenant();
        testTenant.setId(1L);
        testTenant.setStatus(TenantStatus.ACTIVE);

        testConversation = new Conversation();
        testConversation.setId(1L);
        testConversation.setTenantId(1L);
        testConversation.setStatus("open");
        testConversation.setIsTakenOverByAgent(false);
        testConversation.setCustomAttributes("{\"requiredSkill\":\"vip_support\"}");

        testAgent = new Agent();
        testAgent.setId(10L);
        testAgent.setStatus(Agent.AgentStatus.ONLINE);
        testAgent.setActive(true);
        testAgent.setCurrentLoad(2);
        testAgent.setMaxConcurrentConversations(5);
    }

    @Test
    void autoAssignConversation_WithAlreadyTakenOverConversation_ShouldReturnFalse() {
        // Arrange
        testConversation.setIsTakenOverByAgent(true);

        // Act
        boolean result = botInboxAutoAssignService.autoAssignConversation(testConversation);

        // Assert
        assert !result : "Should not auto-assign already taken over conversation";
        verify(routingRuleService, never()).applyRoutingRules(any());
    }

    @Test
    void autoAssignConversation_WithClosedConversation_ShouldReturnFalse() {
        // Arrange
        testConversation.setStatus("closed");

        // Act
        boolean result = botInboxAutoAssignService.autoAssignConversation(testConversation);

        // Assert
        assert !result : "Should not auto-assign closed conversation";
    }

    @Test
    void autoAssignConversation_WithRoutingRuleMatch_ShouldApplyRule() {
        // Arrange
        when(routingRuleService.applyRoutingRules(testConversation)).thenReturn(true);

        // Act
        boolean result = botInboxAutoAssignService.autoAssignConversation(testConversation);

        // Assert
        assert result : "Should return true when routing rule is applied";
        verify(routingRuleService).applyRoutingRules(testConversation);
        verify(agentService, never()).getAvailableAgents(anyLong());
    }

    @Test
    void autoAssignConversation_WithNoRoutingRule_ShouldTrySkillsBasedAssignment() {
        // Arrange
        when(routingRuleService.applyRoutingRules(testConversation)).thenReturn(false);
        when(agentService.getAgentsBySkill(1L, "vip_support")).thenReturn(Arrays.asList(testAgent));
        when(agentAssignmentService.reassignConversation(1L, 10L)).thenReturn(true);

        // Act
        boolean result = botInboxAutoAssignService.autoAssignConversation(testConversation);

        // Assert
        assert result : "Should successfully assign based on skills";
        verify(agentService).getAgentsBySkill(1L, "vip_support");
        verify(agentAssignmentService).reassignConversation(1L, 10L);
    }

    @Test
    void configureAutoAssign_ShouldSaveConfigurationToDatabase() {
        // Arrange
        when(autoAssignConfigRepository.findByTenantId(1L)).thenReturn(Optional.empty());
        when(autoAssignConfigRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        botInboxAutoAssignService.configureAutoAssign(1L, true, 60);

        // Assert
        verify(autoAssignConfigRepository).save(any(AutoAssignConfig.class));
    }

    @Test
    void configureAutoAssign_WithExistingConfig_ShouldUpdateConfiguration() {
        // Arrange
        AutoAssignConfig existingConfig = AutoAssignConfig.builder()
            .tenantId(1L)
            .enabled(false)
            .intervalSeconds(30)
            .maxConcurrentPerAgent(5)
            .build();
        
        when(autoAssignConfigRepository.findByTenantId(1L)).thenReturn(Optional.of(existingConfig));
        when(autoAssignConfigRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        botInboxAutoAssignService.configureAutoAssign(1L, true, 60);

        // Assert
        verify(autoAssignConfigRepository).save(any(AutoAssignConfig.class));
    }

    @Test
    void getAutoAssignConfig_WithExistingConfig_ShouldReturnConfig() {
        // Arrange
        AutoAssignConfig existingConfig = AutoAssignConfig.builder()
            .tenantId(1L)
            .enabled(true)
            .intervalSeconds(45)
            .maxConcurrentPerAgent(8)
            .build();
        
        when(autoAssignConfigRepository.findByTenantId(1L)).thenReturn(Optional.of(existingConfig));

        // Act
        AutoAssignConfig result = botInboxAutoAssignService.getAutoAssignConfig(1L);

        // Assert
        assert result != null : "Should return existing config";
        assert result.getIntervalSeconds() == 45 : "Should return config with correct interval";
    }

    @Test
    void getAutoAssignConfig_WithNoExistingConfig_ShouldReturnDefaultConfig() {
        // Arrange
        when(autoAssignConfigRepository.findByTenantId(1L)).thenReturn(Optional.empty());

        // Act
        AutoAssignConfig result = botInboxAutoAssignService.getAutoAssignConfig(1L);

        // Assert
        assert result != null : "Should return default config";
        assert result.getEnabled() : "Default config should be enabled";
        assert result.getIntervalSeconds() == 30 : "Default interval should be 30 seconds";
    }

    @Test
    void getAutoAssignStats_ShouldReturnStatistics() {
        // Arrange
        when(conversationRepository.findByIsTakenOverByAgentAndTenantId(false, 1L))
            .thenReturn(Arrays.asList(testConversation));
        when(agentService.getAvailableAgents(1L)).thenReturn(Arrays.asList(testAgent));

        // Act
        var stats = botInboxAutoAssignService.getAutoAssignStats(1L);

        // Assert
        assert stats.get("botInboxCount").equals(1) : "Should count 1 bot conversation";
        assert stats.get("availableAgentsCount").equals(1) : "Should count 1 available agent";
        assert stats.get("autoAssignEnabled").equals(true) : "Auto-assign should be enabled";
    }

    @Test
    void triggerAutoAssign_WithValidConversation_ShouldAssign() {
        // Arrange
        testConversation.setCustomAttributes(null); // Remove custom skill requirement
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));
        when(routingRuleService.applyRoutingRules(testConversation)).thenReturn(false);
        when(agentService.getAvailableAgents(1L)).thenReturn(Arrays.asList(testAgent));
        when(agentAssignmentService.reassignConversation(1L, 10L)).thenReturn(true);

        // Act
        boolean result = botInboxAutoAssignService.triggerAutoAssign(1L);

        // Assert
        assert result : "Should successfully trigger auto-assign";
    }

    @Test
    void triggerAutoAssign_WithInvalidConversation_ShouldReturnFalse() {
        // Arrange
        when(conversationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        boolean result = botInboxAutoAssignService.triggerAutoAssign(999L);

        // Assert
        assert !result : "Should return false for invalid conversation";
    }
}
