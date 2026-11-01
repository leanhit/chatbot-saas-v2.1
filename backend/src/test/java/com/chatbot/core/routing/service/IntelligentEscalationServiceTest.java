package com.chatbot.core.routing.service;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.routing.dto.EscalationContext;
import com.chatbot.core.routing.dto.EscalationResult;
import com.chatbot.core.routing.model.EscalationHistory;
import com.chatbot.core.routing.model.EscalationRule;
import com.chatbot.core.routing.repository.EscalationHistoryRepository;
import com.chatbot.core.routing.repository.IntelligentEscalationRuleRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class IntelligentEscalationServiceTest {

    @Mock
    private IntelligentEscalationRuleRepository escalationRuleRepository;

    @Mock
    private EscalationHistoryRepository escalationHistoryRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private IntelligentEscalationService intelligentEscalationService;

    private static final Long TEST_TENANT_ID = 1L;
    private static final Long TEST_CONVERSATION_ID = 100L;
    private static final Long TEST_AGENT_ID = 10L;
    private static final Long TEST_TEAM_ID = 20L;

    @BeforeEach
    void setUp() {
        // Reset before each test
    }

    @Test
    void testEvaluateEscalation_WaitTimeExceeded_Escalates() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            EscalationRule waitTimeRule = createEscalationRule(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
            waitTimeRule.setTimeThresholdMinutes(30);

            EscalationContext context = new EscalationContext();
            context.setWaitTimeMinutes(35); // Exceeds threshold

            when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                    .thenReturn(List.of(waitTimeRule));
            when(conversationRepository.findById(TEST_CONVERSATION_ID)).thenReturn(Optional.of(new Conversation()));
            when(escalationHistoryRepository.save(any(EscalationHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            EscalationResult result = intelligentEscalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEscalated());
            assertEquals("Escalation rule matched: " + waitTimeRule.getName(), result.getReason());
            verify(escalationHistoryRepository, times(1)).save(any(EscalationHistory.class));
        }
    }

    @Test
    void testEvaluateEscalation_NoResponse_Escalates() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            EscalationRule noResponseRule = createEscalationRule(EscalationRule.TriggerType.NO_RESPONSE);
            noResponseRule.setTimeThresholdMinutes(60);

            EscalationContext context = new EscalationContext();
            context.setLastResponseTime(LocalDateTime.now().minusMinutes(65)); // 65 minutes ago

            when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                    .thenReturn(List.of(noResponseRule));
            when(conversationRepository.findById(TEST_CONVERSATION_ID)).thenReturn(Optional.of(new Conversation()));
            when(escalationHistoryRepository.save(any(EscalationHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            EscalationResult result = intelligentEscalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEscalated());
        }
    }

    @Test
    void testEvaluateEscalation_LowCustomerSatisfaction_Escalates() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            EscalationRule satisfactionRule = createEscalationRule(EscalationRule.TriggerType.LOW_CUSTOMER_SATISFACTION);
            satisfactionRule.setThresholdValue(3); // Score below 3 triggers escalation

            EscalationContext context = new EscalationContext();
            context.setCustomerSatisfactionScore(2); // Below threshold

            when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                    .thenReturn(List.of(satisfactionRule));
            when(conversationRepository.findById(TEST_CONVERSATION_ID)).thenReturn(Optional.of(new Conversation()));
            when(escalationHistoryRepository.save(any(EscalationHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            EscalationResult result = intelligentEscalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEscalated());
        }
    }

    @Test
    void testEvaluateEscalation_HighPriorityCustomer_Escalates() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            EscalationRule priorityRule = createEscalationRule(EscalationRule.TriggerType.HIGH_PRIORITY_CUSTOMER);
            priorityRule.setTriggerValue("VIP");

            EscalationContext context = new EscalationContext();
            context.setCustomerPriority("VIP");

            when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                    .thenReturn(List.of(priorityRule));
            when(conversationRepository.findById(TEST_CONVERSATION_ID)).thenReturn(Optional.of(new Conversation()));
            when(escalationHistoryRepository.save(any(EscalationHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            EscalationResult result = intelligentEscalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEscalated());
        }
    }

    @Test
    void testEvaluateEscalation_CustomerTier_Escalates() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            EscalationRule tierRule = createEscalationRule(EscalationRule.TriggerType.CUSTOMER_TIER);
            tierRule.setTriggerValue("ENTERPRISE");

            EscalationContext context = new EscalationContext();
            context.setCustomerTier("ENTERPRISE");

            when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                    .thenReturn(List.of(tierRule));
            when(conversationRepository.findById(TEST_CONVERSATION_ID)).thenReturn(Optional.of(new Conversation()));
            when(escalationHistoryRepository.save(any(EscalationHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            EscalationResult result = intelligentEscalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEscalated());
        }
    }

    @Test
    void testEvaluateEscalation_CustomCondition_Escalates() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            EscalationRule customRule = createEscalationRule(EscalationRule.TriggerType.CUSTOM_CONDITION);
            Map<String, Object> conditions = new HashMap<>();
            conditions.put("region", "US");
            conditions.put("language", "en");
            customRule.setConditions(conditions);

            EscalationContext context = new EscalationContext();
            Map<String, Object> customData = new HashMap<>();
            customData.put("region", "US");
            customData.put("language", "en");
            context.setCustomData(customData);

            when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                    .thenReturn(List.of(customRule));
            when(conversationRepository.findById(TEST_CONVERSATION_ID)).thenReturn(Optional.of(new Conversation()));
            when(escalationHistoryRepository.save(any(EscalationHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            EscalationResult result = intelligentEscalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEscalated());
        }
    }

    @Test
    void testEvaluateEscalation_NoRulesMatch_ReturnsNotEscalated() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            EscalationRule waitTimeRule = createEscalationRule(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
            waitTimeRule.setTimeThresholdMinutes(30);

            EscalationContext context = new EscalationContext();
            context.setWaitTimeMinutes(10); // Below threshold

            when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                    .thenReturn(List.of(waitTimeRule));

            // Act
            EscalationResult result = intelligentEscalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEscalated());
            assertEquals("No escalation rules matched", result.getReason());
        }
    }

    @Test
    void testEvaluateEscalation_NoActiveRules_ReturnsNotEscalated() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                    .thenReturn(Collections.emptyList());

            EscalationContext context = new EscalationContext();

            // Act
            EscalationResult result = intelligentEscalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEscalated());
            assertEquals("No active escalation rules", result.getReason());
        }
    }

    @Test
    void testEvaluateEscalation_MultiTenantIsolation() {
        // Arrange
        Long tenant1Id = 1L;
        Long tenant2Id = 2L;

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(tenant1Id);

            EscalationRule tenant1Rule = createEscalationRule(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
            tenant1Rule.setTenantId(tenant1Id);
            tenant1Rule.setTimeThresholdMinutes(30);

            EscalationContext context = new EscalationContext();
            context.setWaitTimeMinutes(35);

            when(escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(tenant1Id))
                    .thenReturn(List.of(tenant1Rule));
            when(conversationRepository.findById(TEST_CONVERSATION_ID)).thenReturn(Optional.of(new Conversation()));
            when(escalationHistoryRepository.save(any(EscalationHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            EscalationResult result = intelligentEscalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEscalated());

            // Verify tenant2 rules were never queried
            verify(escalationRuleRepository, never()).findByTenantIdAndIsActiveTrueOrderByPriorityAsc(tenant2Id);
        }
    }

    // Helper methods
    private EscalationRule createEscalationRule(EscalationRule.TriggerType triggerType) {
        EscalationRule rule = new EscalationRule();
        rule.setId(1L);
        rule.setName("Test Escalation Rule");
        rule.setDescription("Test rule for unit testing");
        rule.setTriggerType(triggerType);
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_AGENT);
        rule.setEscalateToAgentId(TEST_AGENT_ID);
        rule.setEscalateToSupervisor(false);
        rule.setNotifySupervisor(true);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        return rule;
    }
}
