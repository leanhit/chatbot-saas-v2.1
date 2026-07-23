package com.chatbot.spokes.facebook.exception;

import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;

public class FacebookApiException extends BaseException {
    
    public FacebookApiException(String message) {
        super(ErrorCode.INTEGRATION_ERROR, message);
    }
    
    public FacebookApiException(String message, Throwable cause) {
        super(ErrorCode.INTEGRATION_ERROR, message, cause);
    }
}
