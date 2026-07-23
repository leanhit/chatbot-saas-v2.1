package com.chatbot.spokes.facebook.connection.exception;

import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;

public class AccessDeniedException extends BaseException {
    public AccessDeniedException(String message) {
        super(ErrorCode.ACCESS_DENIED, message);
    }
    
    public AccessDeniedException(String message, Throwable cause) {
        super(ErrorCode.ACCESS_DENIED, message, cause);
    }
}