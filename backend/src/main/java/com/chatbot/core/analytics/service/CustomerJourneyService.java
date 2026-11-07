package com.chatbot.core.analytics.service;

import com.chatbot.core.ai.model.ConversationSentiment;
import com.chatbot.core.ai.model.SentimentLabel;
import com.chatbot.core.ai.repository.ConversationSentimentRepository;
import com.chatbot.core.customer.model.CustomerProfile;
import com.chatbot.core.customer.model.Feedback;
import com.chatbot.core.customer.repository.CustomerProfileRepository;
import com.chatbot.core.customer.repository.FeedbackRepository;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Computes a customer journey timeline on demand from existing tenant-scoped data:
 * profile creation, conversations, assignments, escalations, closures,
 * feedback and negative sentiment events. No separate event table — the
 * source-of-truth entities are queried directly so the journey never drifts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerJourneyService {

    private final CustomerProfileRepository customerProfileRepository;
    private final ConversationRepository conversationRepository;
    private final FeedbackRepository feedbackRepository;
    private final ConversationSentimentRepository sentimentRepository;

    public enum JourneyEventType {
        PROFILE_CREATED,
        CONVERSATION_STARTED,
        AGENT_ASSIGNED,
        ESCALATED,
        CONVERSATION_CLOSED,
        FEEDBACK_RECEIVED,
        NEGATIVE_SENTIMENT
    }

    public record JourneyEvent(JourneyEventType type, LocalDateTime timestamp,
                               Long conversationId, String description) {}

    @Transactional(readOnly = true)
    public List<JourneyEvent> getJourney(Long customerProfileId, Long tenantId) {
        CustomerProfile profile = customerProfileRepository
            .findByIdAndTenantId(customerProfileId, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Customer profile not found"));

        List<JourneyEvent> events = new ArrayList<>();

        if (profile.getCreatedAt() != null) {
            events.add(new JourneyEvent(JourneyEventType.PROFILE_CREATED,
                profile.getCreatedAt(), null, "Customer profile created"));
        }

        List<Conversation> conversations = conversationRepository
            .findByExternalUserIdAndTenantIdOrderByCreatedAtAsc(profile.getExternalId(), tenantId);

        for (Conversation c : conversations) {
            if (c.getCreatedAt() != null) {
                events.add(new JourneyEvent(JourneyEventType.CONVERSATION_STARTED,
                    c.getCreatedAt(), c.getId(), "Conversation started (" + c.getStatus() + ")"));
            }
            if (c.getAgentAssignedId() != null && c.getFirstAgentResponseTime() != null) {
                events.add(new JourneyEvent(JourneyEventType.AGENT_ASSIGNED,
                    c.getFirstAgentResponseTime(), c.getId(), "Agent assigned"));
            }
            if (c.getCurrentEscalationTier() != null && c.getUpdatedAt() != null) {
                events.add(new JourneyEvent(JourneyEventType.ESCALATED,
                    c.getUpdatedAt(), c.getId(), "Escalated to tier " + c.getCurrentEscalationTier()));
            }
            if ("CLOSED".equalsIgnoreCase(String.valueOf(c.getStatus())) && c.getUpdatedAt() != null) {
                events.add(new JourneyEvent(JourneyEventType.CONVERSATION_CLOSED,
                    c.getUpdatedAt(), c.getId(), "Conversation closed"));
            }

            List<ConversationSentiment> sentiments = sentimentRepository
                .findByTenantIdAndConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(tenantId, c.getId());
            sentiments.stream()
                .filter(s -> s.getLabel() == SentimentLabel.NEGATIVE)
                .findFirst()
                .ifPresent(s -> events.add(new JourneyEvent(JourneyEventType.NEGATIVE_SENTIMENT,
                    s.getCreatedAt(), c.getId(), "Negative sentiment detected (score=" + s.getScore() + ")")));
        }

        List<Feedback> feedbacks = feedbackRepository
            .findByCustomerProfileIdAndTenantId(customerProfileId, tenantId);
        for (Feedback f : feedbacks) {
            LocalDateTime ts = f.getSubmittedAt() != null ? f.getSubmittedAt() : f.getCreatedAt();
            if (ts != null) {
                events.add(new JourneyEvent(JourneyEventType.FEEDBACK_RECEIVED, ts,
                    f.getConversationId(),
                    "Feedback " + f.getType() + " rating=" + f.getRating()));
            }
        }

        events.sort(Comparator.comparing(JourneyEvent::timestamp,
            Comparator.nullsLast(Comparator.naturalOrder())));
        return events;
    }
}
