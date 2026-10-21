package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Agent;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.AgentRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit test for AgentReassignmentService
 * Tests auto-reassignment logic when agents go offline
 */
@ExtendWith(MockitoExtension.class)
class AgentReassignmentServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private AgentAssignmentService agentAssignmentService;

    @Mock
    private AgentService agentService;

    @Mock
    private TenantRepository tenantRepository;

    private AgentReassignmentService agentReassignmentService;

    private final Long testTenantId = 1L;

    @BeforeEach
    void setUp() {
        agentReassignmentService = new AgentReassignmentService(
                conversationRepository,
                agentRepository,
                agentAssignmentService,
                agentService,
                tenantRepository   // ← new dependency after fix
        );
    }

    @Test
    void testCheckAndReassignOfflineAgents_NoActiveTenants() {
        // Arrange — scheduler now queries all active tenants (no TenantContext needed)
        when(tenantRepository.findAllIdsByStatus(TenantStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        // Act
        agentReassignmentService.checkAndReassignOfflineAgents();

        // Assert - Should return early without touching conversations
        verify(conversationRepository, never()).findByTenantIdAndAgentAssignedIdIsNotNull(anyLong());
    }

    @Test
    void testCheckAndReassignOfflineAgents_NoAssignedConversations() {
        // Arrange
        when(tenantRepository.findAllIdsByStatus(TenantStatus.ACTIVE))
                .thenReturn(List.of(testTenantId));
        when(conversationRepository.findByTenantIdAndAgentAssignedIdIsNotNull(testTenantId))
                .thenReturn(Collections.emptyList());

        // Act
        agentReassignmentService.checkAndReassignOfflineAgents();

        // Assert
        verify(conversationRepository, times(1))
                .findByTenantIdAndAgentAssignedIdIsNotNull(testTenantId);
        verify(agentRepository, never()).findById(anyLong());
    }

    @Test
    void testCheckAndReassignOfflineAgents_AgentOnline_NoReassignment() {
        // Arrange
        Long conversationId = 1L;
        Long agentId = 100L;

        Conversation conversation = Conversation.builder()
                .id(conversationId)
                .tenantId(testTenantId)
                .agentAssignedId(agentId)
                .build();

        Agent onlineAgent = createAgent(agentId, Agent.AgentStatus.ONLINE);

        when(tenantRepository.findAllIdsByStatus(TenantStatus.ACTIVE))
                .thenReturn(List.of(testTenantId));
        when(conversationRepository.findByTenantIdAndAgentAssignedIdIsNotNull(testTenantId))
                .thenReturn(Arrays.asList(conversation));
        when(agentRepository.findById(agentId)).thenReturn(Optional.of(onlineAgent));

        // Act
        agentReassignmentService.checkAndReassignOfflineAgents();

        // Assert
        verify(agentAssignmentService, never()).autoAssignConversation(any(Conversation.class));
        verify(agentService, never()).decrementAgentLoad(anyLong());
    }

    @Test
    void testCheckAndReassignOfflineAgents_AgentOffline_ReassignToOnlineAgent() {
        // Arrange
        Long conversationId = 1L;
        Long oldAgentId = 100L;
        Long newAgentId = 200L;

        Conversation conversation = Conversation.builder()
                .id(conversationId)
                .tenantId(testTenantId)
                .agentAssignedId(oldAgentId)
                .build();

        Agent offlineAgent = createAgent(oldAgentId, Agent.AgentStatus.OFFLINE);
        Agent onlineAgent = createAgent(newAgentId, Agent.AgentStatus.ONLINE);

        when(tenantRepository.findAllIdsByStatus(TenantStatus.ACTIVE))
                .thenReturn(List.of(testTenantId));
        when(conversationRepository.findByTenantIdAndAgentAssignedIdIsNotNull(testTenantId))
                .thenReturn(Arrays.asList(conversation));
        when(agentRepository.findById(oldAgentId)).thenReturn(Optional.of(offlineAgent));
        when(agentAssignmentService.autoAssignConversation(conversation))
                .thenReturn(Optional.of(onlineAgent));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        // Act
        agentReassignmentService.checkAndReassignOfflineAgents();

        // Assert
        verify(agentService, times(1)).decrementAgentLoad(oldAgentId);
        verify(agentService, times(1)).incrementAgentLoad(newAgentId);
        verify(conversationRepository, times(1)).save(any(Conversation.class));
        assertEquals(newAgentId, conversation.getAgentAssignedId());
    }

    @Test
    void testCheckAndReassignOfflineAgents_AgentOffline_NoOnlineAgents_ReleaseToBot() {
        Long conversationId = 1L;
        Long oldAgentId = 100L;

        Conversation conversation = Conversation.builder()
                .id(conversationId).tenantId(testTenantId)
                .agentAssignedId(oldAgentId).isTakenOverByAgent(true).status("active_agent")
                .build();

        Agent offlineAgent = createAgent(oldAgentId, Agent.AgentStatus.OFFLINE);

        when(tenantRepository.findAllIdsByStatus(TenantStatus.ACTIVE)).thenReturn(List.of(testTenantId));
        when(conversationRepository.findByTenantIdAndAgentAssignedIdIsNotNull(testTenantId))
                .thenReturn(Arrays.asList(conversation));
        when(agentRepository.findById(oldAgentId)).thenReturn(Optional.of(offlineAgent));
        when(agentAssignmentService.autoAssignConversation(conversation)).thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        agentReassignmentService.checkAndReassignOfflineAgents();

        verify(agentService, times(1)).decrementAgentLoad(oldAgentId);
        verify(agentService, never()).incrementAgentLoad(anyLong());
        assertNull(conversation.getAgentAssignedId());
        assertFalse(conversation.getIsTakenOverByAgent());
        assertEquals("open", conversation.getStatus());
    }

    @Test
    void testCheckAndReassignOfflineAgents_AgentNotFound_Skips() {
        Long conversationId = 1L;
        Long agentId = 100L;

        Conversation conversation = Conversation.builder()
                .id(conversationId).tenantId(testTenantId).agentAssignedId(agentId).build();

        when(tenantRepository.findAllIdsByStatus(TenantStatus.ACTIVE)).thenReturn(List.of(testTenantId));
        when(conversationRepository.findByTenantIdAndAgentAssignedIdIsNotNull(testTenantId))
                .thenReturn(Arrays.asList(conversation));
        when(agentRepository.findById(agentId)).thenReturn(Optional.empty());

        agentReassignmentService.checkAndReassignOfflineAgents();

        verify(agentAssignmentService, never()).autoAssignConversation(any(Conversation.class));
        verify(agentService, never()).decrementAgentLoad(anyLong());
    }

    @Test
    void testCheckAndReassignOfflineAgents_MultiTenant_EachTenantChecked() {
        // Scheduler phải loop qua TẤT CẢ active tenants, không phụ thuộc TenantContext
        Long tenant2 = 2L;
        when(tenantRepository.findAllIdsByStatus(TenantStatus.ACTIVE))
                .thenReturn(List.of(testTenantId, tenant2));
        when(conversationRepository.findByTenantIdAndAgentAssignedIdIsNotNull(testTenantId))
                .thenReturn(Collections.emptyList());
        when(conversationRepository.findByTenantIdAndAgentAssignedIdIsNotNull(tenant2))
                .thenReturn(Collections.emptyList());

        agentReassignmentService.checkAndReassignOfflineAgents();

        // Both tenants should be checked
        verify(conversationRepository).findByTenantIdAndAgentAssignedIdIsNotNull(testTenantId);
        verify(conversationRepository).findByTenantIdAndAgentAssignedIdIsNotNull(tenant2);
    }

    @Test
    void testReassignConversation_ManualTrigger_AgentOnline_NoReassignment() {
        // Arrange
        Long conversationId = 1L;
        Long agentId = 100L;

        Conversation conversation = Conversation.builder()
                .id(conversationId)
                .tenantId(testTenantId)
                .agentAssignedId(agentId)
                .build();

        Agent onlineAgent = createAgent(agentId, Agent.AgentStatus.ONLINE);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(agentRepository.findById(agentId)).thenReturn(Optional.of(onlineAgent));

        // Act
        int result = agentReassignmentService.reassignConversation(conversationId);

        // Assert
        assertEquals(0, result);
        verify(agentAssignmentService, never()).autoAssignConversation(any(Conversation.class));
    }

    @Test
    void testReassignConversation_ManualTrigger_AgentOffline_Reassigns() {
        // Arrange
        Long conversationId = 1L;
        Long oldAgentId = 100L;
        Long newAgentId = 200L;

        Conversation conversation = Conversation.builder()
                .id(conversationId)
                .tenantId(testTenantId)
                .agentAssignedId(oldAgentId)
                .build();

        Agent offlineAgent = createAgent(oldAgentId, Agent.AgentStatus.OFFLINE);
        Agent onlineAgent = createAgent(newAgentId, Agent.AgentStatus.ONLINE);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(agentRepository.findById(oldAgentId)).thenReturn(Optional.of(offlineAgent));
        when(agentAssignmentService.autoAssignConversation(conversation))
                .thenReturn(Optional.of(onlineAgent));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        // Act
        int result = agentReassignmentService.reassignConversation(conversationId);

        // Assert
        assertEquals(1, result);
        verify(agentService, times(1)).decrementAgentLoad(oldAgentId);
        verify(agentService, times(1)).incrementAgentLoad(newAgentId);
        assertEquals(newAgentId, conversation.getAgentAssignedId());
    }

    @Test
    void testReassignConversation_ConversationNotFound() {
        // Arrange
        Long conversationId = 999L;

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            agentReassignmentService.reassignConversation(conversationId);
        });
    }

    @Test
    void testReassignConversation_NoAssignedAgent() {
        // Arrange
        Long conversationId = 1L;

        Conversation conversation = Conversation.builder()
                .id(conversationId)
                .tenantId(testTenantId)
                .agentAssignedId(null)
                .build();

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        // Act
        int result = agentReassignmentService.reassignConversation(conversationId);

        // Assert
        assertEquals(0, result);
        verify(agentRepository, never()).findById(anyLong());
    }

    // Helper method
    private Agent createAgent(Long id, Agent.AgentStatus status) {
        return Agent.builder()
                .id(id)
                .status(status)
                .currentLoad(5)
                .maxConcurrentConversations(10)
                .active(true)
                .build();
    }
}
