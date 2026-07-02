package com.chatbot.spokes.facebook.connection.exception;

import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;

/**
 * Exception thrown when a Facebook connection is not found.
 */
public class ConnectionNotFoundException extends BaseException {
    
    public ConnectionNotFoundException(String message) {
        super(ErrorCode.CONNECTION_NOT_FOUND, message);
    }
    
    public ConnectionNotFoundException(String message, Throwable cause) {
        super(ErrorCode.CONNECTION_NOT_FOUND, message, cause);
    }
    
    public ConnectionNotFoundException(java.util.UUID connectionId) {
        super(ErrorCode.CONNECTION_NOT_FOUND, "Connection not found with ID: " + connectionId);
    }
}
