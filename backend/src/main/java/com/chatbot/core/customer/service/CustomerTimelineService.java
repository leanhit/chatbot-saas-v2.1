package com.chatbot.core.customer.service;

import com.chatbot.core.customer.dto.TimelineEventDTO;
import com.chatbot.core.customer.model.CustomerNote;
import com.chatbot.core.customer.model.CustomerProfile;
import com.chatbot.core.customer.model.Feedback;
import com.chatbot.core.customer.repository.CustomerNoteRepository;
import com.chatbot.core.customer.repository.CustomerProfileRepository;
import com.chatbot.core.customer.repository.FeedbackRepository;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.Message;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Customer timeline service (Phase 4).
 * Builds a unified timeline from conversations, messages, feedback and notes
 * for a customer profile. All queries are tenant-scoped.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerTimelineService {

    private static final int MAX_MESSAGES_PER_CONVERSATION = 50;

    private final CustomerProfileRepository customerProfileRepository;
    private final FeedbackRepository feedbackRepository;
    private final CustomerNoteRepository noteRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Transactional(readOnly = true)
    public List<TimelineEventDTO> getTimeline(Long customerProfileId, Long tenantId) {
        CustomerProfile profile = customerProfileRepository.findById(customerProfileId)
            .orElseThrow(() -> new IllegalArgumentException("Customer profile not found: " + customerProfileId));
        if (!profile.getTenantId().equals(tenantId)) {
            throw new SecurityException("Customer profile belongs to another tenant");
        }

        List<TimelineEventDTO> events = new ArrayList<>();

        // 1. Conversations (via externalId ↔ externalUserId link)
        if (profile.getExternalId() != null) {
            List<Conversation> conversations = conversationRepository
                .findByExternalUserIdAndTenantId(profile.getExternalId(), tenantId);
            for (Conversation c : conversations) {
                events.add(TimelineEventDTO.builder()
                    .id("conv-" + c.getId())
                    .type("conversation")
                    .title("Conversation #" + c.getId())
                    .description(c.getStatus() != null ? c.getStatus().name() : null)
                    .badge(c.getChannel() != null ? c.getChannel().name() : null)
                    .timestamp(c.getCreatedAt())
                    .referenceId(c.getId())
                    .build());

                // Recent messages in this conversation
                List<Message> messages = messageRepository
                    .findByConversationIdAndTenantId(c.getId(), tenantId);
                int start = Math.max(0, messages.size() - MAX_MESSAGES_PER_CONVERSATION);
                for (int i = start; i < messages.size(); i++) {
                    Message m = messages.get(i);
                    events.add(TimelineEventDTO.builder()
                        .id("msg-" + m.getId())
                        .type("message")
                        .title((m.getSender() != null ? m.getSender() : "unknown") + " message")
                        .description(truncate(m.getContent(), 200))
                        .timestamp(m.getCreatedAt())
                        .referenceId(m.getId())
                        .build());
                }
            }
        }

        // 2. Feedback
        List<Feedback> feedbackList = feedbackRepository
            .findByCustomerProfileIdAndTenantId(customerProfileId, tenantId);
        for (Feedback f : feedbackList) {
            events.add(TimelineEventDTO.builder()
                .id("fb-" + f.getId())
                .type("feedback")
                .title("Rating " + f.getRating() + "/5 — " + (f.getType() != null ? f.getType().name() : "GENERAL"))
                .description(f.getComment())
                .badge(f.getRating() + "★")
                .timestamp(f.getSubmittedAt())
                .referenceId(f.getId())
                .build());
        }

        // 3. Notes
        List<CustomerNote> notes = noteRepository
            .findByTenantIdAndCustomerProfileIdAndIsDeletedFalseOrderByCreatedAtDesc(tenantId, customerProfileId);
        for (CustomerNote n : notes) {
            events.add(TimelineEventDTO.builder()
                .id("note-" + n.getId())
                .type("note")
                .title("Note by " + (n.getAuthorName() != null ? n.getAuthorName() : "agent"))
                .description(n.getContent())
                .timestamp(n.getCreatedAt())
                .referenceId(n.getId())
                .build());
        }

        // 4. First contact marker
        if (profile.getFirstContactAt() != null) {
            events.add(TimelineEventDTO.builder()
                .id("first-contact")
                .type("conversation")
                .title("First contact")
                .timestamp(profile.getFirstContactAt())
                .build());
        }

        events.sort(Comparator.comparing(TimelineEventDTO::timestamp,
            Comparator.nullsLast(Comparator.reverseOrder())));
        return events;
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
