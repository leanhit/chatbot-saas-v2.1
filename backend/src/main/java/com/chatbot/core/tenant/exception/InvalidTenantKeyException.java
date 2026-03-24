package com.chatbot.core.tenant.exception;

public class InvalidTenantKeyException extends TenantException {
    
    public InvalidTenantKeyException(String message) {
        super("INVALID_TENANT_KEY", message);
    }
    
    public InvalidTenantKeyException(String message, Throwable cause) {
        super("INVALID_TENANT_KEY", message, cause);
    }
}
