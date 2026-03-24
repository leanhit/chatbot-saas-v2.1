package com.chatbot.core.tenant.exception;

public class TenantStatusTransitionException extends TenantException {
    
    public TenantStatusTransitionException(String message) {
        super("INVALID_STATUS_TRANSITION", message);
    }
    
    public TenantStatusTransitionException(String message, Throwable cause) {
        super("INVALID_STATUS_TRANSITION", message, cause);
    }
}
