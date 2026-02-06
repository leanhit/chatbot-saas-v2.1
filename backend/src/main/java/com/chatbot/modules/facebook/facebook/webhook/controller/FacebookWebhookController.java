package com.chatbot.modules.facebook.facebook.webhook.controller;

import com.chatbot.modules.facebook.facebook.webhook.dto.FacebookWebhookRequest;
import com.chatbot.modules.facebook.facebook.webhook.service.FacebookWebhookService;
import com.chatbot.modules.facebook.facebook.connection.repository.FacebookConnectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Facebook Webhook Controller for Phase 0 Integration
 * Receives Facebook webhook events and routes through Message Hub
 */
@RestController
@RequestMapping("/webhook/facebook")
@RequiredArgsConstructor
@Slf4j
public class FacebookWebhookController {

    private final FacebookWebhookService facebookWebhookService;
    private final FacebookConnectionRepository facebookConnectionRepository;

    /**
     * Facebook Webhook verification
     * Facebook sends this when setting up webhook
     */
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verifyToken,
            @RequestParam("hub.challenge") String challenge) {
        
        log.info("Facebook webhook verification - mode: {}, token: {}", mode, verifyToken);
        
        // TODO: Move verify token to configuration
        String expectedToken = "facebook_webhook_verify_token_12345";
        
        if ("subscribe".equals(mode) && expectedToken.equals(verifyToken)) {
            log.info("Facebook webhook verification successful");
            return ResponseEntity.ok(challenge);
        } else {
            log.warn("Facebook webhook verification failed - mode: {}, token: {}", mode, verifyToken);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Facebook Webhook events
     * Receives messages and other events from Facebook
     */
    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody FacebookWebhookRequest webhookRequest) {
        log.info("Received Facebook webhook event: {}", webhookRequest.getObject());

        try {
            // Process messages through Message Hub
            if (webhookRequest.isPageEvent() && webhookRequest.hasMessages()) {
                facebookWebhookService.processWebhookMessage(webhookRequest);
            } else {
                log.debug("Ignoring non-message webhook event: {}", webhookRequest.getObject());
            }

            return ResponseEntity.ok("EVENT_RECEIVED");

        } catch (Exception e) {
            log.error("Error processing Facebook webhook", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
