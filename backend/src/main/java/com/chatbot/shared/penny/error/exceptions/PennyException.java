package com.chatbot.shared.penny.error.exceptions;

/**
 * Base exception for Penny Middleware
 */
public class PennyException extends RuntimeException {
    
    private final String errorCode;
    
    public PennyException(String message) {
        super(message);
        this.errorCode = "PENNY_UNKNOWN";
    }
    
    public PennyException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public PennyException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PENNY_UNKNOWN";
    }
    
    public PennyException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
