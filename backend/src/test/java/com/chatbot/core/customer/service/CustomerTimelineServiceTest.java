package com.chatbot.core.customer.service;

import com.chatbot.core.customer.dto.TimelineEventDTO;
import com.chatbot.core.customer.model.CustomerNote;
import com.chatbot.core.customer.model.CustomerProfile;
import com.chatbot.core.customer.model.Feedback;
import com.chatbot.core.customer.repository.CustomerNoteRepository;
import com.chatbot.core.customer.repository.CustomerProfileRepository;
import com.chatbot.core.customer.repository.FeedbackRepository;
import com.chatbot.core.message.store.model.Channel;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.ConversationStatus;
import com.chatbot.core.message.store.model.Message;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerTimelineServiceTest {

    @Mock private CustomerProfileRepository customerProfileRepository;
    @Mock private FeedbackRepository feedbackRepository;
    @Mock private CustomerNoteRepository noteRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private MessageRepository messageRepository;

    @InjectMocks
    private CustomerTimelineService service;

    private CustomerProfile profile(Long id, Long tenantId, String externalId) {
        CustomerProfile p = new CustomerProfile();
        p.setId(id);
        p.setTenantId(tenantId);
        p.setExternalId(externalId);
        p.setFirstContactAt(LocalDateTime.of(2026, 1, 1, 8, 0));
        return p;
    }

    private void stubEmptySources(Long profileId, Long tenantId, String externalId) {
        when(feedbackRepository.findByCustomerProfileIdAndTenantId(profileId, tenantId)).thenReturn(List.of());
        when(noteRepository.findByTenantIdAndCustomerProfileIdAndIsDeletedFalseOrderByCreatedAtDesc(tenantId, profileId))
            .thenReturn(List.of());
        if (externalId != null) {
            when(conversationRepository.findByExternalUserIdAndTenantId(externalId, tenantId)).thenReturn(List.of());
        }
    }

    @Test
    void throwsWhenProfileNotFound() {
        when(customerProfileRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getTimeline(99L, 1L));
    }

    @Test
    void throwsSecurityExceptionForOtherTenant() {
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(profile(1L, 2L, "ext-1")));
        assertThrows(SecurityException.class, () -> service.getTimeline(1L, 1L));
    }

    @Test
    void buildsFullTimelineSortedDescending() {
        CustomerProfile p = profile(1L, 1L, "ext-1");
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(p));

        Conversation c = new Conversation();
        c.setId(10L);
        c.setStatus(ConversationStatus.OPEN);
        c.setChannel(Channel.FACEBOOK);
        c.setCreatedAt(LocalDateTime.of(2026, 1, 2, 10, 0));
        when(conversationRepository.findByExternalUserIdAndTenantId("ext-1", 1L)).thenReturn(List.of(c));

        Message m = new Message();
        m.setId(100L);
        m.setSender("user");
        m.setContent("Hello there");
        m.setCreatedAt(LocalDateTime.of(2026, 1, 2, 10, 5));
        when(messageRepository.findByConversationIdAndTenantId(10L, 1L)).thenReturn(List.of(m));

        Feedback fb = new Feedback();
        fb.setId(50L);
        fb.setRating(4);
        fb.setType(Feedback.FeedbackType.GENERAL);
        fb.setComment("Good service");
        fb.setSubmittedAt(LocalDateTime.of(2026, 1, 3, 9, 0));
        when(feedbackRepository.findByCustomerProfileIdAndTenantId(1L, 1L)).thenReturn(List.of(fb));

        CustomerNote note = new CustomerNote();
        note.setId(60L);
        note.setAuthorName("Agent A");
        note.setContent("Follow up needed");
        note.setCreatedAt(LocalDateTime.of(2026, 1, 4, 9, 0));
        when(noteRepository.findByTenantIdAndCustomerProfileIdAndIsDeletedFalseOrderByCreatedAtDesc(1L, 1L))
            .thenReturn(List.of(note));

        List<TimelineEventDTO> events = service.getTimeline(1L, 1L);

        // conv + msg + fb + note + first-contact = 5 events
        assertEquals(5, events.size());
        // sorted descending by timestamp: note(1/4) > fb(1/3) > msg(1/2 10:05) > conv(1/2 10:00) > first-contact(1/1)
        assertEquals("note-60", events.get(0).id());
        assertEquals("fb-50", events.get(1).id());
        assertEquals("msg-100", events.get(2).id());
        assertEquals("conv-10", events.get(3).id());
        assertEquals("first-contact", events.get(4).id());

        TimelineEventDTO convEvent = events.get(3);
        assertEquals("conversation", convEvent.type());
        assertEquals("FACEBOOK", convEvent.badge());
        assertEquals(10L, convEvent.referenceId());

        TimelineEventDTO msgEvent = events.get(2);
        assertEquals("message", msgEvent.type());
        assertEquals("user message", msgEvent.title());

        TimelineEventDTO fbEvent = events.get(1);
        assertEquals("feedback", fbEvent.type());
        assertEquals("4★", fbEvent.badge());

        TimelineEventDTO noteEvent = events.get(0);
        assertEquals("note", noteEvent.type());
        assertEquals("Note by Agent A", noteEvent.title());
    }

    @Test
    void skipsConversationsWhenExternalIdNull() {
        CustomerProfile p = profile(1L, 1L, null);
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(p));
        stubEmptySources(1L, 1L, null);

        List<TimelineEventDTO> events = service.getTimeline(1L, 1L);

        // only first-contact marker
        assertEquals(1, events.size());
        assertEquals("first-contact", events.get(0).id());
        verify(conversationRepository, never()).findByExternalUserIdAndTenantId(any(), any());
    }

    @Test
    void truncatesLongMessageContent() {
        CustomerProfile p = profile(1L, 1L, "ext-1");
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(p));

        Conversation c = new Conversation();
        c.setId(10L);
        c.setStatus(ConversationStatus.OPEN);
        c.setCreatedAt(LocalDateTime.of(2026, 1, 2, 10, 0));
        when(conversationRepository.findByExternalUserIdAndTenantId("ext-1", 1L)).thenReturn(List.of(c));

        Message m = new Message();
        m.setId(100L);
        m.setSender("bot");
        m.setContent("x".repeat(300));
        m.setCreatedAt(LocalDateTime.of(2026, 1, 2, 10, 5));
        when(messageRepository.findByConversationIdAndTenantId(10L, 1L)).thenReturn(List.of(m));

        when(feedbackRepository.findByCustomerProfileIdAndTenantId(1L, 1L)).thenReturn(List.of());
        when(noteRepository.findByTenantIdAndCustomerProfileIdAndIsDeletedFalseOrderByCreatedAtDesc(1L, 1L))
            .thenReturn(List.of());

        List<TimelineEventDTO> events = service.getTimeline(1L, 1L);
        TimelineEventDTO msgEvent = events.stream()
            .filter(e -> "msg-100".equals(e.id())).findFirst().orElseThrow();

        assertTrue(msgEvent.description().length() <= 202); // 200 chars + ellipsis
        assertTrue(msgEvent.description().endsWith("…"));
    }

    @Test
    void onlyIncludesLast50MessagesPerConversation() {
        CustomerProfile p = profile(1L, 1L, "ext-1");
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(p));

        Conversation c = new Conversation();
        c.setId(10L);
        c.setStatus(ConversationStatus.OPEN);
        c.setCreatedAt(LocalDateTime.of(2026, 1, 2, 10, 0));
        when(conversationRepository.findByExternalUserIdAndTenantId("ext-1", 1L)).thenReturn(List.of(c));

        List<Message> messages = new ArrayList<>();
        for (long i = 1; i <= 60; i++) {
            Message m = new Message();
            m.setId(i);
            m.setSender("user");
            m.setContent("msg " + i);
            m.setCreatedAt(LocalDateTime.of(2026, 1, 2, 10, 0).plusMinutes(i));
            messages.add(m);
        }
        when(messageRepository.findByConversationIdAndTenantId(10L, 1L)).thenReturn(messages);
        when(feedbackRepository.findByCustomerProfileIdAndTenantId(1L, 1L)).thenReturn(List.of());
        when(noteRepository.findByTenantIdAndCustomerProfileIdAndIsDeletedFalseOrderByCreatedAtDesc(1L, 1L))
            .thenReturn(List.of());

        List<TimelineEventDTO> events = service.getTimeline(1L, 1L);
        long msgEvents = events.stream().filter(e -> "message".equals(e.type())).count();

        assertEquals(50, msgEvents); // capped at MAX_MESSAGES_PER_CONVERSATION
        // oldest included should be msg-11 (60-50+1)
        assertTrue(events.stream().anyMatch(e -> "msg-11".equals(e.id())));
        assertFalse(events.stream().anyMatch(e -> "msg-10".equals(e.id())));
    }

    @Test
    void nullTimestampsSortLast() {
        CustomerProfile p = profile(1L, 1L, null);
        p.setFirstContactAt(null); // no first-contact marker
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(p));

        Feedback fb = new Feedback();
        fb.setId(50L);
        fb.setRating(3);
        fb.setSubmittedAt(null); // null timestamp
        when(feedbackRepository.findByCustomerProfileIdAndTenantId(1L, 1L)).thenReturn(List.of(fb));
        when(noteRepository.findByTenantIdAndCustomerProfileIdAndIsDeletedFalseOrderByCreatedAtDesc(1L, 1L))
            .thenReturn(List.of());

        List<TimelineEventDTO> events = service.getTimeline(1L, 1L);
        assertEquals(1, events.size());
        assertEquals("fb-50", events.get(0).id());
    }
}
