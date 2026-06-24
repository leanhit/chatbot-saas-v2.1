package com.chatbot.core.identity.exception;

import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;

/**
 * Base exception for identity-related errors.
 * All identity exceptions should extend this class.
 */
public class IdentityException extends BaseException {
    
    public IdentityException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public IdentityException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
