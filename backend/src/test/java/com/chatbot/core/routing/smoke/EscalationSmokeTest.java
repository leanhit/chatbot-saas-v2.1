package com.chatbot.core.routing.smoke;

import com.chatbot.config.AbstractTestcontainersIntegrationTest;
import com.chatbot.core.routing.dto.EscalationContext;
import com.chatbot.core.routing.dto.EscalationResult;
import com.chatbot.core.routing.model.EscalationRule;
import com.chatbot.core.routing.repository.IntelligentEscalationRuleRepository;
import com.chatbot.core.routing.service.IntelligentEscalationService;
import com.chatbot.core.tenant.infra.TenantContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EscalationSmokeTest extends AbstractTestcontainersIntegrationTest {

    @Autowired
    private IntelligentEscalationService escalationService;

    @Autowired
    private IntelligentEscalationRuleRepository escalationRuleRepository;

    private static final Long TEST_TENANT_ID = 1L;
    private static final Long TEST_CONVERSATION_ID = 100L;
    private static final Long TEST_AGENT_ID = 10L;

    @BeforeEach
    void setUp() {
        escalationRuleRepository.deleteAll();
        TenantContext.setTenantId(TEST_TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testWaitTimeExceededTrigger() {
        // Arrange - Conversation waited over threshold
        EscalationRule rule = new EscalationRule();
        rule.setName("Wait Time Rule");
        rule.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        rule.setTimeThresholdMinutes(30);
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        rule.setEscalateToSupervisor(true);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        escalationRuleRepository.save(rule);

        EscalationContext context = new EscalationContext();
        context.setConversationId(TEST_CONVERSATION_ID);
        context.setWaitTimeMinutes(35); // Over threshold

        // Act - Trigger rule active
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert - Escalation created / alert triggered
        assertNotNull(result);
        assertTrue(result.isEscalated());
        assertTrue(result.isEscalatedToSupervisor());
    }

    @Test
    void testNoResponseTrigger() {
        // Arrange - No response after time threshold
        EscalationRule rule = new EscalationRule();
        rule.setName("No Response Rule");
        rule.setTriggerType(EscalationRule.TriggerType.NO_RESPONSE);
        rule.setTimeThresholdMinutes(15);
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_AGENT);
        rule.setEscalateToAgentId(TEST_AGENT_ID);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        escalationRuleRepository.save(rule);

        EscalationContext context = new EscalationContext();
        context.setConversationId(TEST_CONVERSATION_ID);
        context.setLastResponseTime(LocalDateTime.now().minusMinutes(20)); // 20 minutes ago

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert - Escalation executed
        assertNotNull(result);
        assertTrue(result.isEscalated());
        assertEquals(TEST_AGENT_ID, result.getEscalatedToAgentId());
    }

    @Test
    void testLowCustomerSatisfactionTrigger() {
        // Arrange - Rating below threshold
        EscalationRule rule = new EscalationRule();
        rule.setName("Low CSAT Rule");
        rule.setTriggerType(EscalationRule.TriggerType.LOW_CUSTOMER_SATISFACTION);
        rule.setThresholdValue(3); // CSAT below 3
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        rule.setEscalateToSupervisor(true);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        escalationRuleRepository.save(rule);

        EscalationContext context = new EscalationContext();
        context.setConversationId(TEST_CONVERSATION_ID);
        context.setCustomerSatisfactionScore(2); // Below threshold

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert - Escalation executed
        assertNotNull(result);
        assertTrue(result.isEscalated());
    }

    @Test
    void testHighPriorityCustomerTrigger() {
        // Arrange - Priority high
        EscalationRule rule = new EscalationRule();
        rule.setName("High Priority Rule");
        rule.setTriggerType(EscalationRule.TriggerType.HIGH_PRIORITY_CUSTOMER);
        rule.setTriggerValue("high");
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        rule.setEscalateToSupervisor(true);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        escalationRuleRepository.save(rule);

        EscalationContext context = new EscalationContext();
        context.setConversationId(TEST_CONVERSATION_ID);
        context.setCustomerPriority("high");

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert - Escalation executed
        assertNotNull(result);
        assertTrue(result.isEscalated());
    }

    @Test
    void testEscalationToAgent() {
        // Arrange - Action = ASSIGN_TO_AGENT
        EscalationRule rule = new EscalationRule();
        rule.setName("Escalate to Agent Rule");
        rule.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        rule.setTimeThresholdMinutes(30);
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_AGENT);
        rule.setEscalateToAgentId(TEST_AGENT_ID);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        escalationRuleRepository.save(rule);

        EscalationContext context = new EscalationContext();
        context.setConversationId(TEST_CONVERSATION_ID);
        context.setWaitTimeMinutes(35);

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert - Assignment target agent set
        assertNotNull(result);
        assertTrue(result.isEscalated());
        assertEquals(TEST_AGENT_ID, result.getEscalatedToAgentId());
        assertEquals("ASSIGN_TO_AGENT", result.getEscalationAction());
    }

    @Test
    void testEscalationToTeam() {
        // Arrange - Action = ASSIGN_TO_TEAM
        Long testTeamId = 100L;
        EscalationRule rule = new EscalationRule();
        rule.setName("Escalate to Team Rule");
        rule.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        rule.setTimeThresholdMinutes(30);
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_TEAM);
        rule.setEscalateToTeamId(testTeamId);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        escalationRuleRepository.save(rule);

        EscalationContext context = new EscalationContext();
        context.setConversationId(TEST_CONVERSATION_ID);
        context.setWaitTimeMinutes(35);

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert - Team assigned
        assertNotNull(result);
        assertTrue(result.isEscalated());
        assertEquals(testTeamId, result.getEscalatedToTeamId());
        assertEquals("ASSIGN_TO_TEAM", result.getEscalationAction());
    }

    @Test
    void testEscalationToSupervisor() {
        // Arrange - Action = ASSIGN_TO_SUPERVISOR
        EscalationRule rule = new EscalationRule();
        rule.setName("Escalate to Supervisor Rule");
        rule.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        rule.setTimeThresholdMinutes(30);
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        rule.setEscalateToSupervisor(true);
        rule.setNotifySupervisor(true);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        escalationRuleRepository.save(rule);

        EscalationContext context = new EscalationContext();
        context.setConversationId(TEST_CONVERSATION_ID);
        context.setWaitTimeMinutes(35);

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert - Supervisor notified or session created
        assertNotNull(result);
        assertTrue(result.isEscalated());
        assertTrue(result.isEscalatedToSupervisor());
        assertEquals("ASSIGN_TO_SUPERVISOR", result.getEscalationAction());
    }

    @Test
    void testNoTriggerScenario() {
        // Arrange - Conditions not met
        EscalationRule rule = new EscalationRule();
        rule.setName("Wait Time Rule");
        rule.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        rule.setTimeThresholdMinutes(30);
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        escalationRuleRepository.save(rule);

        EscalationContext context = new EscalationContext();
        context.setConversationId(TEST_CONVERSATION_ID);
        context.setWaitTimeMinutes(10); // Below threshold

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert - No escalation created
        assertNotNull(result);
        assertFalse(result.isEscalated());
        assertTrue(result.getReason().contains("No escalation rules matched"));
    }

    @Test
    void testHistorySavedAfterEscalation() {
        // Arrange
        EscalationRule rule = new EscalationRule();
        rule.setName("History Test Rule");
        rule.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        rule.setTimeThresholdMinutes(30);
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_AGENT);
        rule.setEscalateToAgentId(TEST_AGENT_ID);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        escalationRuleRepository.save(rule);

        EscalationContext context = new EscalationContext();
        context.setConversationId(TEST_CONVERSATION_ID);
        context.setWaitTimeMinutes(35);

        // Act
        EscalationResult result = escalationService.evaluateEscalation(TEST_CONVERSATION_ID, context);

        // Assert - Escalation record persisted (verify via getEscalationHistory)
        assertNotNull(result);
        assertTrue(result.isEscalated());
        assertNotNull(result.getEscalatedAt());
        assertNotNull(result.getEscalationRuleId());

        // Verify history can be retrieved
        var history = escalationService.getEscalationHistory(TEST_CONVERSATION_ID);
        assertNotNull(history);
        assertFalse(history.isEmpty());
    }
}
