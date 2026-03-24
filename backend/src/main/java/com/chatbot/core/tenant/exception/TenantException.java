package com.chatbot.core.tenant.exception;

public class TenantException extends RuntimeException {
    
    private final String errorCode;
    
    public TenantException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public TenantException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
