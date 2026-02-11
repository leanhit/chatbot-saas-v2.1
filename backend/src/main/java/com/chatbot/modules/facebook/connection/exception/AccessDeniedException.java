package com.chatbot.modules.facebook.connection.exception;

// Sử dụng RuntimeException để không cần try-catch
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}