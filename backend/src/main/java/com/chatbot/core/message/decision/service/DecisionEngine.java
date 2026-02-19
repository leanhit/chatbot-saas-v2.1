package com.chatbot.core.message.decision.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Decision Engine - Makes routing decisions for messages
 */
@Service
@Slf4j
public class DecisionEngine {
    
    public String decideDestination(String messageContent, String context) {
        log.info("Making decision for message: {}", messageContent);
        // Implementation will be added
        return "default";
    }
}
