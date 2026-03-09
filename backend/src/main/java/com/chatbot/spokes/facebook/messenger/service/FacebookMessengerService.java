package com.chatbot.spokes.facebook.messenger.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

/**
 * Facebook Messenger Service
 * Sends messages back to Facebook users via Graph API
 * Following traloitudongV2 pattern with WebClient and retry logic
 */
@Service
@Slf4j
public class FacebookMessengerService {

    private final WebClient webClient;

    @Value("${facebook.graph.api.url:https://graph.facebook.com}")
    private String graphApiUrl;

    public FacebookMessengerService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Send text message to Facebook user
     * Following traloitudongV2 pattern with retry logic
     */
    public void sendTextMessage(String pageAccessToken, String recipientId, String text) {
        String url = graphApiUrl + "/v18.0/me/messages?access_token=" + pageAccessToken;
        
        Map<String, Object> payload = Map.of(
            "messaging_type", "RESPONSE",
            "recipient", Map.of("id", recipientId),
            "message", Map.of("text", text)
        );

        // Retry logic following traloitudongV2 pattern
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                webClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(payload)
                        .retrieve()
                        .toBodilessEntity()
                        .block();

                log.info("✅ Gửi text message thành công (attempt {}). recipientId={}", attempt, recipientId);
                return;

            } catch (Exception e) {
                log.warn("⚠️ Gửi message thất bại (attempt {}): {}", attempt, e.getMessage());
                if (attempt < 3) {
                    try {
                        Thread.sleep(1000L * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("❌ Thread interrupted during retry sleep");
                        return;
                    }
                } else {
                    log.error("❌ Failed to send message after 3 attempts to recipient: {}", recipientId);
                }
            }
        }
    }

    /**
     * Send image message to Facebook user
     * Following traloitudongV2 pattern
     */
    public void sendImageToUser(String pageAccessToken, String recipientId, String imageUrl) {
        String url = graphApiUrl + "/v18.0/me/messages?access_token=" + pageAccessToken;

        Map<String, Object> payload = Map.of(
                "messaging_type", "RESPONSE",
                "recipient", Map.of("id", recipientId),
                "message", Map.of(
                        "attachment", Map.of(
                                "type", "image",
                                "payload", Map.of(
                                        "url", imageUrl,
                                        "is_reusable", true
                                )
                        )
                )
        );

        try {
            webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("✅ Gửi image message thành công. recipientId={}", recipientId);

        } catch (Exception e) {
            log.error("❌ Lỗi gửi image message: {}", e.getMessage(), e);
        }
    }

    /**
     * Send message using connection details
     * Following traloitudongV2 pattern
     */
    public void sendMessageToUser(String pageId, String recipientId, String text, String pageAccessToken) {
        log.info("📤 Sending message to Facebook user {} from page {}: {}", recipientId, pageId, text);
        sendTextMessage(pageAccessToken, recipientId, text);
    }

    /**
     * Send complex responses (following traloitudongV2 pattern for Botpress responses)
     * Adapted for PennyBot responses
     */
    @SuppressWarnings("unchecked")
    public void sendPennyBotRepliesToUser(String pageId, String senderId, Map<String, Object> pennyBotResponse, String pageAccessToken) {
        // For PennyBot, we expect simple text response, but structure it for extensibility
        if (pennyBotResponse.containsKey("text")) {
            String text = (String) pennyBotResponse.get("text");
            sendMessageToUser(pageId, senderId, text, pageAccessToken);
        } else if (pennyBotResponse.containsKey("response")) {
            String text = (String) pennyBotResponse.get("response");
            sendMessageToUser(pageId, senderId, text, pageAccessToken);
        } else {
            // Fallback: convert entire response to string
            String text = pennyBotResponse.toString();
            sendMessageToUser(pageId, senderId, text, pageAccessToken);
        }
    }
}
