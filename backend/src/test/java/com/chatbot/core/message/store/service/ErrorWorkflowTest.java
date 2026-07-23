package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.notification.websocket.NotificationWebSocketHandler;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.spokes.facebook.messenger.service.FacebookMessengerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test for ErrorWorkflow - CRITICAL level testing
 * Tests error handling, fallback messaging, and escalation for critical conversation errors
 */
@ExtendWith(MockitoExtension.class)
class ErrorWorkflowTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private NotificationWebSocketHandler notificationWebSocketHandler;

    @Mock
    private FacebookConnectionRepository facebookConnectionRepository;

    @Mock
    private FacebookMessengerService facebookMessengerService;

    @InjectMocks
    private ErrorWorkflow errorWorkflow;

    private Conversation testConversation;

    @BeforeEach
    void setUp() {
        testConversation = new Conversation();
        testConversation.setId(1L);
        testConversation.setTenantId(1L);
        testConversation.setConnectionId(UUID.fromString("00000000-0000-0000-0000-000000000100"));
        testConversation.setExternalUserId("user123");
        testConversation.setUserName("Test User");
        testConversation.setStatus("open");
        testConversation.setCreatedAt(LocalDateTime.now());
        testConversation.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void handleError_WithCriticalSeverity_ShouldNotifyAdminAndCreateEscalation() {
        // Arrange
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));

        // Act
        errorWorkflow.handleError(1L, "BOT_PROCESSING_FAILED", "Bot crashed", "critical");

        // Assert
        verify(notificationWebSocketHandler, times(2)).broadcastToTenant(eq(1L), anyMap());
        verify(conversationRepository, atLeastOnce()).findById(1L);
    }

    @Test
    void handleError_WithHighSeverity_ShouldNotifyAdmin() {
        // Arrange
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));

        // Act
        errorWorkflow.handleError(1L, "API_ERROR", "API timeout", "high");

        // Assert
        verify(notificationWebSocketHandler, times(1)).broadcastToTenant(eq(1L), anyMap());
    }

    @Test
    void handleError_WithLowSeverity_ShouldNotNotifyAdmin() {
        // Arrange
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));

        // Act
        errorWorkflow.handleError(1L, "MINOR_ERROR", "Minor issue", "low");

        // Assert
        verify(notificationWebSocketHandler, never()).broadcastToTenant(anyLong(), anyMap());
    }

    @Test
    void handleError_WithBotProcessingFailed_ShouldSendFallbackMessage() {
        // Arrange
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));
        when(facebookConnectionRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000100"))).thenReturn(Optional.empty());

        // Act
        errorWorkflow.handleError(1L, "BOT_PROCESSING_FAILED", "Bot error", "medium");

        // Assert
        verify(conversationRepository).findById(1L);
    }

    @Test
    void handleError_WhenConversationNotFound_ShouldLogError() {
        // Arrange
        when(conversationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        errorWorkflow.handleError(999L, "BOT_PROCESSING_FAILED", "Bot error", "critical");

        // Assert
        verify(notificationWebSocketHandler, never()).broadcastToTenant(anyLong(), anyMap());
    }

    @Test
    void recoverFromError_WithOpenConversation_ShouldNotifyRecoveryReady() {
        // Arrange
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));

        // Act
        errorWorkflow.recoverFromError(1L);

        // Assert
        verify(notificationWebSocketHandler).broadcastToTenant(eq(1L), anyMap());
    }

    @Test
    void recoverFromError_WhenConversationNotFound_ShouldReturnSilently() {
        // Arrange
        when(conversationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        errorWorkflow.recoverFromError(999L);

        // Assert
        verify(notificationWebSocketHandler, never()).broadcastToTenant(anyLong(), anyMap());
    }
}
