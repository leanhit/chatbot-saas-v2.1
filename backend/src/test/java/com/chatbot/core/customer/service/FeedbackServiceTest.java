package com.chatbot.core.customer.service;

import com.chatbot.core.customer.dto.CreateFeedbackRequest;
import com.chatbot.core.customer.repository.CustomerProfileRepository;
import com.chatbot.core.customer.repository.FeedbackRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for feedback tenant validation (no Spring context).
 */
@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;
    @Mock
    private CustomerProfileRepository customerProfileRepository;
    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private FeedbackService service;

    private CreateFeedbackRequest request(Long customerProfileId, Long conversationId) {
        CreateFeedbackRequest req = new CreateFeedbackRequest();
        req.setCustomerProfileId(customerProfileId);
        req.setConversationId(conversationId);
        req.setRating(5);
        return req;
    }

    @Test
    void rejectsFeedbackForOtherTenantCustomerProfile() {
        CreateFeedbackRequest req = request(10L, null);
        when(customerProfileRepository.existsByIdAndTenantId(10L, 1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.createFeedback(req, 1L));
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void rejectsFeedbackForOtherTenantConversation() {
        CreateFeedbackRequest req = request(null, 20L);
        when(conversationRepository.findByIdAndTenantId(20L, 1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.createFeedback(req, 1L));
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void acceptsFeedbackWithValidTenantLinks() {
        CreateFeedbackRequest req = request(10L, 20L);
        when(customerProfileRepository.existsByIdAndTenantId(10L, 1L)).thenReturn(true);
        when(conversationRepository.findByIdAndTenantId(20L, 1L))
            .thenReturn(Optional.of(new com.chatbot.core.message.store.model.Conversation()));
        when(feedbackRepository.save(any())).thenAnswer(inv -> {
            var fb = inv.getArgument(0, com.chatbot.core.customer.model.Feedback.class);
            fb.setId(99L);
            return fb;
        });

        var dto = service.createFeedback(req, 1L);

        assertNotNull(dto);
        verify(feedbackRepository).save(any());
    }

    @Test
    void acceptsFeedbackWithoutLinks() {
        CreateFeedbackRequest req = request(null, null);
        when(feedbackRepository.save(any())).thenAnswer(inv -> {
            var fb = inv.getArgument(0, com.chatbot.core.customer.model.Feedback.class);
            fb.setId(100L);
            return fb;
        });

        var dto = service.createFeedback(req, 1L);

        assertNotNull(dto);
        verify(customerProfileRepository, never()).existsByIdAndTenantId(any(), any());
    }
}
