package com.chatbot.configs;

import com.chatbot.core.presence.websocket.PresenceWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Redis message listener for presence WebSocket events.
 * Receives messages from Redis Pub/Sub and broadcasts them to local WebSocket sessions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisPresenceMessageListener {

    private final PresenceWebSocketHandler presenceWebSocketHandler;
    private final ObjectMapper objectMapper;

    public void handleMessage(String messageJson) {
        try {
            log.debug("📡 [Redis Pub/Sub] Received presence event: {}", messageJson);

            var messageMap = objectMapper.readValue(messageJson, java.util.Map.class);
            Object tenantIdObj = messageMap.get("tenantId");
            if (tenantIdObj == null) {
                log.warn("⚠️ [Redis Pub/Sub] Missing tenantId in presence message: {}", messageJson);
                return;
            }

            Long tenantId = ((Number) tenantIdObj).longValue();
            presenceWebSocketHandler.broadcastToTenantLocal(tenantId, messageMap);

            log.debug("✅ [Redis Pub/Sub] Broadcasted presence event to tenant {}", tenantId);
        } catch (Exception e) {
            log.error("❌ [Redis Pub/Sub] Error handling presence message: {}", e.getMessage(), e);
        }
    }
}
