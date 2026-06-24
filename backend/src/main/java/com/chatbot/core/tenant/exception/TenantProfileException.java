package com.chatbot.core.tenant.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class TenantProfileException extends TenantException {
    
    public TenantProfileException(String message) {
        super(ErrorCode.TENANT_PROFILE_ERROR, message);
    }
    
    public TenantProfileException(String message, Throwable cause) {
        super(ErrorCode.TENANT_PROFILE_ERROR, message, cause);
    }
}
