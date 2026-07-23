package com.chatbot.core.message.decision.exception;

import com.chatbot.shared.exceptions.ErrorCode;

public class AgentNotFoundException extends ConversationException {
    
    public AgentNotFoundException(Long agentId) {
        super(ErrorCode.NOT_FOUND, "Agent not found: " + agentId);
    }
}
