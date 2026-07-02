package com.chatbot.core.tenant.membership.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationException
 */
public class NotificationExceptionTest {

    @Test
    public void testConstructorWithMessage() {
        String message = "Failed to send notification";
        NotificationException exception = new NotificationException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    public void testConstructorWithMessageAndCause() {
        String message = "Failed to send notification";
        Throwable cause = new RuntimeException("Network error");
        NotificationException exception = new NotificationException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testErrorCode() {
        NotificationException exception = new NotificationException("Test message");
        
        assertEquals(com.chatbot.shared.exceptions.ErrorCode.NOTIFICATION_ERROR, exception.getErrorCode());
    }
}
