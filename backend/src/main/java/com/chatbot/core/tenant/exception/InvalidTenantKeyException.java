package com.chatbot.core.tenant.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class InvalidTenantKeyException extends TenantException {
    
    public InvalidTenantKeyException(String message) {
        super(ErrorCode.INVALID_TENANT_KEY, message);
    }
    
    public InvalidTenantKeyException(String message, Throwable cause) {
        super(ErrorCode.INVALID_TENANT_KEY, message, cause);
    }
}
