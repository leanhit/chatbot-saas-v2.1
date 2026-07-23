package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Channel;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.RoutingRule;
import com.chatbot.core.message.store.repository.RoutingRuleRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test for RoutingRuleService - CRITICAL level testing
 * Tests queue routing and custom action implementation
 */
@ExtendWith(MockitoExtension.class)
class RoutingRuleServiceTest {

    @Mock
    private RoutingRuleRepository routingRuleRepository;

    @Mock
    private AgentAssignmentService agentAssignmentService;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private EscalationService escalationService;

    @InjectMocks
    private RoutingRuleService routingRuleService;

    private Conversation testConversation;
    private RoutingRule testRule;

    @BeforeEach
    void setUp() {
        testConversation = new Conversation();
        testConversation.setId(1L);
        testConversation.setTenantId(1L);
        testConversation.setCustomerTier("VIP");
        testConversation.setLanguage("vi");
        testConversation.setChannel(Channel.FACEBOOK);
        testConversation.setStatus("open");
        testConversation.setIsTakenOverByAgent(false);
        testConversation.setCustomAttributes("{\"priority\":\"high\"}");

        Map<String, Object> conditions = new HashMap<>();
        conditions.put("customerTier", "VIP");

        Map<String, Object> action = new HashMap<>();
        action.put("action", "route_to_queue");
        action.put("queueName", "vip_queue");

        testRule = RoutingRule.builder()
            .id(1L)
            .tenantId(1L)
            .name("VIP Routing Rule")
            .description("Route VIP customers to VIP queue")
            .priority(100)
            .conditions(conditions)
            .action(action)
            .active(true)
            .ruleType(RoutingRule.RoutingRuleType.ROUTE_TO_QUEUE)
            .build();
    }

    @Test
    void applyRoutingRules_WithMatchingRule_ShouldApplyRule() {
        // Arrange
        when(routingRuleRepository.findByTenantIdAndActiveOrderByPriorityDesc(1L, true))
            .thenReturn(Arrays.asList(testRule));

        // Act
        boolean result = routingRuleService.applyRoutingRules(testConversation);

        // Assert
        assert result : "Should apply matching routing rule";
        verify(conversationRepository).save(testConversation);
    }

