package com.chatbot.core.analytics.service;

import com.chatbot.core.ai.model.AiSuggestionStatus;
import com.chatbot.core.ai.model.SentimentLabel;
import com.chatbot.core.ai.repository.AiSuggestionRepository;
import com.chatbot.core.ai.repository.ConversationSentimentRepository;
import com.chatbot.core.analytics.model.AnalyticsDailySummary;
import com.chatbot.core.analytics.repository.AnalyticsDailySummaryRepository;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.ConversationStatus;
import com.chatbot.core.message.store.model.Message;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.core.tenant.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsAggregationServiceTest {

    @Mock private AnalyticsDailySummaryRepository summaryRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private ConversationSentimentRepository sentimentRepository;
    @Mock private AiSuggestionRepository aiSuggestionRepository;
    @Mock private TenantRepository tenantRepository;

    @InjectMocks
    private AnalyticsAggregationService service;

    private static final LocalDate DATE = LocalDate.of(2026, 1, 10);

    private Conversation conv(Long id, ConversationStatus status) {
        Conversation c = new Conversation();
        c.setId(id);
        c.setStatus(status);
        c.setCreatedAt(LocalDateTime.of(2026, 1, 10, 9, 0));
        return c;
    }

    private Message msg(String sender) {
        Message m = new Message();
        m.setSender(sender);
        m.setCreatedAt(LocalDateTime.of(2026, 1, 10, 10, 0));
        return m;
    }

    private void stubEmptySources() {
        when(conversationRepository.findByTenantIdAndCreatedAtBetween(eq(1L), any(), any())).thenReturn(List.of());
        when(conversationRepository.findByTenantIdAndResolutionTimeBetween(eq(1L), any(), any())).thenReturn(List.of());
        when(conversationRepository.findByTenantIdAndLastEscalatedAtBetween(eq(1L), any(), any())).thenReturn(List.of());
        when(messageRepository.findByTenantIdAndCreatedAtBetween(eq(1L), any(), any())).thenReturn(List.of());
        when(sentimentRepository.avgScoreByTenantIdBetween(eq(1L), any(), any())).thenReturn(null);
        when(sentimentRepository.countByTenantIdAndLabelBetween(eq(1L), eq(SentimentLabel.NEGATIVE), any(), any())).thenReturn(0L);
        when(aiSuggestionRepository.countByTenantIdBetween(eq(1L), any(), any())).thenReturn(0L);
        when(aiSuggestionRepository.countByTenantIdAndStatusBetween(eq(1L), eq(AiSuggestionStatus.ACCEPTED), any(), any())).thenReturn(0L);
        when(aiSuggestionRepository.countByTenantIdAndStatusBetween(eq(1L), eq(AiSuggestionStatus.REJECTED), any(), any())).thenReturn(0L);
        when(summaryRepository.findByTenantIdAndSummaryDateAndIsDeletedFalse(1L, DATE)).thenReturn(Optional.empty());
        when(summaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void aggregateForDateComputesAllMetrics() {
        Conversation created1 = conv(1L, ConversationStatus.OPEN);
        created1.setFirstAgentResponseTime(LocalDateTime.of(2026, 1, 10, 9, 10)); // 10 min
        Conversation created2 = conv(2L, ConversationStatus.CLOSED);
        created2.setResolutionTime(LocalDateTime.of(2026, 1, 10, 11, 0)); // 120 min
        Conversation escalated = conv(3L, ConversationStatus.ACTIVE_AGENT);

        when(conversationRepository.findByTenantIdAndCreatedAtBetween(eq(1L), any(), any()))
            .thenReturn(List.of(created1, created2));
        when(conversationRepository.findByTenantIdAndResolutionTimeBetween(eq(1L), any(), any()))
            .thenReturn(List.of(created2));
        when(conversationRepository.findByTenantIdAndLastEscalatedAtBetween(eq(1L), any(), any()))
            .thenReturn(List.of(escalated));
        when(messageRepository.findByTenantIdAndCreatedAtBetween(eq(1L), any(), any()))
            .thenReturn(List.of(msg("user"), msg("USER"), msg("bot"), msg("agent"), msg("system-x")));
        when(sentimentRepository.avgScoreByTenantIdBetween(eq(1L), any(), any())).thenReturn(0.35);
        when(sentimentRepository.countByTenantIdAndLabelBetween(eq(1L), eq(SentimentLabel.NEGATIVE), any(), any())).thenReturn(4L);
        when(aiSuggestionRepository.countByTenantIdBetween(eq(1L), any(), any())).thenReturn(10L);
        when(aiSuggestionRepository.countByTenantIdAndStatusBetween(eq(1L), eq(AiSuggestionStatus.ACCEPTED), any(), any())).thenReturn(6L);
        when(aiSuggestionRepository.countByTenantIdAndStatusBetween(eq(1L), eq(AiSuggestionStatus.REJECTED), any(), any())).thenReturn(2L);
        when(summaryRepository.findByTenantIdAndSummaryDateAndIsDeletedFalse(1L, DATE)).thenReturn(Optional.empty());
        when(summaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AnalyticsDailySummary result = service.aggregateForDate(1L, DATE);

        assertEquals(2, result.getTotalConversations());
        assertEquals(2, result.getNewConversations());
        assertEquals(1, result.getClosedConversations()); // only CLOSED status counts
        assertEquals(5, result.getTotalMessages());
        assertEquals(2, result.getUserMessages());   // "user" + "USER" case-insensitive
        assertEquals(2, result.getBotMessages());    // "bot" + unknown sender "system-x" -> else bucket
        assertEquals(1, result.getAgentMessages());  // "agent"
        assertEquals(1, result.getEscalationsCount());
        assertEquals(0.35, result.getAvgSentimentScore());
        assertEquals(4, result.getNegativeSentimentCount());
        assertEquals(10, result.getAiSuggestionsCount());
        assertEquals(6, result.getAiSuggestionsAccepted());
        assertEquals(2, result.getAiSuggestionsRejected());
        assertEquals(1L, result.getTenantId());

        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> metrics = (java.util.Map<String, Object>) result.getMetrics();
        assertEquals(0.5, (Double) metrics.get("resolutionRate"), 0.001);   // 1 resolved / 2 created
        assertEquals(0.5, (Double) metrics.get("escalationRate"), 0.001);   // 1 escalated / 2 created
        assertEquals(0.6, (Double) metrics.get("aiAcceptanceRate"), 0.001); // 6/10
    }

    @Test
    void aggregateForDateUpsertsExistingSummary() {
        stubEmptySources();
        AnalyticsDailySummary existing = new AnalyticsDailySummary();
        existing.setId(99L);
        existing.setTenantId(1L);
        existing.setSummaryDate(DATE);
        when(summaryRepository.findByTenantIdAndSummaryDateAndIsDeletedFalse(1L, DATE))
            .thenReturn(Optional.of(existing));

        AnalyticsDailySummary result = service.aggregateForDate(1L, DATE);

        assertEquals(99L, result.getId()); // same entity updated, not new
        verify(summaryRepository).save(existing);
    }

    @Test
    void aggregateForDateHandlesEmptyDay() {
        stubEmptySources();

        AnalyticsDailySummary result = service.aggregateForDate(1L, DATE);

        assertEquals(0, result.getTotalConversations());
        assertEquals(0, result.getTotalMessages());
        assertNull(result.getAvgFirstResponseMinutes());
        assertNull(result.getAvgResolutionMinutes());
        assertNull(result.getAvgSentimentScore());

        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> metrics = (java.util.Map<String, Object>) result.getMetrics();
        assertEquals(0.0, metrics.get("resolutionRate"));
        assertEquals(0.0, metrics.get("escalationRate"));
        assertEquals(0.0, metrics.get("aiAcceptanceRate"));
    }

    @Test
    void getSummaryDelegatesToRepository() {
        when(summaryRepository.findByTenantIdAndDateRange(1L, DATE, DATE.plusDays(7)))
            .thenReturn(List.of());
        assertTrue(service.getSummary(1L, DATE, DATE.plusDays(7)).isEmpty());
    }

    @Test
    void getSummaryForDateDelegatesToRepository() {
        when(summaryRepository.findByTenantIdAndSummaryDateAndIsDeletedFalse(1L, DATE))
            .thenReturn(Optional.empty());
        assertTrue(service.getSummaryForDate(1L, DATE).isEmpty());
    }
}
