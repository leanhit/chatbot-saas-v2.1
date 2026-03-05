package com.chatbot.spokes.facebook.webhook.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Facebook Webhook Service
 * Processes webhook events from Facebook and routes to PennyBot
 */
@Service
@Slf4j
public class FacebookWebhookService {

    private static final String VERIFY_TOKEN = "your_facebook_verify_token";
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // TODO: Inject message router service when available
    // @Autowired
    // private MessageRouterService messageRouterService;

    public boolean verifyWebhook(String mode, String verifyToken) {
        return "subscribe".equals(mode) && VERIFY_TOKEN.equals(verifyToken);
    }

    public void processWebhookEvent(String payload) {
        log.info("Processing Facebook webhook event: {}", payload);
        
        try {
            JsonNode jsonPayload = objectMapper.readTree(payload);
            
            // Check if this is a message event
            if (jsonPayload.has("object") && "page".equals(jsonPayload.get("object").asText())) {
                JsonNode entries = jsonPayload.get("entry");
                
                for (JsonNode entry : entries) {
                    JsonNode messaging = entry.get("messaging");
                    
                    if (messaging != null && messaging.isArray()) {
                        for (JsonNode messageEvent : messaging) {
                            processMessageEvent(messageEvent);
                        }
                    }
                }
            }
            
            // TODO: Route to message hub for processing
            // messageRouterService.routeMessage(payload);
            
        } catch (Exception e) {
            log.error("Error processing Facebook webhook event", e);
            throw new RuntimeException("Failed to process webhook event", e);
        }
    }
    
    private void processMessageEvent(JsonNode messageEvent) {
        try {
            String senderId = messageEvent.get("sender").get("id").asText();
            
            // Check if it's a message
            if (messageEvent.has("message")) {
                JsonNode message = messageEvent.get("message");
                String messageText = "";
                
                if (message.has("text")) {
                    messageText = message.get("text").asText();
                }
                
                log.info("📱 Received message from sender {}: {}", senderId, messageText);
                
                // TODO: Route to PennyBot for processing
                // routeToPennyBot(senderId, messageText);
                
            } else if (messageEvent.has("postback")) {
                String postbackPayload = messageEvent.get("postback").get("payload").asText();
                log.info("📱 Received postback from sender {}: {}", senderId, postbackPayload);
                
                // TODO: Handle postback
                // handlePostback(senderId, postbackPayload);
                
            } else if (messageEvent.has("delivery")) {
                log.info("📱 Message delivery confirmation for sender {}", senderId);
                
            } else if (messageEvent.has("read")) {
                log.info("📱 Message read confirmation for sender {}", senderId);
                
            } else {
                log.info("📱 Received other event type from sender {}: {}", senderId, messageEvent);
            }
            
        } catch (Exception e) {
            log.error("Error processing message event", e);
        }
    }
    
    // TODO: Implement when message routing is ready
    /*
    private void routeToPennyBot(String senderId, String messageText) {
        // Find active Facebook connection for this sender
        // Route message to PennyBot provider
        // Send response back to Facebook
    }
    */
}
