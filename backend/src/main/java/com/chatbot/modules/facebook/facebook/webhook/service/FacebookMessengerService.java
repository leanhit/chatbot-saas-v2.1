package com.chatbot.modules.facebook.facebook.webhook.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FacebookMessengerService {
    
    /**
     * Send message to Facebook user
     * 
     * @param pageId Facebook page ID
     * @param recipientId Facebook user ID
     * @param message Message content
     * @param sender Sender type (bot, agent, penny, etc.)
     */
    public void sendMessageToUser(String pageId, String recipientId, String message, String sender) {
        // STUB: Business logic will be implemented later
        log.info("STUB: sendMessageToUser called - pageId: {}, recipientId: {}, message: {}, sender: {}", 
                pageId, recipientId, message, sender);
    }
}
