package com.chatbot.spokes.facebook.connection.exception;

// Sử dụng RuntimeException
public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}