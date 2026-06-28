package com.chatbot.core.tenant.exception;

import com.chatbot.shared.exceptions.ErrorCode;

/**
 * Exception thrown when a tenant is not active.
 */
public class TenantInactiveException extends TenantException {
    
    public TenantInactiveException(String message) {
        super(ErrorCode.TENANT_INACTIVE, message);
    }
    
    public TenantInactiveException(String message, Throwable cause) {
        super(ErrorCode.TENANT_INACTIVE, message, cause);
    }
}
