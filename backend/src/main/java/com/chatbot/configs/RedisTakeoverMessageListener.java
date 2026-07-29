package com.chatbot.configs;

import com.chatbot.core.message.decision.websocket.TakeoverWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
            
            // Broadcast to local WebSocket sessions
            // The message format should contain tenantId or conversationId
            var messageMap = objectMapper.readValue(messageJson, java.util.Map.class);
            
            Long tenantId = ((Number) messageMap.get("tenantId")).longValue();
            takeoverWebSocketHandler.broadcastToTenant(tenantId, messageJson);
            
            log.debug("✅ [Redis Pub/Sub] Broadcasted takeover event to tenant {}", tenantId);
        } catch (Exception e) {
            log.error("❌ [Redis Pub/Sub] Error handling takeover message: {}", e.getMessage(), e);
        }
    }
}
