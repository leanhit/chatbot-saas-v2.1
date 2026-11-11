package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Agent;
import com.chatbot.core.message.store.repository.AgentRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.training.service.OnboardingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for onboarding → online gate (no Spring context).
 */
@ExtendWith(MockitoExtension.class)
class AgentOnboardingGateTest {

    @Mock
    private AgentRepository agentRepository;
    @Mock
    private OnboardingService onboardingService;

    @InjectMocks
    private AgentService agentService;

    private Agent agent(Long id) {
        Agent a = new Agent();
        a.setId(id);
        a.setStatus(Agent.AgentStatus.OFFLINE);
        return a;
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void blocksGoingOnlineWhenOnboardingIncomplete() {
        TenantContext.setTenantId(1L);
        when(onboardingService.isOnboardingComplete(1L, 1L)).thenReturn(false);

        assertThrows(IllegalStateException.class,
            () -> agentService.updateAgentStatus(1L, Agent.AgentStatus.ONLINE));
        verify(agentRepository, never()).save(any());
    }

    @Test
    void allowsGoingOnlineWhenOnboardingComplete() {
        TenantContext.setTenantId(1L);
        Agent a = agent(1L);
        when(agentRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(a));
        when(onboardingService.isOnboardingComplete(1L, 1L)).thenReturn(true);

        agentService.updateAgentStatus(1L, Agent.AgentStatus.ONLINE);

        assertEquals(Agent.AgentStatus.ONLINE, a.getStatus());
        verify(agentRepository).save(a);
    }

    @Test
    void offlineStatusNeverChecksOnboarding() {
        Agent a = agent(1L);
        when(agentRepository.findById(1L)).thenReturn(Optional.of(a));

        agentService.updateAgentStatus(1L, Agent.AgentStatus.OFFLINE);

        verify(onboardingService, never()).isOnboardingComplete(any(), any());
        verify(agentRepository).save(any());
    }
}
