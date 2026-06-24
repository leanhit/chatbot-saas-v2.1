package com.chatbot.core.tenant.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class TenantStatusTransitionException extends TenantException {
    
    public TenantStatusTransitionException(String message) {
        super(ErrorCode.TENANT_STATUS_TRANSITION, message);
    }
    
    public TenantStatusTransitionException(String message, Throwable cause) {
        super(ErrorCode.TENANT_STATUS_TRANSITION, message, cause);
    }
}
