package com.chatbot.shared.exceptions;

/**
 * Base exception class for all custom exceptions in the application.
 * All domain-specific exceptions should extend this class.
 * Uses ErrorCode enum for type-safe error codes.
 */
public class BaseException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private java.util.Map<String, Object> details;
    
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

    public java.util.Map<String, Object> getDetails() {
        return details;
    }

    public BaseException withDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new java.util.HashMap<>();
        }
        this.details.put(key, value);
        return this;
    }
}
