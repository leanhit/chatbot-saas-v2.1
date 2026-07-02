package com.chatbot.core.message.decision.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConversationNotFoundException
 */
public class ConversationNotFoundExceptionTest {

    @Test
    public void testConstructorWithMessage() {
        String message = "Conversation not found";
        ConversationNotFoundException exception = new ConversationNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    public void testConstructorWithConversationId() {
        Long conversationId = 123L;
        ConversationNotFoundException exception = new ConversationNotFoundException(conversationId);
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains(conversationId.toString()));
    }

    @Test
    public void testConstructorWithMessageAndCause() {
        String message = "Conversation not found";
        Throwable cause = new RuntimeException("Database error");
        ConversationNotFoundException exception = new ConversationNotFoundException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testConstructorWithConversationIdAndCause() {
        Long conversationId = 123L;
        Throwable cause = new RuntimeException("Database error");
        ConversationNotFoundException exception = new ConversationNotFoundException(conversationId, cause);
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains(conversationId.toString()));
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testErrorCode() {
        ConversationNotFoundException exception = new ConversationNotFoundException(123L);
        
        assertEquals(com.chatbot.shared.exceptions.ErrorCode.CONVERSATION_NOT_FOUND, exception.getErrorCode());
    }
}
