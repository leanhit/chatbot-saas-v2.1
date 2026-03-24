package com.chatbot.core.identity.exception;

public class AuthenticationException extends IdentityException {
    
    public AuthenticationException(String message) {
        super("AUTH_ERROR", message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super("AUTH_ERROR", message, cause);
    }
}
