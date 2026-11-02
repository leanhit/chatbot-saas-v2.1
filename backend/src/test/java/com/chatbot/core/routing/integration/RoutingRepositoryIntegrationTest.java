package com.chatbot.core.routing.integration;

import com.chatbot.core.routing.model.RoutingRule;
import com.chatbot.core.routing.repository.IntelligentRoutingRuleRepository;
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
@Disabled("Requires full Spring context - ApplicationContext loading issues in Docker environment")
class RoutingRepositoryIntegrationTest {

    @Autowired
    private IntelligentRoutingRuleRepository routingRuleRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static final Long TEST_TENANT_ID = 1L;

    @BeforeEach
    void setUp() {
        routingRuleRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
        TenantContext.setTenantId(TEST_TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testSaveRoutingRule() {
        // Arrange
        RoutingRule rule = new RoutingRule();
        rule.setName("Test Rule");
        rule.setDescription("Test Description");
        rule.setRuleType(RoutingRule.RoutingRuleType.SKILL_BASED);
        rule.setPriority(10);
        rule.setTargetType(RoutingRule.TargetType.AGENT);
        rule.setTargetAgentId(100L);
        rule.setIsActive(true);
        rule.setIsDefault(false);
        rule.setTenantId(TEST_TENANT_ID);

        // Act
        RoutingRule saved = routingRuleRepository.save(rule);

        // Assert
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Test Rule", saved.getName());
        assertEquals(TEST_TENANT_ID, saved.getTenantId());
    }

    @Test
    void testFindByTenantIdAndIsActiveTrueOrderByPriorityAsc() {
        // Arrange - Create 3 rules with priorities 10, 20, 5
        RoutingRule rule1 = new RoutingRule();
        rule1.setName("Rule 1");
        rule1.setPriority(10);
        rule1.setRuleType(RoutingRule.RoutingRuleType.SKILL_BASED);
        rule1.setTargetType(RoutingRule.TargetType.AGENT);
        rule1.setIsActive(true);
        rule1.setTenantId(TEST_TENANT_ID);

        RoutingRule rule2 = new RoutingRule();
        rule2.setName("Rule 2");
        rule2.setPriority(20);
        rule2.setRuleType(RoutingRule.RoutingRuleType.PRIORITY_BASED);
        rule2.setTargetType(RoutingRule.TargetType.AGENT);
        rule2.setIsActive(true);
        rule2.setTenantId(TEST_TENANT_ID);

        RoutingRule rule3 = new RoutingRule();
        rule3.setName("Rule 3");
        rule3.setPriority(5);
        rule3.setRuleType(RoutingRule.RoutingRuleType.ROUND_ROBIN);
        rule3.setTargetType(RoutingRule.TargetType.AGENT);
        rule3.setIsActive(true);
        rule3.setTenantId(TEST_TENANT_ID);

        routingRuleRepository.save(rule1);
        routingRuleRepository.save(rule2);
        routingRuleRepository.save(rule3);

        // Act
        List<RoutingRule> rules = routingRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID);

        // Assert - Order should be 5, 10, 20
        assertEquals(3, rules.size());
        assertEquals(5, rules.get(0).getPriority());
        assertEquals(10, rules.get(1).getPriority());
        assertEquals(20, rules.get(2).getPriority());
    }

    @Test
    void testFindByTenantIdAndRuleTypeAndIsActiveTrue() {
        // Arrange - Create SKILL_BASED and PRIORITY_BASED rules
        RoutingRule skillRule = new RoutingRule();
        skillRule.setName("Skill Rule");
        skillRule.setRuleType(RoutingRule.RoutingRuleType.SKILL_BASED);
        skillRule.setPriority(1);
        skillRule.setTargetType(RoutingRule.TargetType.AGENT);
        skillRule.setIsActive(true);
        skillRule.setTenantId(TEST_TENANT_ID);

        RoutingRule priorityRule = new RoutingRule();
        priorityRule.setName("Priority Rule");
        priorityRule.setRuleType(RoutingRule.RoutingRuleType.PRIORITY_BASED);
        priorityRule.setPriority(2);
        priorityRule.setTargetType(RoutingRule.TargetType.AGENT);
        priorityRule.setIsActive(true);
        priorityRule.setTenantId(TEST_TENANT_ID);

        routingRuleRepository.save(skillRule);
        routingRuleRepository.save(priorityRule);

        // Act - Query ruleType SKILL_BASED
        List<RoutingRule> skillRules = routingRuleRepository.findByTenantIdAndRuleTypeAndIsActiveTrue(
                TEST_TENANT_ID, RoutingRule.RoutingRuleType.SKILL_BASED);

        // Assert - Only SKILL_BASED returned
        assertEquals(1, skillRules.size());
        assertEquals(RoutingRule.RoutingRuleType.SKILL_BASED, skillRules.get(0).getRuleType());
    }

    @Test
    void testFindByTenantIdAndTargetTypeAndIsActiveTrue() {
        // Arrange - Create AGENT, TEAM, SKILL rules
        RoutingRule agentRule = new RoutingRule();
        agentRule.setName("Agent Rule");
        agentRule.setRuleType(RoutingRule.RoutingRuleType.SKILL_BASED);
        agentRule.setPriority(1);
        agentRule.setTargetType(RoutingRule.TargetType.AGENT);
        agentRule.setIsActive(true);
        agentRule.setTenantId(TEST_TENANT_ID);

        RoutingRule teamRule = new RoutingRule();
        teamRule.setName("Team Rule");
        teamRule.setRuleType(RoutingRule.RoutingRuleType.PRIORITY_BASED);
        teamRule.setPriority(2);
        teamRule.setTargetType(RoutingRule.TargetType.TEAM);
        teamRule.setIsActive(true);
        teamRule.setTenantId(TEST_TENANT_ID);

        RoutingRule skillRule = new RoutingRule();
        skillRule.setName("Skill Rule");
        skillRule.setRuleType(RoutingRule.RoutingRuleType.ROUND_ROBIN);
        skillRule.setPriority(3);
        skillRule.setTargetType(RoutingRule.TargetType.SKILL);
        skillRule.setIsActive(true);
        skillRule.setTenantId(TEST_TENANT_ID);

        routingRuleRepository.save(agentRule);
        routingRuleRepository.save(teamRule);
        routingRuleRepository.save(skillRule);

        // Act - Query TEAM
        List<RoutingRule> teamRules = routingRuleRepository.findByTenantIdAndTargetTypeAndIsActiveTrue(
                TEST_TENANT_ID, RoutingRule.TargetType.TEAM);

        // Assert - Only 1 entity
        assertEquals(1, teamRules.size());
        assertEquals(RoutingRule.TargetType.TEAM, teamRules.get(0).getTargetType());
    }

    @Test
    void testFindByTenantIdAndIsActiveTrueAndIsDefaultTrue() {
        // Arrange - Create default rule true, false
        RoutingRule defaultRule = new RoutingRule();
        defaultRule.setName("Default Rule");
        defaultRule.setRuleType(RoutingRule.RoutingRuleType.SKILL_BASED);
        defaultRule.setPriority(1);
        defaultRule.setTargetType(RoutingRule.TargetType.AGENT);
        defaultRule.setIsActive(true);
        defaultRule.setIsDefault(true);
        defaultRule.setTenantId(TEST_TENANT_ID);

        RoutingRule nonDefaultRule = new RoutingRule();
        nonDefaultRule.setName("Non-Default Rule");
        nonDefaultRule.setRuleType(RoutingRule.RoutingRuleType.PRIORITY_BASED);
        nonDefaultRule.setPriority(2);
        nonDefaultRule.setTargetType(RoutingRule.TargetType.AGENT);
        nonDefaultRule.setIsActive(true);
        nonDefaultRule.setIsDefault(false);
        nonDefaultRule.setTenantId(TEST_TENANT_ID);

        routingRuleRepository.save(defaultRule);
        routingRuleRepository.save(nonDefaultRule);

        // Act - Query default rules
        List<RoutingRule> defaultRules = routingRuleRepository.findByTenantIdAndIsActiveTrueAndIsDefaultTrue(TEST_TENANT_ID);

        // Assert - Only default true
        assertEquals(1, defaultRules.size());
        assertTrue(defaultRules.get(0).getIsDefault());
    }

    @Test
    void testFindFirstByTenantIdAndIsActiveTrueOrderByPriorityAsc() {
        // Arrange - Create rules with different priorities
        RoutingRule rule1 = new RoutingRule();
        rule1.setName("Rule 1");
        rule1.setPriority(10);
        rule1.setRuleType(RoutingRule.RoutingRuleType.SKILL_BASED);
        rule1.setTargetType(RoutingRule.TargetType.AGENT);
        rule1.setIsActive(true);
        rule1.setTenantId(TEST_TENANT_ID);

        RoutingRule rule2 = new RoutingRule();
        rule2.setName("Rule 2");
        rule2.setPriority(5);
        rule2.setRuleType(RoutingRule.RoutingRuleType.PRIORITY_BASED);
        rule2.setTargetType(RoutingRule.TargetType.AGENT);
        rule2.setIsActive(true);
        rule2.setTenantId(TEST_TENANT_ID);

        routingRuleRepository.save(rule1);
        routingRuleRepository.save(rule2);

        // Act
        Optional<RoutingRule> firstRule = routingRuleRepository.findFirstByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID);

        // Assert - Lowest priority returned
        assertTrue(firstRule.isPresent());
        assertEquals(5, firstRule.get().getPriority());
    }

