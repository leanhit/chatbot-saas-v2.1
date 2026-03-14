package com.chatbot.spokes.botpress.service;

import com.chatbot.spokes.facebook.webhook.service.ChatbotProviderService;
import com.chatbot.core.message.store.service.MessageService;
import com.chatbot.core.message.store.service.ConversationService;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.Channel;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.spokes.facebook.messenger.service.FacebookMessengerService;
import com.chatbot.spokes.facebook.webhook.service.ChatbotProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;
import java.time.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Botpress Service Implementation for Facebook integration
 * Placeholder implementation - to be properly implemented
 */
@Service("botpressServiceFb")
@Primary
@Slf4j
public class BotpressServiceFb implements ChatbotProviderService {

    @Value("${app.integrations.botpress.api-url}")
    private String botpressApiUrl;
    
    @Value("${app.integrations.botpress.admin-token}")
    private String adminToken;
    
    @Value("${app.integrations.botpress.max-retry-attempts:3}")
    private int maxRetryAttempts;

    @Value("${app.integrations.botpress.retry-delay-millis:500}")
    private long retryDelayMillis;

    @Value("${app.integrations.botpress.timeout-in-seconds:10}")
    private int timeoutInSeconds;

    @Autowired
    private MessageService messageService;
    
    @Autowired
    private ConversationService conversationService;
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private FacebookConnectionRepository facebookConnectionRepository;
    
    @Autowired
    private FacebookMessengerService facebookMessengerService;

