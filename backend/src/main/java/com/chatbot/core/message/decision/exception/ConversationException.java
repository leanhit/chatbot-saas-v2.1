package com.chatbot.core.message.decision.exception;

import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;

/**
 * Base exception for conversation-related errors.
 */
public class ConversationException extends BaseException {
    
    public ConversationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public ConversationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
