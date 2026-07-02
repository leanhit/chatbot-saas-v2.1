package com.chatbot.core.tenant.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GrpcIntegrationException
 */
public class GrpcIntegrationExceptionTest {

    @Test
    public void testConstructorWithMessage() {
        String message = "gRPC service unavailable";
        GrpcIntegrationException exception = new GrpcIntegrationException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    public void testConstructorWithMessageAndCause() {
        String message = "gRPC service unavailable";
        Throwable cause = new RuntimeException("Connection timeout");
        GrpcIntegrationException exception = new GrpcIntegrationException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testErrorCode() {
        GrpcIntegrationException exception = new GrpcIntegrationException("Test message");
        
        assertEquals(com.chatbot.shared.exceptions.ErrorCode.INTEGRATION_ERROR, exception.getErrorCode());
    }
}
