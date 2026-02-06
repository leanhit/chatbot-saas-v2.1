package com.chatbot.modules.app.core.guard;

/**
 * Domain exceptions for App Service Guard
 * Thrown when guard validation fails
 */
public class GuardException extends RuntimeException {
    
    public GuardException(String message) {
        super(message);
    }
    
    public GuardException(String message, Throwable cause) {
        super(message, cause);
    }
}
