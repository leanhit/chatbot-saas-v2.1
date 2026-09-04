package com.chatbot.spokes.facebook.webhook.controller;

import com.chatbot.spokes.facebook.webhook.dto.WebhookRequest;
import com.chatbot.spokes.facebook.webhook.service.FacebookWebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
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
    private final ObjectMapper objectMapper;

    // Endpoint cho xác thực webhook của Facebook (GET)
    @GetMapping
    public ResponseEntity<String> verifyWebhook(@RequestParam("hub.mode") String mode,
                                              @RequestParam("hub.challenge") String challenge,
                                              @RequestParam("hub.verify_token") String verifyToken) {

        log.info("Received webhook verification request.");
        log.info("Mode: {}, Challenge: {}, Verify Token: {}", mode, challenge, verifyToken);

        // Logic xác thực token trong service
        if (webhookService.verifyWebhook(mode, challenge, verifyToken)) {
            return ResponseEntity.ok(challenge);
        } else {
            return ResponseEntity.badRequest().body("Verification failed.");
        }
    }

    // Endpoint để nhận các sự kiện tin nhắn từ Facebook (POST)
    @PostMapping
    public ResponseEntity<String> handleWebhookEvent(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature256,
            @RequestHeader(value = "X-Hub-Signature", required = false) String signature1,
            @RequestBody String rawPayload) {

        String signatureHeader = signature256 != null ? signature256 : signature1;

        if (!webhookService.verifySignature(rawPayload, signatureHeader)) {
            log.warn("⛔ Rejecting Facebook Webhook request due to invalid signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        try {
            WebhookRequest request = objectMapper.readValue(rawPayload, WebhookRequest.class);
            webhookService.handleWebhookEvent(request);
            return ResponseEntity.ok("EVENT_RECEIVED");
        } catch (Exception e) {
            log.error("❌ Failed to parse or process webhook payload: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Payload processing error");
        }
    }
}