    @Autowired
    private WebClient.Builder webClientBuilder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> sendMessage(String botId, String senderId, String messageText) {
        // GỌI BOTPRESS API TRỰC TIẾP như phiên bản cũ
        return sendMessageToBotpress(botId, senderId, messageText);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> sendMessageToBotpress(String botId, String senderId, String messageText) {
        log.info("===>" + botId + "===" + senderId + "===" + messageText);
        try {
            String url = String.format("%s/api/v1/bots/%s/converse/%s", botpressApiUrl, botId, senderId);
            
            // Create Botpress message payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "text");
            payload.put("text", messageText);
            payload.put("channel", "web");
            payload.put("target", senderId);

            log.info("--------------------------------------------------");
            log.info("🚀 Gửi tin nhắn tới Botpress:");
            log.info("   - URL: " + url);
            log.info("   - Sender ID: " + senderId);
            log.info("   - Nội dung tin nhắn: " + messageText);
            log.info("   - Payload: " + objectMapper.writeValueAsString(payload));
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

                Map<String, Object> response = webClientBuilder.build().post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(payload)
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

    @Override
    public Map<String, Object> sendEvent(String botId, String senderId, String eventName, Map<String, Object> payload) {
        log.info("Sending event to Botpress - Bot: {}, Sender: {}, Event: {}, Payload: {}", botId, senderId, eventName, payload);
        
        // Placeholder implementation
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Event received by Botpress");
        response.put("botId", botId);
        response.put("senderId", senderId);
        
        return response;
    }

    @Override
    public boolean healthCheck(String botId) {
        log.info("Checking Botpress health for bot: {}", botId);
        // Placeholder implementation
        return true;
    }

    @Override
    public String getProviderType() {
        return "BOTPRESS";
    }
    
    /**
     * Save agent message to database
     */
    private void saveAgentMessageToDatabase(String botId, String senderId, String messageText) {
        try {
            // Get current tenant context
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                log.error("❌ Tenant context not found for saving message");
                return;
            }
            
            // Find Facebook connections by botId và tenantId (1 bot có thể quản lý nhiều pages)
            List<FacebookConnection> connections = facebookConnectionRepository.findAllByBotIdAndTenantIdAndIsActiveTrue(botId, tenantId);
            
            if (connections.isEmpty()) {
                log.error("❌ No active Facebook connections found for botId: {} and tenantId: {}", botId, tenantId);
                return;
            }
            
            // Smart routing: Find the right connection by looking for conversation with this senderId
            FacebookConnection targetConnection = null;
            Conversation conversation = null;
            
            for (FacebookConnection connection : connections) {
                Optional<Conversation> convOpt = conversationRepository.findByExternalUserIdAndConnectionId(senderId, connection.getId());
                if (convOpt.isPresent()) {
                    targetConnection = connection;
                    conversation = convOpt.get();
                    log.info("🎯 Found matching connection {} for senderId {} via conversation lookup", connection.getId(), senderId);
                    break;
                }
            }
            
            // If no matching conversation found, use first connection and create new conversation
            if (targetConnection == null) {
                targetConnection = connections.get(0);
                log.warn("⚠️ No matching conversation found for senderId {}. Using first connection: {}", senderId, targetConnection.getId());
                
                // Create new conversation for this connection
                conversation = conversationService.findOrCreate(targetConnection.getId(), senderId, Channel.FACEBOOK);
                log.info("🆕 Created new conversation {} for senderId {} and connection {}", conversation.getId(), senderId, targetConnection.getId());
            }
            
            log.info("📋 Using Facebook connection: {} for botId: {} (tenant: {})", targetConnection.getId(), botId, tenantId);
            
            // Save agent message
            messageService.saveMessage(
                conversation.getId(),
                "agent",
                messageText,
                "TEXT",
                Map.of("botId", botId, "sentVia", "botpress_agent_ui", "connectionId", targetConnection.getId().toString())
            );
            
            log.info("✅ Saved agent message to database via Botpress. ConversationId: {}, ConnectionId: {}, SenderId: {}, TenantId: {}", 
                conversation.getId(), targetConnection.getId(), senderId, tenantId);
            
        } catch (Exception e) {
            log.error("❌ Failed to save agent message to database via Botpress: {}", e.getMessage(), e);
            // Don't throw exception to avoid blocking message flow
        }
    }
    
    /**
     * Send message to Facebook user
     */
    private void sendMessageToFacebook(String botId, String senderId, String messageText) {
        try {
            // Get current tenant context
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                log.error("❌ Tenant context not found for sending message");
                return;
            }
            
            // Find Facebook connections by botId và tenantId (1 bot có thể quản lý nhiều pages)
            List<FacebookConnection> connections = facebookConnectionRepository.findAllByBotIdAndTenantIdAndIsActiveTrue(botId, tenantId);
            
            if (connections.isEmpty()) {
                log.error("❌ No active Facebook connections found for botId: {} and tenantId: {}", botId, tenantId);
                return;
            }
            
            // Smart routing: Find the right connection by looking for conversation with this senderId
            FacebookConnection targetConnection = null;
            for (FacebookConnection connection : connections) {
                Optional<Conversation> conversation = conversationRepository.findByExternalUserIdAndConnectionId(senderId, connection.getId());
                if (conversation.isPresent()) {
                    targetConnection = connection;
                    log.info("🎯 Found matching connection {} for senderId {} via conversation lookup", connection.getId(), senderId);
                    break;
                }
            }
            
            // If no matching conversation found, use first connection (fallback)
            if (targetConnection == null) {
                targetConnection = connections.get(0);
                log.warn("⚠️ No matching conversation found for senderId {}. Using first connection: {}", senderId, targetConnection.getId());
            }
            
            log.info("📋 Using Facebook connection: {} for botId: {} (tenant: {})", targetConnection.getId(), botId, tenantId);
            
            // Get page access token directly (like traloitudongV2)
            String pageAccessToken = targetConnection.getPageAccessToken();
            
            // Send message via FacebookMessengerService
            facebookMessengerService.sendTextMessage(
                pageAccessToken,
                senderId,
                messageText
            );
            
            log.info("✅ Sent agent message to Facebook via Botpress. PageId: {}, ConnectionId: {}, RecipientId: {}, TenantId: {}", 
                targetConnection.getPageId(), targetConnection.getId(), senderId, tenantId);
            
        } catch (Exception e) {
            log.error("❌ Failed to send agent message to Facebook via Botpress: {}", e.getMessage(), e);
            // Don't throw exception to avoid blocking message flow
        }
    }
}
