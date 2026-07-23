package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.notification.websocket.NotificationWebSocketHandler;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.spokes.facebook.messenger.service.FacebookMessengerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test for TimeoutWorkflow - CRITICAL level testing
 * Tests timeout detection, notification, and auto-reassignment for inactive conversations
 */
@ExtendWith(MockitoExtension.class)
class TimeoutWorkflowTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private NotificationWebSocketHandler notificationWebSocketHandler;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private ConversationEndWorkflow conversationEndWorkflow;

    @Mock
    private FacebookConnectionRepository facebookConnectionRepository;

    @Mock
    private FacebookMessengerService facebookMessengerService;

    @InjectMocks
    private TimeoutWorkflow timeoutWorkflow;

    private Conversation testConversation;
    private Tenant testTenant;

    @BeforeEach
    void setUp() {
        testTenant = new Tenant();
        testTenant.setId(1L);
        testTenant.setStatus(TenantStatus.ACTIVE);

        testConversation = new Conversation();
        testConversation.setId(1L);
        testConversation.setTenantId(1L);
        testConversation.setConnectionId(UUID.fromString("00000000-0000-0000-0000-000000000100"));
        testConversation.setExternalUserId("user123");
        testConversation.setUserName("Test User");
        testConversation.setStatus("open");
        testConversation.setIsTakenOverByAgent(false);
        testConversation.setCreatedAt(LocalDateTime.now().minusMinutes(40));
        testConversation.setUpdatedAt(LocalDateTime.now().minusMinutes(40));
    }

    @Test
    void isConversationInactive_WithInactiveConversation_ShouldReturnTrue() {
        // Act
        boolean result = timeoutWorkflow.isConversationInactive(testConversation);

        // Assert
        assert result : "Conversation should be inactive after 40 minutes";
    }

    @Test
    void isConversationInactive_WithActiveConversation_ShouldReturnFalse() {
        // Arrange
        testConversation.setUpdatedAt(LocalDateTime.now().minusMinutes(10));

        // Act
        boolean result = timeoutWorkflow.isConversationInactive(testConversation);

        // Assert
        assert !result : "Conversation should not be inactive after 10 minutes";
    }

    @Test
    void checkInactiveConversations_WithInactiveConversations_ShouldNotifyAgents() {
        // Arrange
        when(tenantRepository.findAll()).thenReturn(Arrays.asList(testTenant));
        when(conversationRepository.findByTenantIdAndStatus(1L, "open"))
            .thenReturn(Arrays.asList(testConversation));

        // Act
        timeoutWorkflow.checkInactiveConversations();

        // Assert
        verify(notificationWebSocketHandler).broadcastToTenant(eq(1L), anyMap());
    }

    @Test
    void checkInactiveConversations_WithNoActiveTenants_ShouldSkip() {
        // Arrange
        testTenant.setStatus(TenantStatus.INACTIVE);
        when(tenantRepository.findAll()).thenReturn(Arrays.asList(testTenant));

        // Act
        timeoutWorkflow.checkInactiveConversations();

        // Assert
        verify(conversationRepository, never()).findByTenantIdAndStatus(anyLong(), anyString());
    }

    @Test
    void isConversationUnresponded_WithUnrespondedConversation_ShouldReturnTrue() {
        // Arrange
        testConversation.setIsTakenOverByAgent(true);
        testConversation.setFirstAgentResponseTime(null);
        testConversation.setUpdatedAt(LocalDateTime.now().minusMinutes(70));

        // Act
        boolean result = timeoutWorkflow.isConversationUnresponded(testConversation);

        // Assert
        assert result : "Conversation should be unresponded after 70 minutes without agent response";
    }

    @Test
    void isConversationUnresponded_WithAgentResponse_ShouldReturnFalse() {
        // Arrange
        testConversation.setIsTakenOverByAgent(true);
        testConversation.setFirstAgentResponseTime(LocalDateTime.now());
        testConversation.setUpdatedAt(LocalDateTime.now().minusMinutes(70));

        // Act
        boolean result = timeoutWorkflow.isConversationUnresponded(testConversation);

        // Assert
        assert !result : "Conversation should not be unresponded if agent has responded";
    }

    @Test
    void checkUnrespondedConversations_WithUnrespondedConversations_ShouldSendUrgentNotification() {
        // Arrange
        testConversation.setIsTakenOverByAgent(true);
        testConversation.setFirstAgentResponseTime(null);
        testConversation.setUpdatedAt(LocalDateTime.now().minusMinutes(70));
        
        when(tenantRepository.findAll()).thenReturn(Arrays.asList(testTenant));
        when(conversationRepository.findByIsTakenOverByAgentAndTenantId(true, 1L))
            .thenReturn(Arrays.asList(testConversation));

        // Act
        timeoutWorkflow.checkUnrespondedConversations();

        // Assert
        verify(notificationWebSocketHandler).broadcastToTenant(eq(1L), anyMap());
    }

    @Test
    void autoCloseInactiveConversation_WithValidConversation_ShouldCallEndWorkflow() {
        // Arrange
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));

        // Act
        timeoutWorkflow.autoCloseInactiveConversation(1L);

        // Assert
        verify(conversationEndWorkflow).handleConversationEnd(1L, "timeout");
    }

    @Test
    void reassignToBot_WithValidConversation_ShouldReassignToBot() {
        // Arrange
        testConversation.setIsTakenOverByAgent(true);
        testConversation.setAgentAssignedId(10L);
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));

        // Act
        timeoutWorkflow.reassignToBot(1L);

        // Assert
        assert !testConversation.getIsTakenOverByAgent() : "Conversation should not be taken over by agent";
        assert testConversation.getAgentAssignedId() == null : "Agent ID should be null";
        verify(conversationRepository).save(testConversation);
        verify(notificationWebSocketHandler).broadcastToTenant(eq(1L), anyMap());
    }

    @Test
    void reassignToBot_WhenConversationNotFound_ShouldReturnSilently() {
        // Arrange
        when(conversationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        timeoutWorkflow.reassignToBot(999L);

        // Assert
        verify(conversationRepository, never()).save(any());
    }
}
