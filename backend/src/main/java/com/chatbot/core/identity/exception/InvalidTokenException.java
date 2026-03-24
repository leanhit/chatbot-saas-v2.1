package com.chatbot.core.identity.exception;

public class InvalidTokenException extends IdentityException {
    
    public InvalidTokenException(String message) {
        super("INVALID_TOKEN", message);
    }
    
    public InvalidTokenException(String message, Throwable cause) {
        super("INVALID_TOKEN", message, cause);
    }
}
