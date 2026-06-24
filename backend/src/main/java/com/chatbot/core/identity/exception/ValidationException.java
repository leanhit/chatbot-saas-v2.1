package com.chatbot.core.identity.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class ValidationException extends IdentityException {
    
    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(ErrorCode.VALIDATION_ERROR, message, cause);
    }
}