    @Test
    void testQueryActiveOnly() {
        // Arrange - Create isActive true + false
        RoutingRule activeRule = new RoutingRule();
        activeRule.setName("Active Rule");
        activeRule.setPriority(1);
        activeRule.setRuleType(RoutingRule.RoutingRuleType.SKILL_BASED);
        activeRule.setTargetType(RoutingRule.TargetType.AGENT);
        activeRule.setIsActive(true);
        activeRule.setTenantId(TEST_TENANT_ID);

        RoutingRule inactiveRule = new RoutingRule();
        inactiveRule.setName("Inactive Rule");
        inactiveRule.setPriority(2);
        inactiveRule.setRuleType(RoutingRule.RoutingRuleType.PRIORITY_BASED);
        inactiveRule.setTargetType(RoutingRule.TargetType.AGENT);
        inactiveRule.setIsActive(false);
        inactiveRule.setTenantId(TEST_TENANT_ID);

        routingRuleRepository.save(activeRule);
        routingRuleRepository.save(inactiveRule);

        // Act - Query active rules
        List<RoutingRule> activeRules = routingRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID);

        // Assert - No inactive returned
        assertEquals(1, activeRules.size());
        assertTrue(activeRules.get(0).getIsActive());
    }

    @Test
    void testQueryByTenantIsolation() {
        // Arrange - Tenant 1 rule + tenant 2 rule
        RoutingRule tenant1Rule = new RoutingRule();
        tenant1Rule.setName("Tenant 1 Rule");
        tenant1Rule.setPriority(1);
        tenant1Rule.setRuleType(RoutingRule.RoutingRuleType.SKILL_BASED);
        tenant1Rule.setTargetType(RoutingRule.TargetType.AGENT);
        tenant1Rule.setIsActive(true);
        tenant1Rule.setTenantId(1L);

        RoutingRule tenant2Rule = new RoutingRule();
        tenant2Rule.setName("Tenant 2 Rule");
        tenant2Rule.setPriority(1);
        tenant2Rule.setRuleType(RoutingRule.RoutingRuleType.PRIORITY_BASED);
        tenant2Rule.setTargetType(RoutingRule.TargetType.AGENT);
        tenant2Rule.setIsActive(true);
        tenant2Rule.setTenantId(2L);

        routingRuleRepository.save(tenant1Rule);
        routingRuleRepository.save(tenant2Rule);

        // Act - Query tenant 1
        List<RoutingRule> tenant1Rules = routingRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(1L);

        // Assert - Cannot see tenant 2
        assertEquals(1, tenant1Rules.size());
        assertEquals("Tenant 1 Rule", tenant1Rules.get(0).getName());
        assertEquals(1L, tenant1Rules.get(0).getTenantId());
    }
}
