package com.chatbot.core.tenant.exception;

import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;

/**
 * Base exception for tenant-related errors.
 * All tenant exceptions should extend this class.
 */
public class TenantException extends BaseException {
    
    public TenantException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public TenantException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
