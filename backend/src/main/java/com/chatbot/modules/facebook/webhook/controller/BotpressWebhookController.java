// src/main/java/com/chatbot/chatHub/facebook/webhook/controller/BotpressWebhookController.java
package com.chatbot.modules.facebook.webhook.controller;
import lombok.extern.slf4j.Slf4j;

import com.chatbot.modules.facebook.webhook.dto.BotpressReplyRequest;
import com.chatbot.modules.facebook.webhook.service.FacebookMessengerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller nhận các yêu cầu từ Botpress.
 */
@RestController
@RequestMapping("/webhooks/botpress/facebook")
@Slf4j
public class BotpressWebhookController {
    
    private final FacebookMessengerService facebookMessengerService;

    public BotpressWebhookController(FacebookMessengerService facebookMessengerService) {
        this.facebookMessengerService = facebookMessengerService;
    }

    @PostMapping("/{pageId}")
    public ResponseEntity<Void> handleBotpressReply(@PathVariable String pageId, @RequestBody BotpressReplyRequest request) {
        log.info("Received reply from Botpress for page " + pageId + " to recipient " + request.getRecipientId());
        
        facebookMessengerService.sendMessageToUser(
            pageId, 
            request.getRecipientId(), 
            request.getPayload().getText(),
            "bot" // Gửi tin nhắn với sender là 'bot'
        );
        
        return ResponseEntity.ok().build();
    }
}
