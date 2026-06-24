package com.chatbot.core.tenant.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class InsufficientPermissionException extends TenantException {
    
    public InsufficientPermissionException(String message) {
        super(ErrorCode.INSUFFICIENT_PERMISSION, message);
    }
    
    public InsufficientPermissionException(String message, Throwable cause) {
        super(ErrorCode.INSUFFICIENT_PERMISSION, message, cause);
    }
    
    public InsufficientPermissionException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public InsufficientPermissionException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
