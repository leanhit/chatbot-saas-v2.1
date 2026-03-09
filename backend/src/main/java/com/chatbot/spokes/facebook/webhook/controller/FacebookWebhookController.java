package com.chatbot.spokes.facebook.webhook.controller;

import com.chatbot.spokes.facebook.webhook.dto.WebhookRequest;
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
@RequestMapping("/webhooks/facebook/botpress")
@RequiredArgsConstructor
@Slf4j
public class FacebookWebhookController {

    private final FacebookWebhookService webhookService;

    // Endpoint cho xác thực webhook của Facebook
    @GetMapping
    public ResponseEntity<String> verifyWebhook(@RequestParam("hub.mode") String mode,
                                              @RequestParam("hub.challenge") String challenge,
                                              @RequestParam("hub.verify_token") String verifyToken) {

        log.info("Received webhook verification request.");
        log.info("Mode: " + mode);
        log.info("Challenge: " + challenge);
        log.info("Verify Token: " + verifyToken);
                                            
        // Logic xác thực token sẽ ở trong service
        if (webhookService.verifyWebhook(mode, challenge, verifyToken)) {
            return ResponseEntity.ok(challenge);
        } else {
            return ResponseEntity.badRequest().body("Verification failed.");
        }
    }

    // Endpoint để nhận các sự kiện tin nhắn từ Facebook
    @PostMapping
    public ResponseEntity<Void> handleWebhookEvent(@RequestBody WebhookRequest request) {
        webhookService.handleWebhookEvent(request);
        return ResponseEntity.ok().build();
    }
}
