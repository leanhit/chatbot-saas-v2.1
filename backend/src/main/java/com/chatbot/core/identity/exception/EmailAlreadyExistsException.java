package com.chatbot.core.identity.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class EmailAlreadyExistsException extends IdentityException {
    
    public EmailAlreadyExistsException(String message) {
        super(ErrorCode.EMAIL_ALREADY_EXISTS, message);
    }
    
    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(ErrorCode.EMAIL_ALREADY_EXISTS, message, cause);
    }
}
