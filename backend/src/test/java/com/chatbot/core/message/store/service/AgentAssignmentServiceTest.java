package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Agent;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.AgentRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit test for AgentAssignmentService
 * Tests online status filtering and skills-based assignment
 */
@ExtendWith(MockitoExtension.class)
class AgentAssignmentServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private AgentService agentService;

    @Mock
    private ObjectMapper objectMapper;

    private AgentAssignmentService agentAssignmentService;

    private Conversation testConversation;
    private final Long testTenantId = 1L;

    @BeforeEach
    void setUp() {
        agentAssignmentService = new AgentAssignmentService(
                agentRepository,
                conversationRepository,
                agentService,
                objectMapper   // ← injected bean thay vì new ObjectMapper()
        );

        testConversation = Conversation.builder()
                .tenantId(testTenantId)
                .externalUserId("user-123")
                .status("open")
                .build();
        testConversation.setId(1L);
    }

    @Test
    void testAssignConversationToAgent_OnlyOnlineAgents() {
        // Arrange
        Agent onlineAgent = createAgent(1L, Agent.AgentStatus.ONLINE, 5);
        Agent offlineAgent = createAgent(2L, Agent.AgentStatus.OFFLINE, 3);

        when(agentRepository.findAvailableAgentsByTenantId(testTenantId))
                .thenReturn(Arrays.asList(onlineAgent, offlineAgent));

        // Act
        Optional<Agent> result = agentAssignmentService.assignConversationToAgent(testConversation, null);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals(Agent.AgentStatus.ONLINE, result.get().getStatus());
    }

    @Test
    void testAssignConversationToAgent_NoOnlineAgents() {
        // Arrange
        Agent offlineAgent1 = createAgent(1L, Agent.AgentStatus.OFFLINE, 5);
        Agent offlineAgent2 = createAgent(2L, Agent.AgentStatus.AWAY, 3);

        when(agentRepository.findAvailableAgentsByTenantId(testTenantId))
                .thenReturn(Arrays.asList(offlineAgent1, offlineAgent2));

        // Act
        Optional<Agent> result = agentAssignmentService.assignConversationToAgent(testConversation, null);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testAssignConversationToAgent_SkillsBasedWithOnlineAgents() {
        // Arrange
        Agent onlineAgentWithSkills = createAgentWithSkills(1L, Agent.AgentStatus.ONLINE, 5, Set.of("billing", "technical"));
        Agent onlineAgentWithoutSkills = createAgentWithSkills(2L, Agent.AgentStatus.ONLINE, 3, Set.of("sales"));

        when(agentRepository.findAvailableAgentsByTenantId(testTenantId))
                .thenReturn(Arrays.asList(onlineAgentWithSkills, onlineAgentWithoutSkills));

        List<String> requiredSkills = Arrays.asList("billing");

        // Act
        Optional<Agent> result = agentAssignmentService.assignConversationToAgent(testConversation, requiredSkills);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertTrue(result.get().getSkills().contains("billing"));
    }

    @Test
    void testAssignConversationToAgent_SkillsBasedNoOnlineAgentsWithSkills() {
        // Arrange
        Agent onlineAgentWithoutSkills = createAgentWithSkills(2L, Agent.AgentStatus.ONLINE, 3, Set.of("sales"));

        when(agentRepository.findAvailableAgentsByTenantId(testTenantId))
                .thenReturn(Arrays.asList(onlineAgentWithoutSkills));

        List<String> requiredSkills = Arrays.asList("billing");

        // Act
        Optional<Agent> result = agentAssignmentService.assignConversationToAgent(testConversation, requiredSkills);

        // Assert
        assertTrue(result.isPresent()); // Should fallback to load balancing with online agents
        assertEquals(2L, result.get().getId());
    }

    @Test
    void testAssignConversationToAgent_LoadBalancing() {
        // Arrange
        Agent onlineAgent1 = createAgent(1L, Agent.AgentStatus.ONLINE, 8);
        Agent onlineAgent2 = createAgent(2L, Agent.AgentStatus.ONLINE, 2);

        when(agentRepository.findAvailableAgentsByTenantId(testTenantId))
                .thenReturn(Arrays.asList(onlineAgent1, onlineAgent2));

        // Act
        Optional<Agent> result = agentAssignmentService.assignConversationToAgent(testConversation, null);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(2L, result.get().getId()); // Should pick agent with lower load
    }

    @Test
    void testGetRecommendedAgents_OnlyOnlineAgents() {
        // Arrange
        Agent onlineAgent = createAgent(1L, Agent.AgentStatus.ONLINE, 5);
        Agent offlineAgent = createAgent(2L, Agent.AgentStatus.OFFLINE, 3);

        when(agentRepository.findAvailableAgentsByTenantId(testTenantId))
                .thenReturn(Arrays.asList(onlineAgent, offlineAgent));

        // Act
        List<Agent> result = agentAssignmentService.getRecommendedAgents(testConversation, null, 10);

        // Assert
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(Agent.AgentStatus.ONLINE, result.get(0).getStatus());
    }

    @Test
    void testGetRecommendedAgents_NoOnlineAgents() {
        // Arrange
        Agent offlineAgent = createAgent(1L, Agent.AgentStatus.OFFLINE, 5);

        when(agentRepository.findAvailableAgentsByTenantId(testTenantId))
                .thenReturn(Arrays.asList(offlineAgent));

        // Act
        List<Agent> result = agentAssignmentService.getRecommendedAgents(testConversation, null, 10);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testReassignConversation_Success() {
        // Arrange
        Long conversationId = 1L;
        Long oldAgentId = 1L;
        Long newAgentId = 2L;

        Conversation conversation = Conversation.builder()
                .id(conversationId)
                .agentAssignedId(oldAgentId)
                .tenantId(testTenantId)
                .build();

        Agent newAgent = createAgent(newAgentId, Agent.AgentStatus.ONLINE, 3);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(agentRepository.findById(newAgentId)).thenReturn(Optional.of(newAgent));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        // Act
        boolean result = agentAssignmentService.reassignConversation(conversationId, newAgentId);

        // Assert
        assertTrue(result);
        assertEquals(newAgentId, conversation.getAgentAssignedId());
        verify(agentService, times(1)).decrementAgentLoad(oldAgentId);
        verify(agentService, times(1)).incrementAgentLoad(newAgentId);
    }

    @Test
    void testReassignConversation_AgentCannotAcceptMore() {
        // Arrange
        Long conversationId = 1L;
        Long newAgentId = 2L;

        Conversation conversation = Conversation.builder()
                .id(conversationId)
                .tenantId(testTenantId)
                .build();

        Agent newAgent = createAgent(newAgentId, Agent.AgentStatus.ONLINE, 10);
        newAgent.setMaxConcurrentConversations(10); // At capacity

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(agentRepository.findById(newAgentId)).thenReturn(Optional.of(newAgent));

        // Act
        boolean result = agentAssignmentService.reassignConversation(conversationId, newAgentId);

        // Assert
        assertFalse(result);
        verify(agentService, never()).incrementAgentLoad(newAgentId);
    }

    @Test
    void testAgentHasSkill_True() {
        // Arrange
        Long agentId = 1L;
        Agent agent = createAgentWithSkills(agentId, Agent.AgentStatus.ONLINE, 5, Set.of("billing", "technical"));

        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));

        // Act
        boolean result = agentAssignmentService.agentHasSkill(agentId, "billing");

        // Assert
        assertTrue(result);
    }

    @Test
    void testAgentHasSkill_False() {
        // Arrange
        Long agentId = 1L;
        Agent agent = createAgentWithSkills(agentId, Agent.AgentStatus.ONLINE, 5, Set.of("sales"));

        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));

        // Act
        boolean result = agentAssignmentService.agentHasSkill(agentId, "billing");

        // Assert
        assertFalse(result);
    }

    // Helper methods
    private Agent createAgent(Long id, Agent.AgentStatus status, int currentLoad) {
        return Agent.builder()
                .id(id)
                .status(status)
                .currentLoad(currentLoad)
                .maxConcurrentConversations(10)
                .active(true)
                .build();
    }

    private Agent createAgentWithSkills(Long id, Agent.AgentStatus status, int currentLoad, Set<String> skills) {
        Agent agent = createAgent(id, status, currentLoad);
        agent.setSkills(skills);
        return agent;
    }
}
