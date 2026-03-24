package com.chatbot.core.identity.exception;

public class EmailAlreadyExistsException extends IdentityException {
    
    public EmailAlreadyExistsException(String message) {
        super("EMAIL_EXISTS", message);
    }
    
    public EmailAlreadyExistsException(String message, Throwable cause) {
        super("EMAIL_EXISTS", message, cause);
    }
}
