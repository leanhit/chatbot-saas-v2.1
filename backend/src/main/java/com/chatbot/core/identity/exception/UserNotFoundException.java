package com.chatbot.core.identity.exception;

public class UserNotFoundException extends IdentityException {
    
    public UserNotFoundException(String message) {
        super("USER_NOT_FOUND", message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super("USER_NOT_FOUND", message, cause);
    }
}
