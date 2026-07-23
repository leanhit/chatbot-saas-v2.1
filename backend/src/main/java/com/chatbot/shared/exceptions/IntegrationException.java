package com.chatbot.shared.exceptions;

/**
 * Exception thrown when external integration operations fail (e.g., Facebook API, gRPC calls).
 * HTTP Status: 502 Bad Gateway or 503 Service Unavailable
 */
public class IntegrationException extends BaseException {
    
    public IntegrationException(String message) {
        super(ErrorCode.INTEGRATION_ERROR, message);
    }
    
    public IntegrationException(String message, Throwable cause) {
        super(ErrorCode.INTEGRATION_ERROR, message, cause);
    }
}
