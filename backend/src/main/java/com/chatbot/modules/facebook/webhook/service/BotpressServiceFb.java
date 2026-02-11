package com.chatbot.modules.facebook.webhook.service;

import com.chatbot.modules.facebook.webhook.dto.SimpleBotpressMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.HashMap;
import java.time.Duration;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

/**
 * Service gửi tin nhắn văn bản đến Botpress và nhận phản hồi.
 */
@Service
@Slf4j
public class BotpressServiceFb implements ChatbotProviderService {

    @Value("${botpress.api.url}")
    private String botpressUrl;
    
    @Value("${botpress.api.timeout.seconds:30}")
    private int timeoutInSeconds;
    
    @Value("${botpress.api.retry.attempts:2}")
    private int maxRetryAttempts;
    
    @Value("${botpress.api.retry.delay:500}")
    private long retryDelayMillis;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public BotpressServiceFb(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> sendMessage(String botId, String senderId, String messageText) {
        return sendMessageToBotpress(botId, senderId, messageText);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> sendMessageToBotpress(String botId, String senderId, String messageText) {
        log.info("===>" + botId + "===" + senderId + "===" + messageText);
        try {
            String url = String.format("%s/api/v1/bots/%s/converse/%s", botpressUrl, botId, senderId);
            SimpleBotpressMessage botpressMessage = new SimpleBotpressMessage(messageText);

            log.info("--------------------------------------------------");
            log.info("🚀 Gửi tin nhắn tới Botpress:");
            log.info("   - URL: " + url);
            log.info("   - Sender ID: " + senderId);
            log.info("   - Nội dung tin nhắn: " + messageText);
            log.info("   - Payload: " + objectMapper.writeValueAsString(botpressMessage));
            log.info("   - Timeout: " + timeoutInSeconds + " seconds");
            log.info("   - Max Retries: " + maxRetryAttempts);
            log.info("--------------------------------------------------");

            try {
                // Configure retry with exponential backoff
                RetryBackoffSpec retrySpec = Retry.backoff(maxRetryAttempts, Duration.ofMillis(retryDelayMillis))
                    .filter(throwable -> {
                        // Only retry on timeout or 5xx errors
                        if (throwable instanceof java.util.concurrent.TimeoutException) {
                            return true;
                        }
                        if (throwable instanceof WebClientResponseException) {
                            int statusCode = ((WebClientResponseException) throwable).getRawStatusCode();
                            return statusCode >= 500 && statusCode < 600;
                        }
                        return false;
                    })
                    .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                        log.error("❌ Max retries (" + maxRetryAttempts + ") reached for Botpress API");
                        return new RuntimeException("Botpress API request failed after " + maxRetryAttempts + " retries", retrySignal.failure());
                    });

                Map<String, Object> response = webClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(botpressMessage)
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                clientResponse -> clientResponse.bodyToMono(String.class)
                                        .flatMap(errorBody -> {
                                            String errorMsg = "Botpress API Error: " + clientResponse.statusCode() + " - " + errorBody;
                                            log.error("❌ " + errorMsg);
                                            return Mono.error(new RuntimeException(errorMsg));
                                        }))
                        .bodyToMono(Map.class)
                        .timeout(Duration.ofSeconds(timeoutInSeconds))
                        .retryWhen(retrySpec)
                        .doOnError(e -> log.error("❌ Error in Botpress request: " + e.getMessage()))
                        .block();

                log.info("✅ Message successfully forwarded to Botpress for bot " + botId);
                return response;
            } catch (Exception e) {
                String errorMsg = "❌ Failed to send message to Botpress: " + e.getMessage();
                log.error(errorMsg);
                if (e.getCause() != null) {
                    log.error("Caused by: " + e.getCause().getMessage());
                }
                return null;
            }
        } catch (Exception e) {
            log.error("❌ Unexpected error in sendMessageToBotpress: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Object> sendEvent(String botId, String senderId, String eventName, Map<String, Object> payload) {
        return sendEventToBotpress(botId, senderId, eventName, payload);
    }

    public Map<String, Object> sendEventToBotpress(String botId, String senderId, String eventName, Map<String, Object> customPayload) {
        String url = String.format("%s/api/v1/bots/%s/converse/%s", botpressUrl, botId, senderId);
        
        try {
            String jsonPayload = objectMapper.writeValueAsString(customPayload);
            String finalPayloadText;
            
            // Botpress có giới hạn 360 ký tự cho tin nhắn văn bản
            if (jsonPayload.length() > 360) {
                finalPayloadText = "Tệp đính kèm quá lớn để xử lý.";
            } else {
                finalPayloadText = jsonPayload;
            }

            Map<String, String> simplePayload = new HashMap<>();
            simplePayload.put("text", finalPayloadText);

            log.info("🚀 Gửi event tới Botpress:");
            log.info(" - URL: " + url);
            log.info(" - User: " + senderId);
            log.info(" - Simple Payload: " + simplePayload);

            return webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(simplePayload)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
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
            String url = String.format("%s/api/v1/bots/%s/info", botpressUrl, botId);
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return response != null && !response.isEmpty();
        } catch (Exception e) {
            log.error("❌ Botpress health check failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderType() {
        return "BOTPRESS";
    }

}
