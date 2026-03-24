package com.chatbot.core.tenant.exception;

public class TenantProfileException extends TenantException {
    
    public TenantProfileException(String message) {
        super("TENANT_PROFILE_ERROR", message);
    }
    
    public TenantProfileException(String message, Throwable cause) {
        super("TENANT_PROFILE_ERROR", message, cause);
    }
}
