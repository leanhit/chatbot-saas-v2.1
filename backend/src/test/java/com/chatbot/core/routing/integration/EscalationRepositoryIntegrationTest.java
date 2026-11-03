package com.chatbot.core.routing.integration;

import com.chatbot.core.routing.model.EscalationRule;
import com.chatbot.core.routing.repository.IntelligentEscalationRuleRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ImportAutoConfiguration(exclude = {FlywayAutoConfiguration.class})
@Disabled("ApplicationContext loading issues - requires full Spring context configuration")
class EscalationRepositoryIntegrationTest {

    @Autowired
    private IntelligentEscalationRuleRepository escalationRuleRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static final Long TEST_TENANT_ID = 1L;
    private static final Long TEST_TENANT_2_ID = 2L;

    @BeforeEach
    void setUp() {
        escalationRuleRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
        TenantContext.setTenantId(TEST_TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testSaveEscalationRule_Success() {
        // Arrange
        EscalationRule rule = new EscalationRule();
        rule.setName("Test Escalation Rule");
        rule.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        // Act
        EscalationRule saved = escalationRuleRepository.save(rule);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Test Escalation Rule", saved.getName());
        assertEquals(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED, saved.getTriggerType());
        assertEquals(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR, saved.getEscalationAction());
        assertEquals(1, saved.getPriority());
        assertTrue(saved.getIsActive());
        assertEquals(TEST_TENANT_ID, saved.getTenantId());
    }

    @Test
    void testQueryByTriggerType() {
        // Arrange
        EscalationRule rule1 = new EscalationRule();
        rule1.setName("Wait Time Rule");
        rule1.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        rule1.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        rule1.setPriority(1);
        rule1.setIsActive(true);
        rule1.setTenantId(TEST_TENANT_ID);

        EscalationRule rule2 = new EscalationRule();
        rule2.setName("No Response Rule");
        rule2.setTriggerType(EscalationRule.TriggerType.NO_RESPONSE);
        rule2.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_AGENT);
        rule2.setPriority(2);
        rule2.setIsActive(true);
        rule2.setTenantId(TEST_TENANT_ID);

        escalationRuleRepository.save(rule1);
        escalationRuleRepository.save(rule2);

        // Act
        List<EscalationRule> result = escalationRuleRepository.findByTenantIdAndTriggerTypeAndIsActiveTrue(
                TEST_TENANT_ID, EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);

        // Assert
        assertEquals(1, result.size());
        assertEquals(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED, result.get(0).getTriggerType());
    }

    @Test
    void testQueryActiveEscalationRulesOnly() {
        // Arrange
        EscalationRule active1 = new EscalationRule();
        active1.setName("Active Rule 1");
        active1.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        active1.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        active1.setPriority(1);
        active1.setIsActive(true);
        active1.setTenantId(TEST_TENANT_ID);

        EscalationRule active2 = new EscalationRule();
        active2.setName("Active Rule 2");
        active2.setTriggerType(EscalationRule.TriggerType.NO_RESPONSE);
        active2.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_AGENT);
        active2.setPriority(2);
        active2.setIsActive(true);
        active2.setTenantId(TEST_TENANT_ID);

        EscalationRule inactive = new EscalationRule();
        inactive.setName("Inactive Rule");
        inactive.setTriggerType(EscalationRule.TriggerType.LOW_CUSTOMER_SATISFACTION);
        inactive.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        inactive.setPriority(3);
        inactive.setIsActive(false);
        inactive.setTenantId(TEST_TENANT_ID);

        escalationRuleRepository.save(active1);
        escalationRuleRepository.save(active2);
        escalationRuleRepository.save(inactive);

        // Act
        List<EscalationRule> result = escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(
                TEST_TENANT_ID);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(EscalationRule::getIsActive));
    }

    @Test
    void testQueryByEscalationAction() {
        // Arrange
        EscalationRule assignToAgent = new EscalationRule();
        assignToAgent.setName("Assign to Agent Rule");
        assignToAgent.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        assignToAgent.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_AGENT);
        assignToAgent.setPriority(1);
        assignToAgent.setIsActive(true);
        assignToAgent.setTenantId(TEST_TENANT_ID);

        EscalationRule assignToSupervisor = new EscalationRule();
        assignToSupervisor.setName("Assign to Supervisor Rule");
        assignToSupervisor.setTriggerType(EscalationRule.TriggerType.NO_RESPONSE);
        assignToSupervisor.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        assignToSupervisor.setPriority(2);
        assignToSupervisor.setIsActive(true);
        assignToSupervisor.setTenantId(TEST_TENANT_ID);

        escalationRuleRepository.save(assignToAgent);
        escalationRuleRepository.save(assignToSupervisor);

        // Act
        List<EscalationRule> result = escalationRuleRepository.findByTenantIdAndEscalationActionAndIsActiveTrue(
                TEST_TENANT_ID, EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);

        // Assert
        assertEquals(1, result.size());
        assertEquals(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR, result.get(0).getEscalationAction());
    }

    @Test
    void testEscalateToSupervisorFlag() {
        // Arrange
        EscalationRule rule = new EscalationRule();
        rule.setName("Escalate to Supervisor Rule");
        rule.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        rule.setEscalateToSupervisor(true);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        // Act
        EscalationRule saved = escalationRuleRepository.save(rule);

        // Assert
        assertTrue(saved.getEscalateToSupervisor());
    }

    @Test
    void testEscalateToTeamFlag() {
        // Arrange
        Long testTeamId = 100L;
        EscalationRule rule = new EscalationRule();
        rule.setName("Escalate to Team Rule");
        rule.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_TEAM);
        rule.setEscalateToTeamId(testTeamId);
        rule.setPriority(1);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        // Act
        EscalationRule saved = escalationRuleRepository.save(rule);

        // Assert
        assertEquals(testTeamId, saved.getEscalateToTeamId());
    }

    @Test
    void testPriorityOrdering() {
        // Arrange
        EscalationRule rule1 = new EscalationRule();
        rule1.setName("Priority 1");
        rule1.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        rule1.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        rule1.setPriority(5);
        rule1.setIsActive(true);
        rule1.setTenantId(TEST_TENANT_ID);

        EscalationRule rule2 = new EscalationRule();
        rule2.setName("Priority 2");
        rule2.setTriggerType(EscalationRule.TriggerType.NO_RESPONSE);
        rule2.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_AGENT);
        rule2.setPriority(1);
        rule2.setIsActive(true);
        rule2.setTenantId(TEST_TENANT_ID);

        EscalationRule rule3 = new EscalationRule();
        rule3.setName("Priority 3");
        rule3.setTriggerType(EscalationRule.TriggerType.LOW_CUSTOMER_SATISFACTION);
        rule3.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        rule3.setPriority(3);
        rule3.setIsActive(true);
        rule3.setTenantId(TEST_TENANT_ID);

        escalationRuleRepository.save(rule1);
        escalationRuleRepository.save(rule2);
        escalationRuleRepository.save(rule3);

        // Act
        List<EscalationRule> result = escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(
                TEST_TENANT_ID);

        // Assert - Order should be 1, 3, 5
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getPriority());
        assertEquals(3, result.get(1).getPriority());
        assertEquals(5, result.get(2).getPriority());
    }

    @Test
    void testTenantIsolationOnEscalationRules() {
        // Arrange
        EscalationRule tenant1Rule = new EscalationRule();
        tenant1Rule.setName("Tenant 1 Rule");
        tenant1Rule.setTriggerType(EscalationRule.TriggerType.WAIT_TIME_EXCEEDED);
        tenant1Rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_SUPERVISOR);
        tenant1Rule.setPriority(1);
        tenant1Rule.setIsActive(true);
        tenant1Rule.setTenantId(TEST_TENANT_ID);

        EscalationRule tenant2Rule = new EscalationRule();
        tenant2Rule.setName("Tenant 2 Rule");
        tenant2Rule.setTriggerType(EscalationRule.TriggerType.NO_RESPONSE);
        tenant2Rule.setEscalationAction(EscalationRule.EscalationAction.ASSIGN_TO_AGENT);
        tenant2Rule.setPriority(1);
        tenant2Rule.setIsActive(true);
        tenant2Rule.setTenantId(TEST_TENANT_2_ID);

        escalationRuleRepository.save(tenant1Rule);
        escalationRuleRepository.save(tenant2Rule);

        // Act
        List<EscalationRule> tenant1Results = escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(
                TEST_TENANT_ID);

        // Assert
        assertEquals(1, tenant1Results.size());
        assertEquals(TEST_TENANT_ID, tenant1Results.get(0).getTenantId());
        assertFalse(tenant1Results.stream().anyMatch(r -> r.getTenantId().equals(TEST_TENANT_2_ID)));
    }
}
