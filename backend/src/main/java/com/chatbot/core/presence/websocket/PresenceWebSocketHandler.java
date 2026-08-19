package com.chatbot.core.presence.websocket;

import com.chatbot.configs.RedisPubSubConfig;
import com.chatbot.core.presence.service.PresenceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class PresenceWebSocketHandler extends TextWebSocketHandler {

    private final PresenceService presenceService;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    // Track sessions by tenantId for broadcasting
    private final Map<Long, Set<WebSocketSession>> tenantSessions = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionToTenantMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long tenantId = (Long) session.getAttributes().get("tenantId");
        Long userId = (Long) session.getAttributes().get("userId");
        String email = (String) session.getAttributes().get("email");
        String fullName = (String) session.getAttributes().get("fullName");

        if (tenantId == null || userId == null || email == null) {
            log.warn("⚠️ [Presence] WebSocket connection missing required attributes. Closing connection.");
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        // Add session to tenant's session set
        tenantSessions.computeIfAbsent(tenantId, k -> ConcurrentHashMap.newKeySet()).add(session);
        sessionToTenantMap.put(session.getId(), tenantId);

        // Add user to online list in Redis
        presenceService.addOnlineMember(tenantId, userId, email, fullName != null ? fullName : email);

        // Broadcast MEMBER_ONLINE to all sessions in the same tenant
        String onlineMessage = presenceService.createMemberOnlineMessage(tenantId, userId, email, fullName != null ? fullName : email);
        if (onlineMessage != null) {
            broadcastToTenantWithRedis(tenantId, onlineMessage); // Broadcast to all sessions with Redis publish
        }

        log.info("✅ [Presence] User {} (ID: {}) connected for tenant {}. Total sessions: {}", 
            email, userId, tenantId, tenantSessions.getOrDefault(tenantId, Set.of()).size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long tenantId = sessionToTenantMap.remove(session.getId());
        Long userId = (Long) session.getAttributes().get("userId");
        String email = (String) session.getAttributes().get("email");

        if (tenantId != null) {
            // Remove session from tenant's session set
            Set<WebSocketSession> sessions = tenantSessions.get(tenantId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    tenantSessions.remove(tenantId);
                }
            }

            // Remove user from online list in Redis
            if (userId != null) {
                presenceService.removeOnlineMember(tenantId, userId);

                // Broadcast MEMBER_OFFLINE to all remaining sessions in the same tenant
                String offlineMessage = presenceService.createMemberOfflineMessage(tenantId, userId);
                if (offlineMessage != null) {
                    broadcastToTenantWithRedis(tenantId, offlineMessage); // Broadcast to all with Redis publish
                }
            }

            log.info("❌ [Presence] User {} (ID: {}) disconnected from tenant {}. Status: {}", 
                email, userId, tenantId, status);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle heartbeat/ping
        String payload = message.getPayload();
        if ("ping".equals(payload)) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }

        // Could handle other message types here (e.g., status updates)
        log.debug("📨 [Presence] Received message from session {}: {}", session.getId(), payload);
    }

    /**
     * Broadcast message to all sessions in a tenant (local only)
     * @param tenantId Tenant ID
     * @param message JSON message to broadcast
     * @param excludeSession Session to exclude (optional, e.g., the sender)
     */
    private void broadcastToTenant(Long tenantId, String message, WebSocketSession excludeSession) {
        broadcastToTenantLocal(tenantId, message, excludeSession);
    }

    /**
     * Broadcast message to all local sessions in a tenant (no Redis publish)
     * Used by Redis message listener to avoid re-publishing
     * @param tenantId Tenant ID
     * @param message JSON message to broadcast
     * @param excludeSession Session to exclude (optional, e.g., the sender)
     */
    public void broadcastToTenantLocal(Long tenantId, String message, WebSocketSession excludeSession) {
        Set<WebSocketSession> sessions = tenantSessions.get(tenantId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        TextMessage textMessage = new TextMessage(message);
        for (WebSocketSession session : sessions) {
            if (session.isOpen() && (excludeSession == null || !session.getId().equals(excludeSession.getId()))) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.error("❌ [Presence] Failed to send message to session {}: {}", session.getId(), e.getMessage());
                }
            }
        }
    }

    /**
     * Broadcast message to all local sessions in a tenant (no Redis publish)
     * Used by Redis message listener to avoid re-publishing
     * @param tenantId Tenant ID
     * @param messageMap Message as Map to broadcast
     */
    public void broadcastToTenantLocal(Long tenantId, Map<String, Object> messageMap) {
        try {
            String message = objectMapper.writeValueAsString(messageMap);
            broadcastToTenantLocal(tenantId, message, null);
        } catch (Exception e) {
            log.error("❌ [Presence] Error serializing message for local broadcast: {}", e.getMessage());
        }
    }

    /**
     * Broadcast message to all sessions in a tenant with Redis publish for cluster-wide sync
     * @param tenantId Tenant ID
     * @param message JSON message to broadcast
     */
    private void broadcastToTenantWithRedis(Long tenantId, String message) {
        // Publish to Redis for cluster-wide broadcast
        try {
            redisTemplate.convertAndSend(RedisPubSubConfig.WEBSOCKET_PRESENCE_TOPIC, message);
            log.debug("📡 [Redis Pub/Sub] Published presence event for tenant {}", tenantId);
        } catch (Exception e) {
            log.error("❌ [Redis Pub/Sub] Failed to publish presence event: {}", e.getMessage());
        }
        
        // Also broadcast locally
        broadcastToTenantLocal(tenantId, message, null);
    }

    /**
     * Get count of active sessions for a tenant
     */
    public int getActiveSessionCount(Long tenantId) {
        Set<WebSocketSession> sessions = tenantSessions.get(tenantId);
        return sessions != null ? sessions.size() : 0;
    }
}
