package com.chatbot.core.message.decision.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class ConversationPermissionDeniedException extends ConversationException {
    
    public ConversationPermissionDeniedException(String message) {
        super(ErrorCode.ACCESS_DENIED, message);
    }
}
