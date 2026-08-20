package com.chatbot.config;

import com.chatbot.core.message.decision.model.TakeoverMessage;
import com.chatbot.core.message.decision.websocket.TakeoverWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Redis message listener for takeover WebSocket events.
 * Receives messages from Redis Pub/Sub and broadcasts them to local WebSocket sessions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisTakeoverMessageListener {

    private final TakeoverWebSocketHandler takeoverWebSocketHandler;
    private final ObjectMapper objectMapper;

    public void handleMessage(String messageJson) {
        try {
            log.debug("📡 [Redis Pub/Sub] Received takeover event: {}", messageJson);

            var messageMap = objectMapper.readValue(messageJson, java.util.Map.class);
            String messageType = (String) messageMap.get("type");

            // Handle conversation-specific messages
            if ("CONVERSATION_MESSAGE".equals(messageType)) {
                String conversationId = (String) messageMap.get("conversationId");
                if (conversationId != null) {
                    Map<String, Object> data = (Map<String, Object>) messageMap.get("data");
                    TakeoverMessage takeoverMessage = new TakeoverMessage(
                        (String) data.get("id"),
                        conversationId,
                        (String) data.get("sender"),
                        (String) data.get("message"),
                        ((Number) data.get("timestamp")).longValue()
                    );
                    takeoverWebSocketHandler.sendToConversationLocal(conversationId, takeoverMessage);
                    log.debug("✅ [Redis Pub/Sub] Broadcasted conversation message to {}", conversationId);
                }
            }
            // Handle tenant-wide broadcasts
            else if (messageMap.containsKey("tenantId")) {
                Long tenantId = ((Number) messageMap.get("tenantId")).longValue();
                takeoverWebSocketHandler.broadcastToTenant(tenantId, messageJson);
                log.debug("✅ [Redis Pub/Sub] Broadcasted takeover event to tenant {}", tenantId);
            } else {
                log.warn("⚠️ [Redis Pub/Sub] Unknown message format: {}", messageJson);
            }
        } catch (Exception e) {
            log.error("❌ [Redis Pub/Sub] Error handling takeover message: {}", e.getMessage(), e);
        }
    }
}
