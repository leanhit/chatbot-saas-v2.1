package com.chatbot.spokes.facebook.connection.exception;

import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;

public class ResourceAlreadyExistsException extends BaseException {
    public ResourceAlreadyExistsException(String message) {
        super(ErrorCode.RESOURCE_ALREADY_EXISTS, message);
    }
    
    public ResourceAlreadyExistsException(String message, Throwable cause) {
        super(ErrorCode.RESOURCE_ALREADY_EXISTS, message, cause);
    }
}