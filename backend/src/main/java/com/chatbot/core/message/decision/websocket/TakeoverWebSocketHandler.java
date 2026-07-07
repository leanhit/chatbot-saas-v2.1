package com.chatbot.core.message.decision.websocket;

import com.chatbot.core.message.decision.model.TakeoverMessage;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.core.notification.websocket.NotificationWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Component
@RequiredArgsConstructor
@Slf4j
public class TakeoverWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ConversationRepository conversationRepository;
    private final FacebookConnectionRepository facebookConnectionRepository;
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    // Session tracking with metadata like traloitudongV2
    private final ConcurrentMap<String, Set<WebSocketSession>> conversationSessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> sessionToConversationMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LocalDateTime> sessionLastActivity = new ConcurrentHashMap<>();
    
    // Tenant-wide session tracking for broadcasting takeover events
    private final ConcurrentMap<Long, Set<WebSocketSession>> tenantSessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> sessionToTenantMap = new ConcurrentHashMap<>();
    
    // Connection health monitoring with configuration
    private final ScheduledExecutorService heartbeatExecutor = new ScheduledThreadPoolExecutor(1);
    
    @Value("${websocket.health-check.enabled:false}")
    private boolean healthCheckEnabled;
    
    @Value("${websocket.health-check.interval:120000}")
    private long healthCheckInterval;
    
    @Value("${websocket.connection.timeout:300000}")
    private long connectionTimeoutMs;
    
    private static final int MAX_CONNECTIONS_PER_CONVERSATION = 10;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        
        log.info("🔍 [WebSocket] Received message from session {}: {}", session.getId(), payload);
        
        // Update activity timestamp
        sessionLastActivity.put(session.getId(), LocalDateTime.now());
        
        // Handle heartbeat messages
        if ("ping".equals(payload)) {
            sendHeartbeatResponse(session);
            return;
        }
        
        // Try to parse as JSON first (for heartbeat messages)
        try {
            Map<String, Object> jsonMessage = objectMapper.readValue(payload, Map.class);
            String messageType = (String) jsonMessage.get("type");
            
            if ("HEARTBEAT".equals(messageType)) {
                // Respond to heartbeat
                Map<String, Object> pongResponse = Map.of(
                    "type", "HEARTBEAT_PONG",
                    "timestamp", System.currentTimeMillis()
                );
                String pongJson = objectMapper.writeValueAsString(pongResponse);
                session.sendMessage(new TextMessage(pongJson));
                log.info("💓 [WebSocket] Sent PONG to session {}", session.getId());
                return;
            }
            
            // Ignore other JSON messages for now
            log.info("📨 [WebSocket] Received JSON message type: {}", messageType);
            return;
            
        } catch (Exception e) {
            // Not JSON, treat as plain text conversation ID. This is expected behavior.
            log.debug("ℹ️ Received non-JSON message, treating as plain text conversation ID");
        }
        
        // Payload sẽ là conversationId mà Agent muốn xem
        if (payload != null && !payload.trim().isEmpty()) {
            // Đây là một lệnh để Agent thông báo họ đang xem conversationId nào
            String newConversationId = payload.trim();
            
            // Validate conversation exists and belongs to the agent's tenant
            try {
                Long conversationIdLong = Long.parseLong(newConversationId);
                var conversationOpt = conversationRepository.findById(conversationIdLong);
                
                if (conversationOpt.isEmpty()) {
                    sendErrorMessage(session, "Conversation not found: " + newConversationId);
                    return;
                }
                
                Long sessionTenantId = (Long) session.getAttributes().get("tenantId");
                if (sessionTenantId == null || !sessionTenantId.equals(conversationOpt.get().getTenantId())) {
                    log.warn("🚨 [SECURITY] IDOR attempt detected! Session {} (Tenant: {}) tried to access Conversation {} (Tenant: {})", 
                        session.getId(), sessionTenantId, newConversationId, conversationOpt.get().getTenantId());
                    sendErrorMessage(session, "Unauthorized access to conversation");
                    session.close(org.springframework.web.socket.CloseStatus.POLICY_VIOLATION);
                    return;
                }
                
            } catch (NumberFormatException e) {
                log.error("❌ [WebSocket] Invalid conversation ID format from session {}: '{}'", session.getId(), payload);
                sendErrorMessage(session, "Invalid conversation ID format");
                return;
            }
            
            // Check connection limits
            Set<WebSocketSession> existingSessions = conversationSessions.get(newConversationId);
            if (existingSessions != null && existingSessions.size() >= MAX_CONNECTIONS_PER_CONVERSATION) {
                if (!existingSessions.contains(session)) {
                    sendErrorMessage(session, "Maximum connections reached for conversation: " + newConversationId);
                    return;
                }
            }
            
            // 1. Dọn dẹp session khỏi conversation cũ (nếu có)
            String oldConversationId = sessionToConversationMap.get(session.getId());
            if (oldConversationId != null && !oldConversationId.equals(newConversationId)) {
                Set<WebSocketSession> oldSessions = conversationSessions.get(oldConversationId);
                if (oldSessions != null) {
                    oldSessions.remove(session);
                    if (oldSessions.isEmpty()) {
                        conversationSessions.remove(oldConversationId);
                    }
                }
            }
            
            // 2. Thêm session vào conversationSessions mới
            conversationSessions.computeIfAbsent(newConversationId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                                .add(session);
            
            // 3. Cập nhật ánh xạ ngược
            sessionToConversationMap.put(session.getId(), newConversationId);
            log.info("✅ WebSocket: Session {} đang theo dõi Conversation {} (Total sessions: {})", 
                session.getId(), newConversationId, conversationSessions.values().stream().mapToInt(Set::size).sum());
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Initialize session tracking
        sessionLastActivity.put(session.getId(), LocalDateTime.now());
        
        // Track tenant session for broadcast
        Long tenantId = (Long) session.getAttributes().get("tenantId");
        if (tenantId != null) {
            tenantSessions.computeIfAbsent(tenantId, k -> ConcurrentHashMap.newKeySet()).add(session);
            sessionToTenantMap.put(session.getId(), tenantId);
            log.info("🔗 WebSocket: New connection established - Session: {}, Tenant: {}", session.getId(), tenantId);
        } else {
            log.warn("⚠️ WebSocket: Connection established without tenantId - Session: {}", session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        // 1. Tìm conversationId mà session này đang theo dõi
        String conversationId = sessionToConversationMap.remove(session.getId());
        
        // 2. Xóa session khỏi tập hợp của conversation đó
        if (conversationId != null) {
            Set<WebSocketSession> sessions = conversationSessions.get(conversationId);
            if (sessions != null) {
                sessions.remove(session);
                // 3. Nếu không còn ai xem conversation này, dọn dẹp Map
                if (sessions.isEmpty()) {
                    conversationSessions.remove(conversationId);
                }
            }
            log.info("❌ WebSocket: Session {} đã ngắt kết nối. Xóa khỏi Conversation {}. Status: {}", 
                session.getId(), conversationId, status);
        }
        
        // 4. Clean up tenant session tracking
        Long tenantId = sessionToTenantMap.remove(session.getId());
        if (tenantId != null) {
            Set<WebSocketSession> tenantSessionSet = tenantSessions.get(tenantId);
            if (tenantSessionSet != null) {
                tenantSessionSet.remove(session);
                if (tenantSessionSet.isEmpty()) {
                    tenantSessions.remove(tenantId);
                }
            }
        }
        
        // 5. Clean up activity tracking
        sessionLastActivity.remove(session.getId());
    }

    // Helper methods for enhanced WebSocket functionality
    private void sendHeartbeatResponse(WebSocketSession session) {
        try {
            Map<String, Object> heartbeatResponse = Map.of(
                "type", "HEARTBEAT_RESPONSE",
                "timestamp", System.currentTimeMillis(),
                "status", "active"
            );
            String payload = objectMapper.writeValueAsString(heartbeatResponse);
            session.sendMessage(new TextMessage(payload));
            log.debug("💓 WebSocket: Heartbeat response sent to session {}", session.getId());
        } catch (Exception e) {
            log.error("❌ WebSocket: Failed to send heartbeat response: {}", e.getMessage());
        }
    }

    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            Map<String, Object> errorResponse = Map.of(
                "type", "ERROR",
                "message", errorMessage,
                "timestamp", System.currentTimeMillis()
            );
            String payload = objectMapper.writeValueAsString(errorResponse);
            session.sendMessage(new TextMessage(payload));
            log.warn("⚠️ WebSocket: Error message sent to session {}: {}", session.getId(), errorMessage);
        } catch (Exception e) {
            log.error("❌ WebSocket: Failed to send error message: {}", e.getMessage());
        }
    }

    // Scheduled heartbeat and cleanup tasks - CONDITIONAL EXECUTION
    @Scheduled(fixedRateString = "${websocket.health-check.interval:120000}")
    public void performConnectionHealthCheck() {
        // Skip if health check is disabled
        if (!healthCheckEnabled) {
            log.debug("🔧 WebSocket health check is disabled");
            return;
        }
        
        try {
            log.debug("🔍 Performing WebSocket health check (interval: {}ms)", healthCheckInterval);
            
            // Remove inactive sessions
            LocalDateTime cutoff = LocalDateTime.now().minus(connectionTimeoutMs, ChronoUnit.MILLIS);
            
            sessionLastActivity.entrySet().removeIf(entry -> {
                if (entry.getValue().isBefore(cutoff)) {
                    String sessionId = entry.getKey();
                    String conversationId = sessionToConversationMap.get(sessionId);
                    
                    // Remove session from conversation
                    if (conversationId != null) {
                        Set<WebSocketSession> sessions = conversationSessions.get(conversationId);
                        if (sessions != null) {
                            sessions.removeIf(s -> s.getId().equals(sessionId));
                            if (sessions.isEmpty()) {
                                conversationSessions.remove(conversationId);
                            }
                        }
                    }
                    
                    sessionToConversationMap.remove(sessionId);
                    log.info("🧹 WebSocket: Cleaned up inactive session {} from conversation {}", sessionId, conversationId);
                    return true;
                }
                return false;
            });

            // Send heartbeat ONLY to sessions that haven't received messages recently
            // This reduces network overhead significantly
            Map<String, Object> heartbeatMessage = Map.of(
                "type", "HEARTBEAT",
                "timestamp", System.currentTimeMillis()
            );
            String heartbeatPayload = objectMapper.writeValueAsString(heartbeatMessage);
            TextMessage heartbeatTextMessage = new TextMessage(heartbeatPayload);
            
            int activeSessions = 0;
            int heartbeatSent = 0;
            for (Set<WebSocketSession> sessions : conversationSessions.values()) {
                for (WebSocketSession session : sessions) {
                    if (session.isOpen()) {
                        activeSessions++;
                        
                        // Only send heartbeat if session hasn't been active recently
                        LocalDateTime lastActivity = sessionLastActivity.get(session.getId());
                        if (lastActivity != null && lastActivity.isBefore(LocalDateTime.now().minus(healthCheckInterval / 2, ChronoUnit.MILLIS))) {
                            try {
                                session.sendMessage(heartbeatTextMessage);
                                heartbeatSent++;
                            } catch (Exception e) {
                                log.warn("⚠️ WebSocket: Failed to send heartbeat to session {}: {}", session.getId(), e.getMessage());
                            }
                        }
                    }
                }
            }
            
            if (heartbeatSent > 0) {
                log.debug("💓 WebSocket: Heartbeat sent to {} of {} active sessions", heartbeatSent, activeSessions);
            }
            
        } catch (Exception e) {
            log.error("❌ WebSocket: Error during health check: {}", e.getMessage());
        }
    }

    /**
     * Broadcast message to all sessions in a tenant
     * @param tenantId Tenant ID
     * @param message JSON message to broadcast
     */
    public void broadcastToTenant(Long tenantId, String message) {
        Set<WebSocketSession> sessions = tenantSessions.get(tenantId);
        if (sessions == null || sessions.isEmpty()) {
            log.debug("⚠️ WebSocket: No sessions for tenant {}", tenantId);
            return;
        }

        try {
            TextMessage textMessage = new TextMessage(message);
            // Snapshot the set to avoid ConcurrentModificationException if a session
            // disconnects while we are iterating (race condition fix)
            Set<WebSocketSession> snapshot = new java.util.HashSet<>(sessions);
            int sent = 0;
            for (WebSocketSession session : snapshot) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                        sent++;
                    } catch (Exception ex) {
                        log.warn("⚠️ WebSocket: Failed to send to session {} in tenant {}: {}",
                            session.getId(), tenantId, ex.getMessage());
                    }
                }
            }
            log.info("📡 WebSocket: Broadcasted message to {}/{} sessions in tenant {}", sent, snapshot.size(), tenantId);
        } catch (Exception e) {
            log.error("❌ WebSocket: Failed to broadcast to tenant {}: {}", tenantId, e.getMessage());
        }
    }

    /**
     * Gửi tin nhắn đến tất cả các Agent đang xem cuộc hội thoại cụ thể này.
     * Đây là hàm sẽ được gọi từ các service khác (như FacebookMessengerService, FacebookWebhookService).
     */
    public void sendToConversation(String conversationId, TakeoverMessage message) {
        // Broadcast new message notification to the entire tenant via Notification WebSocket
        try {
            Long conversationIdLong = Long.parseLong(conversationId);
            conversationRepository.findById(conversationIdLong).ifPresent(conversation -> {
                Long tenantId = conversation.getTenantId();
                if (tenantId != null && notificationWebSocketHandler != null) {
                    Map<String, Object> wsNotification = Map.of(
                        "type", "CONVERSATION_MESSAGE",
                        "data", Map.of(
                            "id", message.getId(),
                            "conversationId", conversationId,
                            "sender", message.getSender(),
                            "message", message.getContent(),
                            "timestamp", message.getTimestamp()
                        )
                    );
                    notificationWebSocketHandler.broadcastToTenant(tenantId, wsNotification);
                    log.info("📢 Broadcasted CONVERSATION_MESSAGE to notification WS for tenant: {}", tenantId);
                }
            });
        } catch (Exception e) {
            log.error("Failed to broadcast conversation message notification to tenant", e);
        }

        Set<WebSocketSession> sessions = conversationSessions.get(conversationId);
        if (sessions == null || sessions.isEmpty()) {
            log.info("⚠️ WebSocket: Không có Agent nào đang xem Conversation " + conversationId);
            // NOTE: Messages are saved centrally in TakeoverService.saveMessage()
            // No need to save here to avoid duplicates
            return;
        }

        try {
            // Tạo message format phù hợp cho frontend
            Map<String, Object> websocketMessage = Map.of(
                "type", "CONVERSATION_MESSAGE",
                "data", Map.of(
                    "id", message.getId(),
                    "conversationId", conversationId,
                    "sender", message.getSender(),
                    "message", message.getContent(),
                    "timestamp", message.getTimestamp()
                )
            );
            
            String payload = objectMapper.writeValueAsString(websocketMessage);
            TextMessage textMessage = new TextMessage(payload);
            
            log.info("🔍 [DEBUG] Sending WebSocket message - ID: {}, Sender: {}, Content: {}, Sessions: {}", 
                message.getId(), message.getSender(), message.getContent(), sessions.size());
            
            // Snapshot the set to avoid ConcurrentModificationException if a session
            // disconnects while we are iterating (race condition fix)
            Set<WebSocketSession> snapshot = new java.util.HashSet<>(sessions);
            int sent = 0;
            for (WebSocketSession session : snapshot) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                        sent++;
                        log.debug("🔍 [DEBUG] Sent to session: {}", session.getId());
                    } catch (Exception ex) {
                        log.warn("⚠️ WebSocket: Failed to send to session {} for conversation {}: {}",
                            session.getId(), conversationId, ex.getMessage());
                    }
                }
            }
            log.info("✉️ WebSocket: Đã gửi tin nhắn đến {}/{} Agent xem Conversation {}", sent, snapshot.size(), conversationId);
            
            // NOTE: Messages are now saved centrally in TakeoverService
            // WebSocket handler only broadcasts messages, no longer saves to database
            // This prevents duplicate saves since TakeoverService already handles persistence

        } catch (Exception e) {
            log.error("❌ WebSocket: Error sending message to conversation {}: {}", conversationId, e.getMessage());
            e.printStackTrace();
        }
    }
    
    // NOTE: saveMessageToDatabase method removed since messages are now saved centrally in TakeoverService
    // This prevents duplicate saves and centralizes message persistence logic
}