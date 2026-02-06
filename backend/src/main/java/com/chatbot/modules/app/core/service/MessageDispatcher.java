package com.chatbot.modules.app.core.service;

import com.chatbot.modules.app.core.command.SendMessageCommand;
import com.chatbot.modules.facebook.facebook.webhook.service.FacebookMessengerService;
import org.springframework.stereotype.Component;
@Component
/**
 * MessageDispatcher
 *
 * Single responsibility:
 * - Dispatch message after guards passed
 * - NO validation logic here
 * - NO tenant / connection checks
 */
public class MessageDispatcher {

    private final FacebookMessengerService facebookMessengerService;

    public MessageDispatcher(FacebookMessengerService facebookMessengerService) {
        this.facebookMessengerService = facebookMessengerService;
    }

    /**
     * Dispatch SEND_MESSAGE command
     */
    public void dispatchSendMessage(SendMessageCommand command) {
        facebookMessengerService.sendMessageToUser(
                command.getPageId(),
                command.getRecipientId(),
                command.getMessage(),
                command.getSender()
        );
    }
}
