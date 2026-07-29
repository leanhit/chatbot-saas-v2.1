package com.chatbot.configs;

import com.chatbot.core.notification.websocket.NotificationWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Redis message listener for notification WebSocket events.
 * Receives messages from Redis Pub/Sub and broadcasts them to local WebSocket sessions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisNotificationMessageListener {

    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final ObjectMapper objectMapper;

    public void handleMessage(String messageJson) {
        try {
            log.debug("📡 [Redis Pub/Sub] Received notification event: {}", messageJson);
            
            // Broadcast to local WebSocket sessions
            var messageMap = objectMapper.readValue(messageJson, java.util.Map.class);
            
            Long tenantId = ((Number) messageMap.get("tenantId")).longValue();
            notificationWebSocketHandler.broadcastToTenant(tenantId, messageMap);
            
            log.debug("✅ [Redis Pub/Sub] Broadcasted notification event to tenant {}", tenantId);
        } catch (Exception e) {
            log.error("❌ [Redis Pub/Sub] Error handling notification message: {}", e.getMessage(), e);
        }
    }
}
