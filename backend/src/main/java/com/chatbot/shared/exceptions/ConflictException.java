package com.chatbot.shared.exceptions;

/**
 * Exception thrown when a request conflicts with the current state of the target resource.
 * HTTP Status: 409 Conflict
 * Common use cases: duplicate resources, concurrent modification conflicts
 */
public class ConflictException extends BaseException {
    
    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, message);
    }
    
    public ConflictException(String message, Throwable cause) {
        super(ErrorCode.CONFLICT, message, cause);
    }
}
