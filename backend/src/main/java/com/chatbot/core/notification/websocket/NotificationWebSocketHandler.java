package com.chatbot.core.notification.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NotificationWebSocketHandler - Manages WebSocket connections for real-time system/tenant notifications.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    // Track sessions by email (since invitations/approvals can be targeted by email)
    private final Map<String, Set<WebSocketSession>> emailSessions = new ConcurrentHashMap<>();
    
    // Track sessions by userId
    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    // Track sessions by tenantId (for tenant join requests, activity, etc.)
    private final Map<Long, Set<WebSocketSession>> tenantSessions = new ConcurrentHashMap<>();

    // Map session ID to metadata for cleanup
    private final Map<String, String> sessionToEmailMap = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionToUserMap = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionToTenantMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String email = (String) session.getAttributes().get("email");
        Long userId = (Long) session.getAttributes().get("userId");
        Long tenantId = (Long) session.getAttributes().get("tenantId");

        if (email == null) {
            log.warn("[Notification WS] Connection missing email attribute. Closing session.");
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        // Register under email
        emailSessions.computeIfAbsent(email, k -> ConcurrentHashMap.newKeySet()).add(session);
        sessionToEmailMap.put(session.getId(), email);

        // Register under userId
        if (userId != null) {
            userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
            sessionToUserMap.put(session.getId(), userId);
        }

        // Register under tenantId if available
        if (tenantId != null) {
            tenantSessions.computeIfAbsent(tenantId, k -> ConcurrentHashMap.newKeySet()).add(session);
            sessionToTenantMap.put(session.getId(), tenantId);
            log.info("[Notification WS] Connected: {} (ID: {}) for tenant {}", email, userId, tenantId);
        } else {
            log.info("[Notification WS] Connected: {} (ID: {}) without tenant context", email, userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String email = sessionToEmailMap.remove(session.getId());
        if (email != null) {
            Set<WebSocketSession> sessions = emailSessions.get(email);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    emailSessions.remove(email);
                }
            }
        }

        Long userId = sessionToUserMap.remove(session.getId());
        if (userId != null) {
            Set<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }

        Long tenantId = sessionToTenantMap.remove(session.getId());
        if (tenantId != null) {
            Set<WebSocketSession> sessions = tenantSessions.get(tenantId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    tenantSessions.remove(tenantId);
                }
            }
        }

        log.info("[Notification WS] Disconnected session of {}. Status: {}", email, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        
        // Handle heartbeat
        if ("ping".equals(payload)) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }

        try {
            Map<?, ?> jsonMessage = objectMapper.readValue(payload, Map.class);
            if ("HEARTBEAT".equals(jsonMessage.get("type"))) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "HEARTBEAT_PONG",
                    "timestamp", System.currentTimeMillis()
                ))));
                return;
            }
        } catch (Exception e) {
            log.trace("ℹ️ [Notification WS] Non-JSON payload received: {}", payload);
        }

        log.debug("📨 [Notification WS] Received payload: {}", payload);
    }

    /**
     * Send notification to a specific email
     */
    public void sendToUser(String email, Object notification) {
        if (email == null) return;
        Set<WebSocketSession> sessions = emailSessions.get(email);
        if (sessions == null || sessions.isEmpty()) {
            log.debug("[Notification WS] No active WebSocket session for user {}", email);
            return;
        }

        sendToSessions(sessions, notification);
    }

    /**
     * Send notification to a specific user ID
     */
    public void sendToUser(Long userId, Object notification) {
        if (userId == null) return;
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            log.debug("[Notification WS] No active WebSocket session for user ID {}", userId);
            return;
        }

        sendToSessions(sessions, notification);
    }

    /**
     * Send notification to a specific user ID with priority
     * Priority levels: urgent, high, medium, low
     */
    @SuppressWarnings("unchecked")
    public void sendToUserWithPriority(Long userId, Object notification, String priority) {
        if (userId == null) return;
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            log.debug("[Notification WS] No active WebSocket session for user ID {}", userId);
            return;
        }

        // Add priority to notification if it's a Map
        if (notification instanceof Map) {
            ((Map<String, Object>) notification).put("priority", priority);
        }

        sendToSessions(sessions, notification);
    }

    /**
     * Send notification to all active sessions in a tenant
     */
    public void broadcastToTenant(Long tenantId, Object notification) {
        if (tenantId == null) return;
        Set<WebSocketSession> sessions = tenantSessions.get(tenantId);
        if (sessions == null || sessions.isEmpty()) {
            log.debug("[Notification WS] No active WebSocket sessions for tenant ID {}", tenantId);
            return;
        }

        sendToSessions(sessions, notification);
    }

    private void sendToSessions(Set<WebSocketSession> sessions, Object notification) {
        try {
            String payload = objectMapper.writeValueAsString(notification);
            TextMessage textMessage = new TextMessage(payload);
            
            // Avoid ConcurrentModificationException by taking a copy of the sessions set
            Set<WebSocketSession> snapshot = new java.util.HashSet<>(sessions);
            int sentCount = 0;
            for (WebSocketSession s : snapshot) {
                if (s.isOpen()) {
                    try {
                        s.sendMessage(textMessage);
                        sentCount++;
                    } catch (IOException e) {
                        log.warn("[Notification WS] Failed to send message to session {}: {}", s.getId(), e.getMessage());
                    }
                }
            }
            log.info("[Notification WS] Broadcasted notification to {}/{} open sessions", sentCount, snapshot.size());
        } catch (Exception e) {
            log.error("[Notification WS] Failed to serialize or send notification: {}", e.getMessage(), e);
        }
    }
}
