package com.chatbot.core.message.router.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Message Router Service - Routes messages to appropriate hubs
 */
@Service
@Slf4j
public class MessageRouterService {
    
    public void routeMessage(String messageId, String destination) {
        log.info("Routing message {} to {}", messageId, destination);
        // Implementation will be added
    }
}
