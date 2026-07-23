package com.chatbot.core.message.decision.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class MessageNotFoundException extends ConversationException {
    
    public MessageNotFoundException(Long messageId) {
        super(ErrorCode.CONVERSATION_NOT_FOUND, "Message not found with ID: " + messageId);
    }
    
    public MessageNotFoundException(String message) {
        super(ErrorCode.CONVERSATION_NOT_FOUND, message);
    }
}
