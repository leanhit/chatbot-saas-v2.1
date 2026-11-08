package com.chatbot.core.analytics.service;

import com.chatbot.core.analytics.model.FunnelStage;
import com.chatbot.core.analytics.repository.FunnelStageRepository;
import com.chatbot.core.customer.model.Feedback;
import com.chatbot.core.customer.repository.FeedbackRepository;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.ConversationStatus;
import com.chatbot.core.message.store.repository.ConversationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FunnelServiceTest {

    @Mock private FunnelStageRepository funnelStageRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FunnelService service;

    private FunnelStage stage(Long id, String name, int order, FunnelStage.MatchType type, String value) {
        FunnelStage s = new FunnelStage();
        s.setId(id);
        s.setName(name);
        s.setStageOrder(order);
        s.setMatchType(type);
        s.setMatchValue(value);
        s.setActive(true);
        s.setTenantId(1L);
        return s;
    }

    private Conversation conv(Long id, ConversationStatus status) {
        Conversation c = new Conversation();
        c.setId(id);
        c.setStatus(status);
        return c;
    }

    @Test
    void createStageAssignsTenantAndDefaultsActive() {
        FunnelStage s = stage(null, "Opened", 1, FunnelStage.MatchType.STATUS, "open");
        s.setActive(null);
        s.setTenantId(null);
        when(funnelStageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FunnelStage saved = service.createStage(s, 7L);

        assertEquals(7L, saved.getTenantId());
        assertEquals(Boolean.TRUE, saved.getActive());
    }

    @Test
    void updateStageRejectsOtherTenant() {
        FunnelStage existing = stage(1L, "Old", 1, FunnelStage.MatchType.STATUS, "open");
        existing.setTenantId(2L);
        when(funnelStageRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
            () -> service.updateStage(1L, new FunnelStage(), 1L));
    }

    @Test
    void deleteStageRejectsOtherTenant() {
        FunnelStage existing = stage(1L, "Old", 1, FunnelStage.MatchType.STATUS, "open");
        existing.setTenantId(2L);
        when(funnelStageRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> service.deleteStage(1L, 1L));
        verify(funnelStageRepository, never()).delete(any());
    }

    @Test
    void computeFunnelReturnsEmptyWhenNoStages() {
        when(funnelStageRepository.findByTenantIdAndActiveTrueOrderByStageOrderAsc(1L)).thenReturn(List.of());
        assertTrue(service.computeFunnel(1L).isEmpty());
    }

    @Test
    void computeFunnelCountsByStatusAndComputesConversion() {
        FunnelStage open = stage(1L, "Open", 1, FunnelStage.MatchType.STATUS, "open");
        FunnelStage closed = stage(2L, "Closed", 2, FunnelStage.MatchType.STATUS, "closed");
        when(funnelStageRepository.findByTenantIdAndActiveTrueOrderByStageOrderAsc(1L))
            .thenReturn(List.of(open, closed));
        when(conversationRepository.findByTenantId(1L)).thenReturn(List.of(
            conv(1L, ConversationStatus.OPEN),
            conv(2L, ConversationStatus.OPEN),
            conv(3L, ConversationStatus.CLOSED)
        ));
        when(feedbackRepository.findByTenantId(1L)).thenReturn(List.of());

        List<FunnelService.FunnelStageResult> results = service.computeFunnel(1L);

        assertEquals(2, results.size());
        assertEquals(2, results.get(0).count());
        assertNull(results.get(0).conversionFromPrevious());
        assertEquals(1, results.get(1).count());
        assertEquals(0.5, results.get(1).conversionFromPrevious());
    }

    @Test
    void computeFunnelMatchesEscalatedAndFeedback() {
        FunnelStage esc = stage(1L, "Escalated", 1, FunnelStage.MatchType.ESCALATED, null);
        FunnelStage fb = stage(2L, "Feedback", 2, FunnelStage.MatchType.FEEDBACK, null);
        when(funnelStageRepository.findByTenantIdAndActiveTrueOrderByStageOrderAsc(1L))
            .thenReturn(List.of(esc, fb));

        Conversation c1 = conv(1L, ConversationStatus.OPEN);
        c1.setCurrentEscalationTier(1);
        Conversation c2 = conv(2L, ConversationStatus.OPEN);
        when(conversationRepository.findByTenantId(1L)).thenReturn(List.of(c1, c2));

        Feedback f = new Feedback();
        f.setConversationId(2L);
        when(feedbackRepository.findByTenantId(1L)).thenReturn(List.of(f));

        List<FunnelService.FunnelStageResult> results = service.computeFunnel(1L);

        assertEquals(1, results.get(0).count());   // only c1 escalated
        assertEquals(1, results.get(1).count());   // only c2 has feedback
    }

    @Test
    void listStagesDelegatesToRepository() {
        FunnelStage s = stage(1L, "Open", 1, FunnelStage.MatchType.STATUS, "open");
        when(funnelStageRepository.findByTenantIdOrderByStageOrderAsc(1L)).thenReturn(List.of(s));

        List<FunnelStage> result = service.listStages(1L);

        assertEquals(1, result.size());
        assertEquals("Open", result.get(0).getName());
    }

    @Test
    void updateStageAppliesNonNullFieldsAndSaves() {
        FunnelStage existing = stage(1L, "Old", 1, FunnelStage.MatchType.STATUS, "open");
        when(funnelStageRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(funnelStageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FunnelStage update = new FunnelStage();
        update.setName("New name");
        update.setActive(false);
        // stageOrder, matchType, matchValue null -> preserved

        FunnelStage saved = service.updateStage(1L, update, 1L);

        assertEquals("New name", saved.getName());
        assertEquals(Boolean.FALSE, saved.getActive());
        assertEquals(1, saved.getStageOrder());
        assertEquals(FunnelStage.MatchType.STATUS, saved.getMatchType());
        assertEquals("open", saved.getMatchValue());
    }

    @Test
    void updateStageThrowsWhenNotFound() {
        when(funnelStageRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
            () -> service.updateStage(99L, new FunnelStage(), 1L));
    }

    @Test
    void deleteStageDeletesWhenTenantMatches() {
        FunnelStage existing = stage(1L, "Open", 1, FunnelStage.MatchType.STATUS, "open");
        when(funnelStageRepository.findById(1L)).thenReturn(Optional.of(existing));

        service.deleteStage(1L, 1L);

        verify(funnelStageRepository).delete(existing);
    }

    @Test
    void computeFunnelMatchesAgentAssigned() {
        FunnelStage assigned = stage(1L, "Assigned", 1, FunnelStage.MatchType.AGENT_ASSIGNED, null);
        when(funnelStageRepository.findByTenantIdAndActiveTrueOrderByStageOrderAsc(1L))
            .thenReturn(List.of(assigned));

        Conversation withAgent = conv(1L, ConversationStatus.ACTIVE_AGENT);
        withAgent.setAgentAssignedId(42L);
        Conversation noAgent = conv(2L, ConversationStatus.OPEN);
        when(conversationRepository.findByTenantId(1L)).thenReturn(List.of(withAgent, noAgent));
        when(feedbackRepository.findByTenantId(1L)).thenReturn(List.of());

        List<FunnelService.FunnelStageResult> results = service.computeFunnel(1L);

        assertEquals(1, results.size());
        assertEquals(1, results.get(0).count());
    }

    @Test
    void computeFunnelConversionIsNullWhenPreviousStageEmpty() {
        FunnelStage none = stage(1L, "None", 1, FunnelStage.MatchType.STATUS, "nonexistent_status");
        FunnelStage some = stage(2L, "Some", 2, FunnelStage.MatchType.STATUS, "open");
        when(funnelStageRepository.findByTenantIdAndActiveTrueOrderByStageOrderAsc(1L))
            .thenReturn(List.of(none, some));
        when(conversationRepository.findByTenantId(1L))
            .thenReturn(List.of(conv(1L, ConversationStatus.OPEN)));
        when(feedbackRepository.findByTenantId(1L)).thenReturn(List.of());

        List<FunnelService.FunnelStageResult> results = service.computeFunnel(1L);

        assertEquals(0, results.get(0).count());
        assertEquals(1, results.get(1).count());
        // previousCount == 0 -> conversion must be null, not divide-by-zero
        assertNull(results.get(1).conversionFromPrevious());
    }
}
