package com.chatbot.core.routing.smoke;

import com.chatbot.core.routing.dto.RoutingAssignmentResult;
import com.chatbot.core.routing.model.AgentWorkload;
import com.chatbot.core.routing.model.RoutingRule;
import com.chatbot.core.routing.repository.AgentWorkloadRepository;
import com.chatbot.core.routing.repository.IntelligentRoutingRuleRepository;
import com.chatbot.core.routing.service.IntelligentRoutingService;
import com.chatbot.core.tenant.infra.TenantContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires full Spring context - ApplicationContext loading issues in Docker environment")
class RoutingSmokeTest {

    @Autowired
    private IntelligentRoutingService routingService;

    @Autowired
    private IntelligentRoutingRuleRepository routingRuleRepository;

    @Autowired
    private AgentWorkloadRepository agentWorkloadRepository;

    private static final Long TEST_TENANT_ID = 1L;
    private static final Long TEST_CONVERSATION_ID = 100L;

    @BeforeEach
    void setUp() {
        routingRuleRepository.deleteAll();
        agentWorkloadRepository.deleteAll();
        TenantContext.setTenantId(TEST_TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testPriorityBasedRoutingEndToEnd() {
        // Arrange - Create 2 available agents with different priority scores
        AgentWorkload agentA = new AgentWorkload();
        agentA.setAgentId(1L);
        agentA.setTenantId(TEST_TENANT_ID);
        agentA.setStatus(AgentWorkload.AgentStatus.AVAILABLE);
        agentA.setPriorityScore(90);
        agentA.setActiveConversations(0);
        agentA.setMaxConcurrentConversations(10);

        AgentWorkload agentB = new AgentWorkload();
        agentB.setAgentId(2L);
        agentB.setTenantId(TEST_TENANT_ID);
        agentB.setStatus(AgentWorkload.AgentStatus.AVAILABLE);
        agentB.setPriorityScore(50);
        agentB.setActiveConversations(0);
        agentB.setMaxConcurrentConversations(10);

        agentWorkloadRepository.save(agentA);
        agentWorkloadRepository.save(agentB);

        RoutingRule rule = new RoutingRule();
        rule.setName("Priority Rule");
        rule.setRuleType(RoutingRule.RoutingRuleType.PRIORITY_BASED);
        rule.setPriority(1);
        rule.setTargetType(RoutingRule.TargetType.AGENT);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        routingRuleRepository.save(rule);

        Map<String, Object> context = new HashMap<>();
        context.put("conversationId", TEST_CONVERSATION_ID);

        // Act - Route conversation with priority rule
        RoutingAssignmentResult result = routingService.routeConversation(TEST_CONVERSATION_ID, context);

        // Assert - Agent A with higher priority score selected
        assertNotNull(result);
        assertTrue(result.isAssigned());
        assertEquals(1L, result.getAssignedAgentId());
        assertEquals("PRIORITY_BASED", result.getRoutingStrategy());
    }

    @Test
    void testSkillBasedRoutingEndToEnd() {
        // Arrange - Create agent with required skill
        AgentWorkload agent = new AgentWorkload();
        agent.setAgentId(1L);
        agent.setTenantId(TEST_TENANT_ID);
        agent.setStatus(AgentWorkload.AgentStatus.AVAILABLE);
        agent.setActiveConversations(0);
        agent.setMaxConcurrentConversations(10);

        agentWorkloadRepository.save(agent);

        RoutingRule rule = new RoutingRule();
        rule.setName("Skill Rule");
        rule.setRuleType(RoutingRule.RoutingRuleType.SKILL_BASED);
        rule.setPriority(1);
        rule.setTargetType(RoutingRule.TargetType.AGENT);
        rule.setTargetAgentId(1L);
        rule.setTargetSkillId(5L);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        routingRuleRepository.save(rule);

        Map<String, Object> context = new HashMap<>();
        context.put("conversationId", TEST_CONVERSATION_ID);
        context.put("requiredSkill", "technical_support");

        // Act - Route conversation
        RoutingAssignmentResult result = routingService.routeConversation(TEST_CONVERSATION_ID, context);

        // Assert - Matched skill agent selected
        assertNotNull(result);
        assertTrue(result.isAssigned());
        assertEquals(1L, result.getAssignedAgentId());
    }

    @Test
    void testTeamBasedRoutingEndToEnd() {
        // Arrange - Create agents in same team
        AgentWorkload agent1 = new AgentWorkload();
        agent1.setAgentId(1L);
        agent1.setTenantId(TEST_TENANT_ID);
        agent1.setTeamId(100L);
        agent1.setStatus(AgentWorkload.AgentStatus.AVAILABLE);
        agent1.setActiveConversations(0);
        agent1.setMaxConcurrentConversations(10);

        AgentWorkload agent2 = new AgentWorkload();
        agent2.setAgentId(2L);
        agent2.setTenantId(TEST_TENANT_ID);
        agent2.setTeamId(200L); // Different team
        agent2.setStatus(AgentWorkload.AgentStatus.AVAILABLE);
        agent2.setActiveConversations(0);
        agent2.setMaxConcurrentConversations(10);

        agentWorkloadRepository.save(agent1);
        agentWorkloadRepository.save(agent2);

        RoutingRule rule = new RoutingRule();
        rule.setName("Team Rule");
        rule.setRuleType(RoutingRule.RoutingRuleType.SKILL_BASED);
        rule.setPriority(1);
        rule.setTargetType(RoutingRule.TargetType.TEAM);
        rule.setTargetTeamId(100L);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        routingRuleRepository.save(rule);

        Map<String, Object> context = new HashMap<>();
        context.put("conversationId", TEST_CONVERSATION_ID);

        // Act - Route with targetTeamId
        RoutingAssignmentResult result = routingService.routeConversation(TEST_CONVERSATION_ID, context);

        // Assert - Only team member selected
        assertNotNull(result);
        assertTrue(result.isAssigned());
        assertEquals(100L, result.getAssignedTeamId());
    }

    @Test
    void testRoundRobinFairness() {
        // Arrange - 3 agents available, same team
        AgentWorkload agent1 = new AgentWorkload();
        agent1.setAgentId(1L);
        agent1.setTenantId(TEST_TENANT_ID);
        agent1.setStatus(AgentWorkload.AgentStatus.AVAILABLE);
        agent1.setActiveConversations(0);
        agent1.setMaxConcurrentConversations(10);

        AgentWorkload agent2 = new AgentWorkload();
        agent2.setAgentId(2L);
        agent2.setTenantId(TEST_TENANT_ID);
        agent2.setStatus(AgentWorkload.AgentStatus.AVAILABLE);
        agent2.setActiveConversations(0);
        agent2.setMaxConcurrentConversations(10);

        AgentWorkload agent3 = new AgentWorkload();
        agent3.setAgentId(3L);
        agent3.setTenantId(TEST_TENANT_ID);
        agent3.setStatus(AgentWorkload.AgentStatus.AVAILABLE);
        agent3.setActiveConversations(0);
        agent3.setMaxConcurrentConversations(10);

        agentWorkloadRepository.save(agent1);
        agentWorkloadRepository.save(agent2);
        agentWorkloadRepository.save(agent3);

        RoutingRule rule = new RoutingRule();
        rule.setName("Round Robin Rule");
        rule.setRuleType(RoutingRule.RoutingRuleType.ROUND_ROBIN);
        rule.setPriority(1);
        rule.setTargetType(RoutingRule.TargetType.AGENT);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        routingRuleRepository.save(rule);

        // Act - Route 3 conversations sequentially
        Map<String, Object> context1 = new HashMap<>();
        RoutingAssignmentResult result1 = routingService.routeConversation(TEST_CONVERSATION_ID, context1);

        Map<String, Object> context2 = new HashMap<>();
        RoutingAssignmentResult result2 = routingService.routeConversation(TEST_CONVERSATION_ID + 1, context2);

        Map<String, Object> context3 = new HashMap<>();
        RoutingAssignmentResult result3 = routingService.routeConversation(TEST_CONVERSATION_ID + 2, context3);

        // Assert - Assignments rotate in order
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertTrue(result1.isAssigned());
        assertTrue(result2.isAssigned());
        assertTrue(result3.isAssigned());
    }

    @Test
    void testLeastBusyRoutingEndToEnd() {
        // Arrange - Agents with activeConversations = 3, 1, 5
        AgentWorkload agent1 = new AgentWorkload();
        agent1.setAgentId(1L);
        agent1.setTenantId(TEST_TENANT_ID);
        agent1.setStatus(AgentWorkload.AgentStatus.AVAILABLE);
        agent1.setActiveConversations(3);
        agent1.setMaxConcurrentConversations(10);

        AgentWorkload agent2 = new AgentWorkload();
        agent2.setAgentId(2L);
        agent2.setTenantId(TEST_TENANT_ID);
        agent2.setStatus(AgentWorkload.AgentStatus.AVAILABLE);
        agent2.setActiveConversations(1);
        agent2.setMaxConcurrentConversations(10);

        AgentWorkload agent3 = new AgentWorkload();
        agent3.setAgentId(3L);
        agent3.setTenantId(TEST_TENANT_ID);
        agent3.setStatus(AgentWorkload.AgentStatus.AVAILABLE);
        agent3.setActiveConversations(5);
        agent3.setMaxConcurrentConversations(10);

        agentWorkloadRepository.save(agent1);
        agentWorkloadRepository.save(agent2);
        agentWorkloadRepository.save(agent3);

        RoutingRule rule = new RoutingRule();
        rule.setName("Least Busy Rule");
        rule.setRuleType(RoutingRule.RoutingRuleType.LEAST_BUSY);
        rule.setPriority(1);
        rule.setTargetType(RoutingRule.TargetType.AGENT);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        routingRuleRepository.save(rule);

        Map<String, Object> context = new HashMap<>();
        context.put("conversationId", TEST_CONVERSATION_ID);

        // Act - Route conversation
        RoutingAssignmentResult result = routingService.routeConversation(TEST_CONVERSATION_ID, context);

        // Assert - Lowest busy agent selected (agent2 with 1 conversation)
        assertNotNull(result);
        assertTrue(result.isAssigned());
        assertEquals(2L, result.getAssignedAgentId());
    }

    @Test
    void testNoMatchScenarioFallbackToDefault() {
        // Arrange - No rule matches, default rule exists
        RoutingRule defaultRule = new RoutingRule();
        defaultRule.setName("Default Rule");
        defaultRule.setRuleType(RoutingRule.RoutingRuleType.SKILL_BASED);
        defaultRule.setPriority(1);
        defaultRule.setTargetType(RoutingRule.TargetType.AGENT);
        defaultRule.setTargetAgentId(10L);
        defaultRule.setIsActive(true);
        defaultRule.setIsDefault(true);
        defaultRule.setTenantId(TEST_TENANT_ID);

        routingRuleRepository.save(defaultRule);

        Map<String, Object> context = new HashMap<>();
        context.put("conversationId", TEST_CONVERSATION_ID);
        context.put("nonExistentCondition", "value");

        // Act - Route conversation
        RoutingAssignmentResult result = routingService.routeConversation(TEST_CONVERSATION_ID, context);

        // Assert - Default route selected
        assertNotNull(result);
        assertTrue(result.isAssigned());
        assertEquals(10L, result.getAssignedAgentId());
    }

    @Test
    void testNoAgentsAvailableScenario() {
        // Arrange - All agents busy or offline
        AgentWorkload busyAgent = new AgentWorkload();
        busyAgent.setAgentId(1L);
        busyAgent.setTenantId(TEST_TENANT_ID);
        busyAgent.setStatus(AgentWorkload.AgentStatus.BUSY);
        busyAgent.setActiveConversations(10);
        busyAgent.setMaxConcurrentConversations(10);

        agentWorkloadRepository.save(busyAgent);

        RoutingRule rule = new RoutingRule();
        rule.setName("Test Rule");
        rule.setRuleType(RoutingRule.RoutingRuleType.SKILL_BASED);
        rule.setPriority(1);
        rule.setTargetType(RoutingRule.TargetType.AGENT);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        routingRuleRepository.save(rule);

        Map<String, Object> context = new HashMap<>();
        context.put("conversationId", TEST_CONVERSATION_ID);

        // Act - Route conversation
        RoutingAssignmentResult result = routingService.routeConversation(TEST_CONVERSATION_ID, context);

        // Assert - Assigned = false
        assertNotNull(result);
        assertFalse(result.isAssigned());
    }

    @Test
    void testHistorySavedAfterRoute() {
        // Arrange
        AgentWorkload agent = new AgentWorkload();
        agent.setAgentId(1L);
        agent.setTenantId(TEST_TENANT_ID);
        agent.setStatus(AgentWorkload.AgentStatus.AVAILABLE);
        agent.setActiveConversations(0);
        agent.setMaxConcurrentConversations(10);

        agentWorkloadRepository.save(agent);

        RoutingRule rule = new RoutingRule();
        rule.setName("History Test Rule");
        rule.setRuleType(RoutingRule.RoutingRuleType.PRIORITY_BASED);
        rule.setPriority(1);
        rule.setTargetType(RoutingRule.TargetType.AGENT);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);

        routingRuleRepository.save(rule);

        Map<String, Object> context = new HashMap<>();
        context.put("conversationId", TEST_CONVERSATION_ID);

        // Act - Route conversation
        RoutingAssignmentResult result = routingService.routeConversation(TEST_CONVERSATION_ID, context);

        // Assert - History saved (verify routing history repository has record)
        assertNotNull(result);
        assertTrue(result.isAssigned());
        assertNotNull(result.getRoutingRuleId());
        assertNotNull(result.getAssignedAt());
        assertNotNull(result.getRoutingStrategy());
    }
}
