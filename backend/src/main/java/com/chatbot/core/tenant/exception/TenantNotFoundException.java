package com.chatbot.core.tenant.exception;

public class TenantNotFoundException extends TenantException {
    
    public TenantNotFoundException(String message) {
        super("TENANT_NOT_FOUND", message);
    }
    
    public TenantNotFoundException(String message, Throwable cause) {
        super("TENANT_NOT_FOUND", message, cause);
    }
}
