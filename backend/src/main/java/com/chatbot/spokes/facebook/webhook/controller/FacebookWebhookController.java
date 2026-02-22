package com.chatbot.spokes.facebook.webhook.controller;

import com.chatbot.spokes.facebook.webhook.service.FacebookWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Facebook Webhook Controller
 * Handles incoming webhook events from Facebook
 */
@RestController
@RequestMapping("/api/v1/facebook/webhook")
@RequiredArgsConstructor
@Slf4j
public class FacebookWebhookController {

    private final FacebookWebhookService webhookService;

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verifyToken,
            @RequestParam("hub.challenge") String challenge) {
        
        log.info("Facebook webhook verification request: mode={}, verifyToken={}", mode, verifyToken);
        
        if (webhookService.verifyWebhook(mode, verifyToken)) {
            log.info("Facebook webhook verification successful");
            return ResponseEntity.ok(challenge);
        }
        
        log.warn("Facebook webhook verification failed");
        return ResponseEntity.badRequest().body("Verification failed");
    }

    @PostMapping
    public ResponseEntity<String> handleWebhookEvent(@RequestBody String payload) {
        log.info("Received Facebook webhook event");
        
        try {
            webhookService.processWebhookEvent(payload);
            return ResponseEntity.ok("EVENT_RECEIVED");
        } catch (Exception e) {
            log.error("Error processing Facebook webhook event", e);
            return ResponseEntity.internalServerError().body("Error processing event");
        }
    }
}
