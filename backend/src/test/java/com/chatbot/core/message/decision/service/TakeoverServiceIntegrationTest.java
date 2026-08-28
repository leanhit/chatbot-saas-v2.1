package com.chatbot.core.message.decision.service;

import com.chatbot.core.message.decision.model.TakeoverMessage;
import com.chatbot.core.message.decision.websocket.TakeoverWebSocketHandler;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.service.MessageService;
import com.chatbot.shared.messenger.ChannelMessengerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit test for TakeoverService
 * Tests takeover logic with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
class TakeoverServiceIntegrationTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @Mock
    private TakeoverWebSocketHandler takeoverWebSocketHandler;

    @Mock
    private MessageService messageService;

    @Mock
    private ChannelMessengerService channelMessengerService;

    @Mock
    private ObjectMapper objectMapper;

    private TakeoverService takeoverService;

    private Conversation testConversation;
    private UUID connectionId;
    private Long conversationId = 1L;

    @BeforeEach
    void setUp() {
        takeoverService = new TakeoverService(
                redisTemplate,
                objectMapper,
                takeoverWebSocketHandler,
                messageService,
                channelMessengerService,
                conversationRepository
        );

        connectionId = UUID.randomUUID();

        testConversation = Conversation.builder()
                .tenantId(1L)
                .externalUserId("user-123")
                .status("open")
                .isTakenOverByAgent(false)
                .connectionId(connectionId)
                .build();
        testConversation.setId(conversationId);

        // Mock Redis operations
        lenient().when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @AfterEach
    void tearDown() {
        reset(redisTemplate, conversationRepository, takeoverWebSocketHandler, messageService, channelMessengerService, objectMapper);
    }

    @Test
    void testSaveMessage_SavesToRedis() throws Exception {
        // Arrange
        TakeoverMessage message = new TakeoverMessage(
                UUID.randomUUID().toString(),
                String.valueOf(conversationId),
                "agent",
                "Test message",
                System.currentTimeMillis()
        );
        when(objectMapper.writeValueAsString(any(TakeoverMessage.class))).thenReturn("{\"test\":\"json\"}");

        // Act
        takeoverService.saveMessage(message);

        // Assert
        verify(redisTemplate.opsForList(), times(1)).rightPush(anyString(), anyString());
        verify(redisTemplate.opsForList(), times(1)).trim(anyString(), anyLong(), anyLong());
        verify(redisTemplate, times(1)).expire(anyString(), anyLong(), any(java.util.concurrent.TimeUnit.class));
    }

    @Test
    void testSaveMessage_NullMessage() {
        // Act & Assert - Should handle null gracefully
        assertThrows(Exception.class, () -> {
            takeoverService.saveMessage(null);
        });
    }
}
