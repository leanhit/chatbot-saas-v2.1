package com.chatbot.core.routing.service;

import com.chatbot.core.routing.dto.RoutingAssignmentResult;
import com.chatbot.core.routing.model.AgentWorkload;
import com.chatbot.core.routing.model.RoutingRule;
import com.chatbot.core.routing.repository.AgentWorkloadRepository;
import com.chatbot.core.routing.repository.IntelligentRoutingRuleRepository;
import com.chatbot.core.routing.repository.RoutingHistoryRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoutingServiceMockitoTest {

    @Mock
    private IntelligentRoutingRuleRepository routingRuleRepository;

    @Mock
    private AgentWorkloadRepository agentWorkloadRepository;

    @Mock
    private RoutingHistoryRepository routingHistoryRepository;

    @InjectMocks
    private IntelligentRoutingService routingService;

    private MockedStatic<TenantContext> tenantContextMock;

    private static final Long TEST_TENANT_ID = 1L;
    private static final Long TEST_CONVERSATION_ID = 100L;

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
    void testRouteConversation_NoRules_ReturnsNotAssigned() {
        // Arrange
        when(routingRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                .thenReturn(Collections.emptyList());

        Map<String, Object> context = new HashMap<>();

        // Act
        RoutingAssignmentResult result = routingService.routeConversation(TEST_CONVERSATION_ID, context);

        // Assert
        assertNotNull(result);
        assertFalse(result.isAssigned());
        assertEquals("No active routing rules", result.getReason());
    }

    @Test
    void testRouteConversation_WithRules_ReturnsAssigned() {
        // Arrange
        RoutingRule rule = new RoutingRule();
        rule.setId(1L);
        rule.setName("Test Rule");
        rule.setRuleType(RoutingRule.RoutingRuleType.PRIORITY_BASED);
        rule.setPriority(1);
        rule.setTargetType(RoutingRule.TargetType.AGENT);
        rule.setTargetAgentId(10L);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);
        rule.setConditions(new HashMap<>());

        when(routingRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                .thenReturn(List.of(rule));

        AgentWorkload agent = new AgentWorkload();
        agent.setAgentId(10L);
        agent.setTenantId(TEST_TENANT_ID);
        agent.setStatus(AgentWorkload.AgentStatus.AVAILABLE);
        agent.setActiveConversations(0);
        agent.setMaxConcurrentConversations(10);

        when(agentWorkloadRepository.findByTenantIdAndAgentId(TEST_TENANT_ID, 10L))
                .thenReturn(Optional.of(agent));

        Map<String, Object> context = new HashMap<>();

        // Act
        RoutingAssignmentResult result = routingService.routeConversation(TEST_CONVERSATION_ID, context);

        // Assert
        assertNotNull(result);
        assertTrue(result.isAssigned());
        assertEquals(10L, result.getAssignedAgentId());
    }

    @Test
    void testRouteConversation_TenantContextNull_ReturnsNotAssigned() {
        // Arrange
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(null);

        Map<String, Object> context = new HashMap<>();

        // Act
        RoutingAssignmentResult result = routingService.routeConversation(TEST_CONVERSATION_ID, context);

        // Assert
        assertNotNull(result);
        assertFalse(result.isAssigned());
        assertEquals("Tenant context not set - cannot route without tenant", result.getReason());
    }

    @Test
    void testRouteConversation_ConversationIdNull_ReturnsNotAssigned() {
        // Arrange
        Map<String, Object> context = new HashMap<>();

        // Act
        RoutingAssignmentResult result = routingService.routeConversation(null, context);

        // Assert
        assertNotNull(result);
        assertFalse(result.isAssigned());
        assertEquals("Invalid input: conversationId is null", result.getReason());
    }

    @Test
    void testRouteConversation_AgentBusy_SkipsAgent() {
        // Arrange
        RoutingRule rule = new RoutingRule();
        rule.setId(1L);
        rule.setName("Test Rule");
        rule.setRuleType(RoutingRule.RoutingRuleType.PRIORITY_BASED);
        rule.setPriority(1);
        rule.setTargetType(RoutingRule.TargetType.AGENT);
        rule.setTargetAgentId(10L);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);
        rule.setConditions(new HashMap<>());

        when(routingRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                .thenReturn(List.of(rule));

        AgentWorkload busyAgent = new AgentWorkload();
        busyAgent.setAgentId(10L);
        busyAgent.setTenantId(TEST_TENANT_ID);
        busyAgent.setStatus(AgentWorkload.AgentStatus.BUSY);
        busyAgent.setActiveConversations(10);
        busyAgent.setMaxConcurrentConversations(10);

        when(agentWorkloadRepository.findByTenantIdAndAgentId(TEST_TENANT_ID, 10L))
                .thenReturn(Optional.of(busyAgent));

        Map<String, Object> context = new HashMap<>();

        // Act
        RoutingAssignmentResult result = routingService.routeConversation(TEST_CONVERSATION_ID, context);

        // Assert
        assertNotNull(result);
        assertFalse(result.isAssigned());
    }

    @Test
    void testRouteConversation_AgentOffline_SkipsAgent() {
        // Arrange
        RoutingRule rule = new RoutingRule();
        rule.setId(1L);
        rule.setName("Test Rule");
        rule.setRuleType(RoutingRule.RoutingRuleType.PRIORITY_BASED);
        rule.setPriority(1);
        rule.setTargetType(RoutingRule.TargetType.AGENT);
        rule.setTargetAgentId(10L);
        rule.setIsActive(true);
        rule.setTenantId(TEST_TENANT_ID);
        rule.setConditions(new HashMap<>());

        when(routingRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                .thenReturn(List.of(rule));

        AgentWorkload offlineAgent = new AgentWorkload();
        offlineAgent.setAgentId(10L);
        offlineAgent.setTenantId(TEST_TENANT_ID);
        offlineAgent.setStatus(AgentWorkload.AgentStatus.OFFLINE);
        offlineAgent.setActiveConversations(0);
        offlineAgent.setMaxConcurrentConversations(10);

        when(agentWorkloadRepository.findByTenantIdAndAgentId(TEST_TENANT_ID, 10L))
                .thenReturn(Optional.of(offlineAgent));

        Map<String, Object> context = new HashMap<>();

        // Act
        RoutingAssignmentResult result = routingService.routeConversation(TEST_CONVERSATION_ID, context);

        // Assert
        assertNotNull(result);
        assertFalse(result.isAssigned());
    }

    @Test
    void testRouteConversation_MultipleRules_PriorityOrder() {
        // Arrange
        RoutingRule rule1 = new RoutingRule();
        rule1.setId(1L);
        rule1.setName("High Priority Rule");
        rule1.setRuleType(RoutingRule.RoutingRuleType.PRIORITY_BASED);
        rule1.setPriority(1);
        rule1.setTargetType(RoutingRule.TargetType.AGENT);
        rule1.setTargetAgentId(10L);
        rule1.setIsActive(true);
        rule1.setTenantId(TEST_TENANT_ID);
        rule1.setConditions(new HashMap<>());

        RoutingRule rule2 = new RoutingRule();
        rule2.setId(2L);
        rule2.setName("Low Priority Rule");
        rule2.setRuleType(RoutingRule.RoutingRuleType.PRIORITY_BASED);
        rule2.setPriority(10);
        rule2.setTargetType(RoutingRule.TargetType.AGENT);
        rule2.setTargetAgentId(20L);
        rule2.setIsActive(true);
        rule2.setTenantId(TEST_TENANT_ID);
        rule2.setConditions(new HashMap<>());

        when(routingRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                .thenReturn(List.of(rule1, rule2));

        AgentWorkload agent1 = new AgentWorkload();
        agent1.setAgentId(10L);
        agent1.setTenantId(TEST_TENANT_ID);
        agent1.setStatus(AgentWorkload.AgentStatus.AVAILABLE);
        agent1.setActiveConversations(0);
        agent1.setMaxConcurrentConversations(10);

        when(agentWorkloadRepository.findByTenantIdAndAgentId(TEST_TENANT_ID, 10L))
                .thenReturn(Optional.of(agent1));

        Map<String, Object> context = new HashMap<>();

        // Act
        RoutingAssignmentResult result = routingService.routeConversation(TEST_CONVERSATION_ID, context);

        // Assert - Should match first rule (priority 1)
        assertNotNull(result);
        assertTrue(result.isAssigned());
        assertEquals(10L, result.getAssignedAgentId());
    }

    @Test
    void testRouteConversation_DefaultRule_UsedWhenNoMatch() {
        // Arrange
        RoutingRule defaultRule = new RoutingRule();
        defaultRule.setId(1L);
        defaultRule.setName("Default Rule");
        defaultRule.setRuleType(RoutingRule.RoutingRuleType.SKILL_BASED);
        defaultRule.setPriority(100);
        defaultRule.setTargetType(RoutingRule.TargetType.AGENT);
        defaultRule.setTargetAgentId(99L);
        defaultRule.setIsActive(true);
        defaultRule.setIsDefault(true);
        defaultRule.setTenantId(TEST_TENANT_ID);
        defaultRule.setConditions(new HashMap<>());

        when(routingRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(TEST_TENANT_ID))
                .thenReturn(List.of(defaultRule));

        AgentWorkload defaultAgent = new AgentWorkload();
        defaultAgent.setAgentId(99L);
        defaultAgent.setTenantId(TEST_TENANT_ID);
        defaultAgent.setStatus(AgentWorkload.AgentStatus.AVAILABLE);
        defaultAgent.setActiveConversations(0);
        defaultAgent.setMaxConcurrentConversations(10);

        when(agentWorkloadRepository.findByTenantIdAndAgentId(TEST_TENANT_ID, 99L))
                .thenReturn(Optional.of(defaultAgent));

        Map<String, Object> context = new HashMap<>();
        context.put("nonExistentCondition", "value");

        // Act
        RoutingAssignmentResult result = routingService.routeConversation(TEST_CONVERSATION_ID, context);

        // Assert
        assertNotNull(result);
        assertTrue(result.isAssigned());
        assertEquals(99L, result.getAssignedAgentId());
    }
}
