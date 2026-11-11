package com.chatbot.core.routing.service;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.routing.dto.EscalationContext;
import com.chatbot.core.routing.dto.EscalationResult;
import com.chatbot.core.routing.model.EscalationRule;
import com.chatbot.core.routing.repository.EscalationHistoryRepository;
import com.chatbot.core.routing.repository.IntelligentEscalationRuleRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EscalationServiceMockitoTest {

    @Mock
    private IntelligentEscalationRuleRepository escalationRuleRepository;

    @Mock
    private EscalationHistoryRepository escalationHistoryRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private IntelligentEscalationService escalationService;

    private MockedStatic<TenantContext> tenantContextMock;

    private static final Long TEST_TENANT_ID = 1L;
    private static final Long TEST_CONVERSATION_ID = 100L;
    private static final Long TEST_AGENT_ID = 10L;

    @BeforeEach
    void setUp() {
        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Test
    void testEvaluateEscalation_NoRules_ReturnsNotEscalated() {
        // Arrange
        when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                .thenReturn(Collections.emptyList());

        EscalationContext context = new EscalationContext();

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEscalated());
        assertEquals("No active escalation rules", result.getReason());
    }

    @Test
    void testEvaluateEscalation_WaitTimeExceeded_ReturnsEscalated() {
        // Arrange
        EscalationRule rule = new EscalationRule();
        rule.setId(1L);
        rule.setName("Wait Time Rule");
        rule.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        rule.setTimeThresholdMinutes(30);
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        rule.setEscalateToSupervisor(true);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                .thenReturn(List.of(rule));

        EscalationContext context = new EscalationContext();
        context.setWaitTimeMinutes(35); // Over threshold

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEscalated());
        assertTrue(result.isEscalatedToSupervisor());
    }

    @Test
    void testEvaluateEscalation_NoResponse_ReturnsEscalated() {
        // Arrange
        EscalationRule rule = new EscalationRule();
        rule.setId(1L);
        rule.setName("No Response Rule");
        rule.setTriggerType(EscalationRule.TriggerType.NO_RESPONSE);
        rule.setTimeThresholdMinutes(15);
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_AGENT);
        rule.setEscalateToAgentId(TEST_AGENT_ID);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                .thenReturn(List.of(rule));

        EscalationContext context = new EscalationContext();
        context.setLastResponseTime(LocalDateTime.now().minusMinutes(20)); // 20 minutes ago

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEscalated());
        assertEquals(TEST_AGENT_ID, result.getEscalatedToAgentId());
    }

    @Test
    void testEvaluateEscalation_LowCustomerSatisfaction_ReturnsEscalated() {
        // Arrange
        EscalationRule rule = new EscalationRule();
        rule.setId(1L);
        rule.setName("Low CSAT Rule");
        rule.setTriggerType(EscalationRule.TriggerType.LOW_CUSTOMER_SATISFACTION);
        rule.setThresholdValue(3); // CSAT below 3
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        rule.setEscalateToSupervisor(true);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                .thenReturn(List.of(rule));

        EscalationContext context = new EscalationContext();
        context.setCustomerSatisfactionScore(2); // Below threshold

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEscalated());
    }

    @Test
    void testEvaluateEscalation_HighPriorityCustomer_ReturnsEscalated() {
        // Arrange
        EscalationRule rule = new EscalationRule();
        rule.setId(1L);
        rule.setName("High Priority Rule");
        rule.setTriggerType(EscalationRule.TriggerType.HIGH_PRIORITY_CUSTOMER);
        rule.setTriggerValue("high");
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        rule.setEscalateToSupervisor(true);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                .thenReturn(List.of(rule));

        EscalationContext context = new EscalationContext();
        context.setCustomerPriority("high");

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEscalated());
    }

    @Test
    void testEvaluateEscalation_CustomerTier_ReturnsEscalated() {
        // Arrange
        EscalationRule rule = new EscalationRule();
        rule.setId(1L);
        rule.setName("Customer Tier Rule");
        rule.setTriggerType(EscalationRule.TriggerType.CUSTOMER_TIER);
        rule.setTriggerValue("premium");
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        rule.setEscalateToSupervisor(true);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                .thenReturn(List.of(rule));

        EscalationContext context = new EscalationContext();
        context.setCustomerTier("premium");

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEscalated());
    }

    @Test
    void testEvaluateEscalation_CustomCondition_ReturnsEscalated() {
        // Arrange
        EscalationRule rule = new EscalationRule();
        rule.setId(1L);
        rule.setName("Custom Condition Rule");
        rule.setTriggerType(EscalationRule.TriggerType.CUSTOM_CONDITION);
        rule.setConditions(Map.of("customField", "expectedValue"));
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        rule.setEscalateToSupervisor(true);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                .thenReturn(List.of(rule));

        EscalationContext context = new EscalationContext();
        context.setCustomData(Map.of("customField", "expectedValue"));

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEscalated());
    }

    @Test
    void testEvaluateEscalation_ConditionNotMet_ReturnsNotEscalated() {
        // Arrange
        EscalationRule rule = new EscalationRule();
        rule.setId(1L);
        rule.setName("Wait Time Rule");
        rule.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        rule.setTimeThresholdMinutes(30);
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                .thenReturn(List.of(rule));

        EscalationContext context = new EscalationContext();
        context.setWaitTimeMinutes(10); // Below threshold

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEscalated());
        assertEquals("No escalation rules matched", result.getReason());
    }

    @Test
    void testEvaluateEscalation_AssignToAgent_ActionExecuted() {
        // Arrange
        EscalationRule rule = new EscalationRule();
        rule.setId(1L);
        rule.setName("Assign to Agent Rule");
        rule.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        rule.setTimeThresholdMinutes(30);
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_AGENT);
        rule.setEscalateToAgentId(TEST_AGENT_ID);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                .thenReturn(List.of(rule));

        Conversation conversation = new Conversation();
        conversation.setId(TEST_CONVERSATION_ID);
        conversation.setAgentAssignedId(null);

        when(conversationRepository.findByIdAndTenantId(TEST_CONVERSATION_ID, TEST_TENANT_ID)).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        EscalationContext context = new EscalationContext();
        context.setWaitTimeMinutes(35);

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEscalated());
        assertEquals(TEST_AGENT_ID, result.getEscalatedToAgentId());
        assertEquals("ASSIGN_TO_AGENT", result.getEscalationAction());
        verify(conversationRepository, atLeastOnce()).save(any(Conversation.class));
    }

    @Test
    void testEvaluateEscalation_AssignToTeam_ActionExecuted() {
        // Arrange
        Long testTeamId = 100L;
        EscalationRule rule = new EscalationRule();
        rule.setId(1L);
        rule.setName("Assign to Team Rule");
        rule.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        rule.setTimeThresholdMinutes(30);
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_TEAM);
        rule.setEscalateToTeamId(testTeamId);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                .thenReturn(List.of(rule));

        EscalationContext context = new EscalationContext();
        context.setWaitTimeMinutes(35);

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEscalated());
        assertEquals(testTeamId, result.getEscalatedToTeamId());
        assertEquals("ASSIGN_TO_TEAM", result.getEscalationAction());
    }

    @Test
    void testEvaluateEscalation_MultipleRules_PriorityOrder() {
        // Arrange
        EscalationRule rule1 = new EscalationRule();
        rule1.setId(1L);
        rule1.setName("High Priority Rule");
        rule1.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        rule1.setTimeThresholdMinutes(30);
        rule1.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        rule1.setEscalateToSupervisor(true);
        rule1.setPriority(1);
        rule1.setIsActive(true);
        rule1.setTenantId(TEST_TENANT_ID);

        EscalationRule rule2 = new EscalationRule();
        rule2.setId(2L);
        rule2.setName("Low Priority Rule");
        rule2.setTriggerType(EscalationRule.TriggerType.NO_RESPONSE);
        rule2.setTimeThresholdMinutes(15);
        rule2.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_AGENT);
        rule2.setEscalateToAgentId(TEST_AGENT_ID);
        rule2.setPriority(10);
        rule2.setIsActive(true);
        rule2.setTenantId(TEST_TENANT_ID);

        when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                .thenReturn(List.of(rule1, rule2));

        EscalationContext context = new EscalationContext();
        context.setWaitTimeMinutes(35);

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert - Should match first rule (priority 1)
        assertNotNull(result);
        assertTrue(result.isEscalated());
        assertTrue(result.isEscalatedToSupervisor());
    }

    @Test
    void testGetEscalationHistory_ReturnsHistory() {
        // Arrange
        when(escalationHistoryRepository.findByTenantIdAndConversationId(TEST_TENANT_ID, TEST_CONVERSATION_ID))
                .thenReturn(Collections.emptyList());

        // Act
        var history = escalationService.getEscalationHistory(TEST_CONVERSATION_ID);

        // Assert
        assertNotNull(history);
        verify(escalationHistoryRepository).findByTenantIdAndConversationId(TEST_TENANT_ID, TEST_CONVERSATION_ID);
    }
}
