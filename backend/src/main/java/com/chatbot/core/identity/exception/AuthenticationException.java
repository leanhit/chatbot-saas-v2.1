package com.chatbot.core.identity.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class AuthenticationException extends IdentityException {
    
    public AuthenticationException(String message) {
        super(ErrorCode.AUTHENTICATION_FAILED, message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(ErrorCode.AUTHENTICATION_FAILED, message, cause);
    }
}
