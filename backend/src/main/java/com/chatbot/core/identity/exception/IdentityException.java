package com.chatbot.core.identity.exception;

public class IdentityException extends RuntimeException {
    
    private final String errorCode;
    
    public IdentityException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public IdentityException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
