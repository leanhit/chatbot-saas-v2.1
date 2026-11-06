package com.chatbot.core.ai.service;

import com.chatbot.core.ai.model.AiSuggestion;
import com.chatbot.core.ai.model.AiSuggestionStatus;
import com.chatbot.core.ai.repository.AiSuggestionRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.core.penny.providers.llm.LlmRoutingService;
import com.chatbot.core.penny.repository.PennyBotRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AI suggestion state transitions and tenant scoping (no Spring context).
 */
@ExtendWith(MockitoExtension.class)
class AiSuggestionServiceTest {

    @Mock
    private AiSuggestionRepository suggestionRepository;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private PennyBotRepository pennyBotRepository;
    @Mock
    private LlmRoutingService llmRoutingService;

    @InjectMocks
    private AiSuggestionService service;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private AiSuggestion pendingSuggestion(Long id) {
        AiSuggestion suggestion = AiSuggestion.builder()
            .id(id)
            .conversationId(10L)
            .suggestionText("Try offering a refund.")
            .status(AiSuggestionStatus.PENDING)
            .build();
        suggestion.setTenantId(1L);
        return suggestion;
    }

    @Test
    void acceptPendingSuggestionTransitionsToAccepted() {
        AiSuggestion suggestion = pendingSuggestion(5L);
        when(suggestionRepository.findByIdAndTenantIdAndIsDeletedFalse(5L, 1L))
            .thenReturn(Optional.of(suggestion));
        when(suggestionRepository.save(any(AiSuggestion.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<AiSuggestion> result = service.acceptSuggestion(5L);

        assertTrue(result.isPresent());
        assertEquals(AiSuggestionStatus.ACCEPTED, result.get().getStatus());
        assertNotNull(result.get().getRespondedAt());
    }

    @Test
    void rejectPendingSuggestionTransitionsToRejectedWithNote() {
        AiSuggestion suggestion = pendingSuggestion(6L);
        when(suggestionRepository.findByIdAndTenantIdAndIsDeletedFalse(6L, 1L))
            .thenReturn(Optional.of(suggestion));
        when(suggestionRepository.save(any(AiSuggestion.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<AiSuggestion> result = service.rejectSuggestion(6L, "not relevant");

        assertTrue(result.isPresent());
        assertEquals(AiSuggestionStatus.REJECTED, result.get().getStatus());
        assertEquals("not relevant", result.get().getResponseNote());
    }

    @Test
    void cannotRespondToAlreadyAcceptedSuggestion() {
        AiSuggestion suggestion = pendingSuggestion(7L);
        suggestion.setStatus(AiSuggestionStatus.ACCEPTED);
        when(suggestionRepository.findByIdAndTenantIdAndIsDeletedFalse(7L, 1L))
            .thenReturn(Optional.of(suggestion));

        Optional<AiSuggestion> result = service.acceptSuggestion(7L);

        assertTrue(result.isEmpty());
        verify(suggestionRepository, never()).save(any());
    }

    @Test
    void suggestionFromOtherTenantIsNotFound() {
        when(suggestionRepository.findByIdAndTenantIdAndIsDeletedFalse(8L, 1L))
            .thenReturn(Optional.empty());

        Optional<AiSuggestion> result = service.acceptSuggestion(8L);

        assertTrue(result.isEmpty());
        verify(suggestionRepository, never()).save(any());
    }

    @Test
    void noTenantContextReturnsEmpty() {
        TenantContext.clear();

        Optional<AiSuggestion> result = service.acceptSuggestion(5L);

        assertTrue(result.isEmpty());
        verifyNoInteractions(suggestionRepository);
    }

    @Test
    void generateSuggestionReturnsEmptyWhenNoTenantContext() {
        TenantContext.clear();

        Optional<AiSuggestion> result = service.generateSuggestion(10L, 1L);

        assertTrue(result.isEmpty());
        verifyNoInteractions(llmRoutingService);
    }
}
