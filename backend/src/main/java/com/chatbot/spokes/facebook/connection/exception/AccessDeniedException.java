package com.chatbot.spokes.facebook.connection.exception;

// Sử dụng RuntimeException để không cần try-catch
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}