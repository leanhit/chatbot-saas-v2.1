package com.chatbot.core.identity.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class UserNotFoundException extends IdentityException {
    
    public UserNotFoundException(String message) {
        super(ErrorCode.USER_NOT_FOUND, message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(ErrorCode.USER_NOT_FOUND, message, cause);
    }
}
