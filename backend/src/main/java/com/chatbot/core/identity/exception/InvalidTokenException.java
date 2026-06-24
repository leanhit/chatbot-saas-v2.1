package com.chatbot.core.identity.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class InvalidTokenException extends IdentityException {
    
    public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message);
    }
    
    public InvalidTokenException(String message, Throwable cause) {
        super(ErrorCode.INVALID_TOKEN, message, cause);
    }
    
    public InvalidTokenException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public InvalidTokenException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
