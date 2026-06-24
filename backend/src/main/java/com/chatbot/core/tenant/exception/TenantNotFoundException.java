package com.chatbot.core.tenant.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class TenantNotFoundException extends TenantException {
    
    public TenantNotFoundException(String message) {
        super(ErrorCode.TENANT_NOT_FOUND, message);
    }
    
    public TenantNotFoundException(String message, Throwable cause) {
        super(ErrorCode.TENANT_NOT_FOUND, message, cause);
    }
    
    public TenantNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public TenantNotFoundException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
