package com.chatbot.core.tenant.exception;

import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;

public class BusinessLogicException extends BaseException {
    public BusinessLogicException(String message) {
        super(ErrorCode.CONFLICT, message);
    }
    
    public BusinessLogicException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public BusinessLogicException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
