package com.chatbot.shared.exceptions;

/**
 * Base exception class for all custom exceptions in the application.
 * All domain-specific exceptions should extend this class.
 * Uses ErrorCode enum for type-safe error codes.
 */
public class BaseException extends RuntimeException {
    
    private final ErrorCode errorCode;
    
    public BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BaseException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    public String getErrorCodeValue() {
        return errorCode != null ? errorCode.getCode() : null;
    }
}
