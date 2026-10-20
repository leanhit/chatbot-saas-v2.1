package com.chatbot.core.message.store.service;

import com.chatbot.core.message.decision.exception.ConversationNotFoundException;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.Message;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.core.membership.model.TenantRole;
import com.chatbot.core.membership.service.TenantMemberService;
import com.chatbot.core.message.usage.service.MessageUsageService;
import com.chatbot.core.tenant.infra.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test for MessageService
 * Tests role-based permission checks for message access
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageUsageService messageUsageService;

    @Mock
    private TenantMemberService tenantMemberService;

    private MessageService messageService;

    private Long testTenantId = 1L;
    private Long testConversationId = 1L;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(
                messageRepository,
                conversationRepository,
                messageUsageService,
                tenantMemberService
        );

        // Set tenant context
        TenantContext.setTenantId(testTenantId);
    }

    @Test
    void testGetMessages_OwnerRole_CanAccessAllMessages() {
        // Arrange
        when(tenantMemberService.getCurrentUserRole(testTenantId)).thenReturn(TenantRole.OWNER);

        Message message1 = createMessage(1L, "user", "Hello");
        Message message2 = createMessage(2L, "bot", "Hi there");
        List<Message> messages = Arrays.asList(message1, message2);
        Page<Message> messagePage = new PageImpl<>(messages);

        when(messageRepository.findByConversationIdAndTenantIdOrderByCreatedAtDesc(
                eq(testConversationId), eq(testTenantId), any(Pageable.class)))
                .thenReturn(messagePage);

        // Act
        Page<Message> result = messageService.getMessages(testConversationId, 0, 10);

        // Assert
        assertEquals(2, result.getContent().size());
        verify(tenantMemberService, times(1)).getCurrentUserRole(testTenantId);
        verify(messageRepository, times(1))
                .findByConversationIdAndTenantIdOrderByCreatedAtDesc(anyLong(), anyLong(), any(Pageable.class));
    }

    @Test
    void testGetMessages_EditorRole_CanAccessAllMessages() {
        // Arrange
        when(tenantMemberService.getCurrentUserRole(testTenantId)).thenReturn(TenantRole.EDITOR);

        Message message1 = createMessage(1L, "user", "Hello");
        List<Message> messages = Arrays.asList(message1);
        Page<Message> messagePage = new PageImpl<>(messages);

        when(messageRepository.findByConversationIdAndTenantIdOrderByCreatedAtDesc(
                eq(testConversationId), eq(testTenantId), any(Pageable.class)))
                .thenReturn(messagePage);

        // Act
        Page<Message> result = messageService.getMessages(testConversationId, 0, 10);

        // Assert
        assertEquals(1, result.getContent().size());
        verify(tenantMemberService, times(1)).getCurrentUserRole(testTenantId);
    }

    @Test
    void testGetMessages_MemberRole_CanAccessMessages() {
        // Arrange
        when(tenantMemberService.getCurrentUserRole(testTenantId)).thenReturn(TenantRole.MEMBER);

        Message message1 = createMessage(1L, "user", "Hello");
        List<Message> messages = Arrays.asList(message1);
        Page<Message> messagePage = new PageImpl<>(messages);

        when(messageRepository.findByConversationIdAndTenantIdOrderByCreatedAtDesc(
                eq(testConversationId), eq(testTenantId), any(Pageable.class)))
                .thenReturn(messagePage);

        // Act
        Page<Message> result = messageService.getMessages(testConversationId, 0, 10);

        // Assert
        assertEquals(1, result.getContent().size());
        verify(tenantMemberService, times(1)).getCurrentUserRole(testTenantId);
        // Note: Full assignment check for MEMBER role is not yet implemented (TODO in code)
    }

    @Test
    void testGetMessages_NoneRole_CanAccessMessages() {
        // Arrange
        when(tenantMemberService.getCurrentUserRole(testTenantId)).thenReturn(TenantRole.NONE);

        Message message1 = createMessage(1L, "user", "Hello");
        List<Message> messages = Arrays.asList(message1);
        Page<Message> messagePage = new PageImpl<>(messages);

        when(messageRepository.findByConversationIdAndTenantIdOrderByCreatedAtDesc(
                eq(testConversationId), eq(testTenantId), any(Pageable.class)))
                .thenReturn(messagePage);

        // Act
        Page<Message> result = messageService.getMessages(testConversationId, 0, 10);

        // Assert
        assertEquals(1, result.getContent().size());
        verify(tenantMemberService, times(1)).getCurrentUserRole(testTenantId);
    }

    @Test
    void testGetMessages_MarksUserMessagesAsRead() {
        // Arrange
        when(tenantMemberService.getCurrentUserRole(testTenantId)).thenReturn(TenantRole.OWNER);

        Message unreadMessage = createMessage(1L, "user", "Hello");
        unreadMessage.setIsRead(false);
        Message readMessage = createMessage(2L, "bot", "Hi");
        readMessage.setIsRead(true);
        List<Message> messages = Arrays.asList(unreadMessage, readMessage);
        Page<Message> messagePage = new PageImpl<>(messages);

        when(messageRepository.findByConversationIdAndTenantIdOrderByCreatedAtDesc(
                eq(testConversationId), eq(testTenantId), any(Pageable.class)))
                .thenReturn(messagePage);

        when(messageRepository.save(any(Message.class))).thenReturn(unreadMessage);

        // Act
        Page<Message> result = messageService.getMessages(testConversationId, 0, 10);

        // Assert
        assertEquals(2, result.getContent().size());
        verify(messageRepository, times(1)).save(unreadMessage); // Should mark user message as read
    }

    @Test
    void testGetMessages_NoTenantContext_ThrowsException() {
        // Arrange
        TenantContext.clear();

        // Act & Assert
        assertThrows(com.chatbot.shared.exceptions.BaseException.class, () -> {
            messageService.getMessages(testConversationId, 0, 10);
        });

        // Restore tenant context
        TenantContext.setTenantId(testTenantId);
    }

    @Test
    void testSaveMessage_Success() {
        // Arrange
        Conversation conversation = Conversation.builder()
                .id(testConversationId)
                .tenantId(testTenantId)
                .build();

        when(conversationRepository.findById(testConversationId))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        // Act
        Message result = messageService.saveMessage(
                testConversationId,
                "user",
                "Test message",
                "TEXT",
                null
        );

        // Assert
        assertNotNull(result);
        assertEquals("user", result.getSender());
        assertEquals("Test message", result.getContent());
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void testSaveMessage_UpdatesConversationLastMessage() {
        // Arrange
        Conversation conversation = Conversation.builder()
                .id(testConversationId)
                .tenantId(testTenantId)
                .build();

        when(conversationRepository.findById(testConversationId))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message msg = invocation.getArgument(0);
            msg.setId(1L);
            return msg;
        });
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        // Act
        messageService.saveMessage(
                testConversationId,
                "user",
                "Test message",
                "TEXT",
                null
        );

        // Assert
        verify(conversationRepository, times(1)).save(any(Conversation.class));
    }

    @Test
    void testDeleteMessage_Success() {
        // Arrange
        Long messageId = 1L;
        when(messageRepository.existsById(messageId)).thenReturn(true);

        // Act
        messageService.deleteMessage(messageId);

        // Assert
        verify(messageRepository, times(1)).deleteById(messageId);
    }

    @Test
    void testDeleteMessage_MessageNotFound_ThrowsException() {
        // Arrange
        Long messageId = 999L;
        when(messageRepository.existsById(messageId)).thenReturn(false);

        // Act & Assert
        assertThrows(ConversationNotFoundException.class, () -> {
            messageService.deleteMessage(messageId);
        });
    }

    // Helper method
    private Message createMessage(Long id, String sender, String content) {
        Message message = new Message();
        message.setId(id);
        message.setConversationId(testConversationId);
        message.setTenantId(testTenantId);
        message.setSender(sender);
        message.setContent(content);
        message.setMessageType("TEXT");
        message.setIsRead(false);
        return message;
    }
}