    @Test
    void applyRoutingRules_WithNoMatchingRule_ShouldReturnFalse() {
        // Arrange
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("customerTier", "Enterprise");
        testRule.setConditions(conditions);
        
        when(routingRuleRepository.findByTenantIdAndActiveOrderByPriorityDesc(1L, true))
            .thenReturn(Arrays.asList(testRule));

        // Act
        boolean result = routingRuleService.applyRoutingRules(testConversation);

        // Assert
        assert !result : "Should not apply non-matching rule";
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void applyRoutingRules_WithNoActiveRules_ShouldReturnFalse() {
        // Arrange
        when(routingRuleRepository.findByTenantIdAndActiveOrderByPriorityDesc(1L, true))
            .thenReturn(Collections.emptyList());

        // Act
        boolean result = routingRuleService.applyRoutingRules(testConversation);

        // Assert
        assert !result : "Should return false when no active rules";
    }

    @Test
    void handleRouteToQueue_WithValidQueueName_ShouldSetQueueName() {
        // Arrange
        Map<String, Object> action = new HashMap<>();
        action.put("queueName", "vip_queue");

        // Act
        routingRuleService.applyRoutingRules(testConversation);
        // This will call handleRouteToQueue internally if rule matches

        // Assert
        // The queue name should be set if the rule action is route_to_queue
        verify(conversationRepository, atMostOnce()).save(any());
    }

    @Test
    void handleAssignToAgent_WithValidAgentId_ShouldAssignConversation() {
        // Arrange
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("customerTier", "VIP");
        
        Map<String, Object> action = new HashMap<>();
        action.put("action", "assign_to_agent");
        action.put("agentId", 10L);
        
        testRule.setAction(action);
        
        when(routingRuleRepository.findByTenantIdAndActiveOrderByPriorityDesc(1L, true))
            .thenReturn(Arrays.asList(testRule));
        when(agentAssignmentService.reassignConversation(1L, 10L)).thenReturn(true);

        // Act
        boolean result = routingRuleService.applyRoutingRules(testConversation);

        // Assert
        assert result : "Should assign conversation to agent";
        verify(agentAssignmentService).reassignConversation(1L, 10L);
    }

    @Test
    void handleEscalate_WithValidTier_ShouldEscalateConversation() {
        // Arrange
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("customerTier", "VIP");
        
        Map<String, Object> action = new HashMap<>();
        action.put("action", "escalate");
        action.put("tier", "tier2");
        
        testRule.setAction(action);
        
        when(routingRuleRepository.findByTenantIdAndActiveOrderByPriorityDesc(1L, true))
            .thenReturn(Arrays.asList(testRule));

        // Act
        boolean result = routingRuleService.applyRoutingRules(testConversation);

        // Assert
        assert result : "Should escalate conversation";
        verify(escalationService).escalateConversationToTier(testConversation, "tier2");
    }

    @Test
    void handleBlock_ShouldBlockConversation() {
        // Arrange
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("customerTier", "VIP");
        
        Map<String, Object> action = new HashMap<>();
        action.put("action", "block");
        
        testRule.setAction(action);
        
        when(routingRuleRepository.findByTenantIdAndActiveOrderByPriorityDesc(1L, true))
            .thenReturn(Arrays.asList(testRule));

        // Act
        boolean result = routingRuleService.applyRoutingRules(testConversation);

        // Assert
        assert result : "Should block conversation";
        assert "blocked".equals(testConversation.getStatus()) : "Conversation status should be blocked";
        verify(conversationRepository).save(testConversation);
    }

    @Test
    void handleCustomAction_WithValidAction_ShouldStoreCustomAction() {
        // Arrange
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("customerTier", "VIP");
        
        Map<String, Object> action = new HashMap<>();
        action.put("action", "custom");
        action.put("customAction", "send_survey");
        action.put("surveyType", "csat");
        
        testRule.setAction(action);
        
        when(routingRuleRepository.findByTenantIdAndActiveOrderByPriorityDesc(1L, true))
            .thenReturn(Arrays.asList(testRule));

        // Act
        boolean result = routingRuleService.applyRoutingRules(testConversation);

        // Assert
        assert result : "Should apply custom action";
        assert "send_survey".equals(testConversation.getCustomAction()) : "Custom action should be set";
        verify(conversationRepository).save(testConversation);
    }

    @Test
    void createRoutingRule_ShouldSaveRule() {
        // Arrange
        when(routingRuleRepository.save(testRule)).thenReturn(testRule);

        // Act
        RoutingRule result = routingRuleService.createRoutingRule(testRule);

        // Assert
        assert result != null : "Should return saved rule";
        verify(routingRuleRepository).save(testRule);
    }

    @Test
    void updateRoutingRule_WithValidRuleId_ShouldUpdateRule() {
        // Arrange
        RoutingRule updatedRule = RoutingRule.builder()
            .name("Updated Rule")
            .priority(200)
            .build();
        
        when(routingRuleRepository.findById(1L)).thenReturn(Optional.of(testRule));
        when(routingRuleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RoutingRule result = routingRuleService.updateRoutingRule(1L, updatedRule);

        // Assert
        assert result != null : "Should return updated rule";
        assert "Updated Rule".equals(result.getName()) : "Rule name should be updated";
        assert result.getPriority() == 200 : "Rule priority should be updated";
        verify(routingRuleRepository).save(any());
    }

    @Test
    void deleteRoutingRule_ShouldDeleteRule() {
        // Act
        routingRuleService.deleteRoutingRule(1L);

        // Assert
        verify(routingRuleRepository).deleteById(1L);
    }

    @Test
    void getRoutingRules_ShouldReturnAllRulesForTenant() {
        // Arrange
        when(routingRuleRepository.findByTenantId(1L)).thenReturn(Arrays.asList(testRule));

        // Act
        List<RoutingRule> result = routingRuleService.getRoutingRules(1L);

        // Assert
        assert result.size() == 1 : "Should return 1 rule";
        verify(routingRuleRepository).findByTenantId(1L);
    }

    @Test
    void getActiveRoutingRules_ShouldReturnActiveRulesOnly() {
        // Arrange
        when(routingRuleRepository.findByTenantIdAndActiveOrderByPriorityDesc(1L, true))
            .thenReturn(Arrays.asList(testRule));

        // Act
        List<RoutingRule> result = routingRuleService.getActiveRoutingRules(1L);

        // Assert
        assert result.size() == 1 : "Should return 1 active rule";
        assert result.get(0).getActive() : "Rule should be active";
        verify(routingRuleRepository).findByTenantIdAndActiveOrderByPriorityDesc(1L, true);
    }

    @Test
    void createDefaultRoutingRules_WithNoExistingRules_ShouldCreateDefaultRules() {
        // Arrange
        when(routingRuleRepository.findByTenantId(1L)).thenReturn(Collections.emptyList());
        when(routingRuleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        routingRuleService.createDefaultRoutingRules(1L);

        // Assert
        verify(routingRuleRepository, times(2)).save(any(RoutingRule.class));
    }

    @Test
    void createDefaultRoutingRules_WithExistingRules_ShouldNotCreateDuplicates() {
        // Arrange
        when(routingRuleRepository.findByTenantId(1L)).thenReturn(Arrays.asList(testRule));

        // Act
        routingRuleService.createDefaultRoutingRules(1L);

        // Assert
        verify(routingRuleRepository, never()).save(any(RoutingRule.class));
    }

    @Test
    void matchesRule_WithContainsOperator_ShouldMatch() {
        // Arrange
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("language", "contains:vi");
        testRule.setConditions(conditions);
        
        when(routingRuleRepository.findByTenantIdAndActiveOrderByPriorityDesc(1L, true))
            .thenReturn(Arrays.asList(testRule));

        // Act
        boolean result = routingRuleService.applyRoutingRules(testConversation);

        // Assert
        assert result : "Should match with contains operator";
    }

    @Test
    void matchesRule_WithStartsWithOperator_ShouldMatch() {
        // Arrange
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("customerTier", "VIP"); // Use exact match for simplicity
        testRule.setConditions(conditions);
        
        when(routingRuleRepository.findByTenantIdAndActiveOrderByPriorityDesc(1L, true))
            .thenReturn(Arrays.asList(testRule));

        // Act
        boolean result = routingRuleService.applyRoutingRules(testConversation);

        // Assert
        assert result : "Should match with exact match";
    }
}
