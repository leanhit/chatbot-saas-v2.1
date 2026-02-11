package com.chatbot.modules.facebook.webhook.service;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Service gửi tin nhắn đến Rasa và nhận phản hồi.
 */
@Service
@Slf4j
public class RasaService implements ChatbotProviderService {

    @Value("${rasa.api.url}")
    private String rasaUrl;
    
    @Value("${rasa.api.timeout.seconds:30}")
    private int timeoutInSeconds;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public RasaService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Map<String, Object> sendMessage(String botId, String senderId, String messageText) {
        try {
            String url = String.format("%s/webhooks/rest/webhook", rasaUrl);
            
            // Rasa REST API payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("sender", senderId);
            payload.put("message", messageText);

            log.info("--------------------------------------------------");
            log.info("🚀 Gửi tin nhắn tới Rasa:");
            log.info("   - URL: " + url);
            log.info("   - Sender ID: " + senderId);
            log.info("   - Nội dung tin nhắn: " + messageText);
            log.info("   - Payload: " + objectMapper.writeValueAsString(payload));
            log.info("--------------------------------------------------");

            try {
                Map<String, Object>[] responses = webClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(payload)
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                clientResponse -> clientResponse.bodyToMono(String.class)
                                        .flatMap(errorBody -> {
                                            String errorMsg = "Rasa API Error: " + clientResponse.statusCode() + " - " + errorBody;
                                            log.error("❌ " + errorMsg);
                                            return Mono.error(new RuntimeException(errorMsg));
                                        }))
                        .bodyToMono(Map[].class)
                        .timeout(Duration.ofSeconds(timeoutInSeconds))
                        .doOnError(e -> log.error("❌ Error in Rasa request: " + e.getMessage()))
                        .block();

                if (responses != null && responses.length > 0) {
                    // Convert Rasa response to Botpress-compatible format
                    return convertRasaToBotpressFormat(responses[0]);
                }
                
                log.info("✅ Message successfully forwarded to Rasa");
                return new HashMap<>(); // Empty response if no replies
            } catch (Exception e) {
                String errorMsg = "❌ Failed to send message to Rasa: " + e.getMessage();
                log.error(errorMsg);
                if (e.getCause() != null) {
                    log.error("Caused by: " + e.getCause().getMessage());
                }
                return null;
            }
        } catch (Exception e) {
            log.error("❌ Unexpected error in sendMessage: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map<String, Object> sendEvent(String botId, String senderId, String eventName, Map<String, Object> payload) {
        try {
            String url = String.format("%s/conversations/%s/tracker/events", rasaUrl, senderId);
            
            // Rasa event payload
            Map<String, Object> eventPayload = new HashMap<>();
            eventPayload.put("event", eventName);
            eventPayload.putAll(payload);

            log.info("🚀 Gửi event tới Rasa:");
            log.info(" - URL: " + url);
            log.info(" - Event: " + eventName);
            log.info(" - Payload: " + objectMapper.writeValueAsString(eventPayload));

            webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(eventPayload)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(timeoutInSeconds))
                    .block();
                    
            return new HashMap<>(); // Rasa events don't return responses
        } catch (WebClientResponseException e) {
            log.error("❌ LỖI: " + e.getRawStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("❌ Lỗi khi xử lý JSON: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean healthCheck(String botId) {
        try {
            String url = String.format("%s/status", rasaUrl);
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return response != null && response.contains("\"fingerprint\"");
        } catch (Exception e) {
            log.error("❌ Rasa health check failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderType() {
        return "RASA";
    }

    /**
     * Chuyển đổi response format từ Rasa sang Botpress-compatible format
     */
    private Map<String, Object> convertRasaToBotpressFormat(Map<String, Object> rasaResponse) {
        Map<String, Object> botpressResponse = new HashMap<>();
        
        if (rasaResponse.containsKey("text")) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("text", rasaResponse.get("text"));
            botpressResponse.put("payload", payload);
        }
        
        if (rasaResponse.containsKey("buttons")) {
            // Handle quick replies
            botpressResponse.put("quickReplies", rasaResponse.get("buttons"));
        }
        
        if (rasaResponse.containsKey("image")) {
            // Handle image attachments
            Map<String, Object> attachment = new HashMap<>();
            attachment.put("type", "image");
            attachment.put("url", rasaResponse.get("image"));
            botpressResponse.put("attachments", new Object[]{attachment});
        }
        
        return botpressResponse;
    }
}
