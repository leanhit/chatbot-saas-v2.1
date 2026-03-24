package com.chatbot.core.tenant.exception;

public class InsufficientPermissionException extends TenantException {
    
    public InsufficientPermissionException(String message) {
        super("INSUFFICIENT_PERMISSION", message);
    }
    
    public InsufficientPermissionException(String message, Throwable cause) {
        super("INSUFFICIENT_PERMISSION", message, cause);
    }
}
