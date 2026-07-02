package com.chatbot.spokes.facebook.connection.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

/**
 * Unit tests for ConnectionNotFoundException
 */
public class ConnectionNotFoundExceptionTest {

    @Test
    public void testConstructorWithMessage() {
        String message = "Connection not found";
        ConnectionNotFoundException exception = new ConnectionNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    public void testConstructorWithConnectionId() {
        UUID connectionId = UUID.randomUUID();
        ConnectionNotFoundException exception = new ConnectionNotFoundException(connectionId);
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains(connectionId.toString()));
    }

    @Test
    public void testConstructorWithMessageAndCause() {
        String message = "Connection not found";
        Throwable cause = new RuntimeException("Database error");
        ConnectionNotFoundException exception = new ConnectionNotFoundException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testConstructorWithConnectionIdAndCause() {
        UUID connectionId = UUID.randomUUID();
        Throwable cause = new RuntimeException("Database error");
        ConnectionNotFoundException exception = new ConnectionNotFoundException(connectionId, cause);
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains(connectionId.toString()));
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testErrorCode() {
        ConnectionNotFoundException exception = new ConnectionNotFoundException(UUID.randomUUID());
        
        assertEquals(com.chatbot.shared.exceptions.ErrorCode.CONNECTION_NOT_FOUND, exception.getErrorCode());
    }
}
