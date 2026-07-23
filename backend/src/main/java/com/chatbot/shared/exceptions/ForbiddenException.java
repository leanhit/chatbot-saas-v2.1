package com.chatbot.shared.exceptions;

/**
 * Exception thrown when a user attempts to access a resource they don't have permission for.
 * HTTP Status: 403 Forbidden
 */
public class ForbiddenException extends BaseException {
    
    public ForbiddenException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(ErrorCode.FORBIDDEN, message, cause);
    }
}
