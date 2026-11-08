package com.chatbot.core.analytics.service;

import com.chatbot.core.ai.model.ConversationSentiment;
import com.chatbot.core.ai.model.SentimentLabel;
import com.chatbot.core.ai.repository.ConversationSentimentRepository;
import com.chatbot.core.customer.model.CustomerProfile;
import com.chatbot.core.customer.model.Feedback;
import com.chatbot.core.customer.repository.CustomerProfileRepository;
import com.chatbot.core.customer.repository.FeedbackRepository;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.ConversationStatus;
import com.chatbot.core.message.store.repository.ConversationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerJourneyServiceTest {

    @Mock private CustomerProfileRepository customerProfileRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private FeedbackRepository feedbackRepository;
    @Mock private ConversationSentimentRepository sentimentRepository;

    @InjectMocks
    private CustomerJourneyService service;

    private CustomerProfile profile(Long id, String externalId) {
        CustomerProfile p = new CustomerProfile();
        p.setId(id);
        p.setExternalId(externalId);
        p.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        return p;
    }

    private Conversation conversation(Long id, String externalUserId, ConversationStatus status) {
        Conversation c = new Conversation();
        c.setId(id);
        c.setExternalUserId(externalUserId);
        c.setStatus(status);
        c.setCreatedAt(LocalDateTime.of(2026, 1, 2, 10, 0));
        c.setUpdatedAt(LocalDateTime.of(2026, 1, 2, 11, 0));
        return c;
    }

    @Test
    void throwsWhenProfileNotFound() {
        when(customerProfileRepository.findByIdAndTenantId(99L, 1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getJourney(99L, 1L));
    }

    @Test
    void returnsEmptyJourneyWhenNoActivity() {
        when(customerProfileRepository.findByIdAndTenantId(1L, 1L))
            .thenReturn(Optional.of(profile(1L, "fb-1")));
        when(conversationRepository.findByExternalUserIdAndTenantIdOrderByCreatedAtAsc("fb-1", 1L))
            .thenReturn(List.of());
        when(feedbackRepository.findByCustomerProfileIdAndTenantId(1L, 1L)).thenReturn(List.of());

        List<CustomerJourneyService.JourneyEvent> events = service.getJourney(1L, 1L);

        assertEquals(1, events.size());
        assertEquals(CustomerJourneyService.JourneyEventType.PROFILE_CREATED, events.get(0).type());
    }

    @Test
    void includesConversationLifecycleEvents() {
        Conversation c = conversation(10L, "fb-1", ConversationStatus.CLOSED);
        c.setAgentAssignedId(5L);
        c.setFirstAgentResponseTime(LocalDateTime.of(2026, 1, 2, 10, 5));
        c.setCurrentEscalationTier(2);

        when(customerProfileRepository.findByIdAndTenantId(1L, 1L))
            .thenReturn(Optional.of(profile(1L, "fb-1")));
        when(conversationRepository.findByExternalUserIdAndTenantIdOrderByCreatedAtAsc("fb-1", 1L))
            .thenReturn(List.of(c));
        when(sentimentRepository.findByTenantIdAndConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(1L, 10L))
            .thenReturn(List.of());
        when(feedbackRepository.findByCustomerProfileIdAndTenantId(1L, 1L)).thenReturn(List.of());

        List<CustomerJourneyService.JourneyEvent> events = service.getJourney(1L, 1L);
        List<CustomerJourneyService.JourneyEventType> types = events.stream().map(CustomerJourneyService.JourneyEvent::type).toList();

        assertTrue(types.contains(CustomerJourneyService.JourneyEventType.CONVERSATION_STARTED));
        assertTrue(types.contains(CustomerJourneyService.JourneyEventType.AGENT_ASSIGNED));
        assertTrue(types.contains(CustomerJourneyService.JourneyEventType.ESCALATED));
        assertTrue(types.contains(CustomerJourneyService.JourneyEventType.CONVERSATION_CLOSED));
    }

    @Test
    void includesNegativeSentimentAndFeedback() {
        Conversation c = conversation(10L, "fb-1", ConversationStatus.OPEN);
        ConversationSentiment neg = new ConversationSentiment();
        neg.setLabel(SentimentLabel.NEGATIVE);
        neg.setScore(-0.8);
        neg.setCreatedAt(LocalDateTime.of(2026, 1, 2, 10, 30));

        Feedback fb = new Feedback();
        fb.setConversationId(10L);
        fb.setRating(2);
        fb.setSubmittedAt(LocalDateTime.of(2026, 1, 3, 9, 0));

        when(customerProfileRepository.findByIdAndTenantId(1L, 1L))
            .thenReturn(Optional.of(profile(1L, "fb-1")));
        when(conversationRepository.findByExternalUserIdAndTenantIdOrderByCreatedAtAsc("fb-1", 1L))
            .thenReturn(List.of(c));
        when(sentimentRepository.findByTenantIdAndConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(1L, 10L))
            .thenReturn(List.of(neg));
        when(feedbackRepository.findByCustomerProfileIdAndTenantId(1L, 1L)).thenReturn(List.of(fb));

        List<CustomerJourneyService.JourneyEvent> events = service.getJourney(1L, 1L);
        List<CustomerJourneyService.JourneyEventType> types = events.stream().map(CustomerJourneyService.JourneyEvent::type).toList();

        assertTrue(types.contains(CustomerJourneyService.JourneyEventType.NEGATIVE_SENTIMENT));
        assertTrue(types.contains(CustomerJourneyService.JourneyEventType.FEEDBACK_RECEIVED));
        // sorted by timestamp ascending
        for (int i = 1; i < events.size(); i++) {
            assertFalse(events.get(i).timestamp().isBefore(events.get(i - 1).timestamp()));
        }
    }

    @Test
    void ignoresPositiveSentiment() {
        Conversation c = conversation(10L, "fb-1", ConversationStatus.OPEN);
        ConversationSentiment pos = new ConversationSentiment();
        pos.setLabel(SentimentLabel.POSITIVE);
        pos.setScore(0.9);
        pos.setCreatedAt(LocalDateTime.of(2026, 1, 2, 10, 30));

        when(customerProfileRepository.findByIdAndTenantId(1L, 1L))
            .thenReturn(Optional.of(profile(1L, "fb-1")));
        when(conversationRepository.findByExternalUserIdAndTenantIdOrderByCreatedAtAsc("fb-1", 1L))
            .thenReturn(List.of(c));
        when(sentimentRepository.findByTenantIdAndConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(1L, 10L))
            .thenReturn(List.of(pos));
        when(feedbackRepository.findByCustomerProfileIdAndTenantId(1L, 1L)).thenReturn(List.of());

        List<CustomerJourneyService.JourneyEvent> events = service.getJourney(1L, 1L);
        assertTrue(events.stream().noneMatch(e -> e.type() == CustomerJourneyService.JourneyEventType.NEGATIVE_SENTIMENT));
    }

    @Test
    void agentAssignedRequiresBothIdAndResponseTime() {
        Conversation c = conversation(10L, "fb-1", ConversationStatus.OPEN);
        c.setAgentAssignedId(5L);
        // firstAgentResponseTime intentionally null -> no AGENT_ASSIGNED event

        when(customerProfileRepository.findByIdAndTenantId(1L, 1L))
            .thenReturn(Optional.of(profile(1L, "fb-1")));
        when(conversationRepository.findByExternalUserIdAndTenantIdOrderByCreatedAtAsc("fb-1", 1L))
            .thenReturn(List.of(c));
        when(sentimentRepository.findByTenantIdAndConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(1L, 10L))
            .thenReturn(List.of());
        when(feedbackRepository.findByCustomerProfileIdAndTenantId(1L, 1L)).thenReturn(List.of());

        List<CustomerJourneyService.JourneyEvent> events = service.getJourney(1L, 1L);
        assertTrue(events.stream().noneMatch(e -> e.type() == CustomerJourneyService.JourneyEventType.AGENT_ASSIGNED));
    }

    @Test
    void feedbackFallsBackToCreatedAtWhenSubmittedAtNull() {
        Feedback fb = new Feedback();
        fb.setConversationId(null);
        fb.setRating(5);
        fb.setSubmittedAt(null);
        fb.setCreatedAt(LocalDateTime.of(2026, 1, 4, 8, 0));

        when(customerProfileRepository.findByIdAndTenantId(1L, 1L))
            .thenReturn(Optional.of(profile(1L, "fb-1")));
        when(conversationRepository.findByExternalUserIdAndTenantIdOrderByCreatedAtAsc("fb-1", 1L))
            .thenReturn(List.of());
        when(feedbackRepository.findByCustomerProfileIdAndTenantId(1L, 1L)).thenReturn(List.of(fb));

        List<CustomerJourneyService.JourneyEvent> events = service.getJourney(1L, 1L);
        CustomerJourneyService.JourneyEvent feedbackEvent = events.stream()
            .filter(e -> e.type() == CustomerJourneyService.JourneyEventType.FEEDBACK_RECEIVED)
            .findFirst().orElseThrow();
        assertEquals(LocalDateTime.of(2026, 1, 4, 8, 0), feedbackEvent.timestamp());
    }
}
