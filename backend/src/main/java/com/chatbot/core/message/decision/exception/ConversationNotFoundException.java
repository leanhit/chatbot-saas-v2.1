package com.chatbot.core.message.decision.exception;

import com.chatbot.shared.exceptions.ErrorCode;

/**
 * Exception thrown when a conversation is not found.
 */
public class ConversationNotFoundException extends ConversationException {
    
    public ConversationNotFoundException(String message) {
        super(ErrorCode.CONVERSATION_NOT_FOUND, message);
    }
    
    public ConversationNotFoundException(String message, Throwable cause) {
        super(ErrorCode.CONVERSATION_NOT_FOUND, message, cause);
    }
    
    public ConversationNotFoundException(Long conversationId) {
        super(ErrorCode.CONVERSATION_NOT_FOUND, "Conversation not found with ID: " + conversationId);
    }

    public ConversationNotFoundException(Long conversationId, Throwable cause) {
        super(ErrorCode.CONVERSATION_NOT_FOUND, "Conversation not found with ID: " + conversationId, cause);
    }
}
