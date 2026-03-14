package com.chatbot.core.message.decision.websocket;

import com.chatbot.core.message.decision.model.TakeoverMessage;
import com.chatbot.core.message.store.service.MessageService;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Component
@RequiredArgsConstructor
@Slf4j
public class TakeoverWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final ConversationRepository conversationRepository;
    private final FacebookConnectionRepository facebookConnectionRepository;
    private final StringRedisTemplate redisTemplate;

    // Session tracking with metadata like traloitudongV2
    private final ConcurrentMap<String, Set<WebSocketSession>> conversationSessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> sessionToConversationMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LocalDateTime> sessionLastActivity = new ConcurrentHashMap<>();
    
    // Connection health monitoring
    private final ScheduledExecutorService heartbeatExecutor = new ScheduledThreadPoolExecutor(1);
    private static final long CONNECTION_TIMEOUT_MS = 300000; // 5 minutes
    private static final long HEARTBEAT_INTERVAL_MS = 30000; // 30 seconds
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
            // Not JSON, treat as plain text conversation ID
        }
        
        // Payload sẽ là conversationId mà Agent muốn xem
        if (payload != null && !payload.trim().isEmpty()) {
            // Đây là một lệnh để Agent thông báo họ đang xem conversationId nào
            String newConversationId = payload.trim();
            
            // Validate conversation exists
            try {
                Long conversationIdLong = Long.parseLong(newConversationId);
                if (!conversationRepository.existsById(conversationIdLong)) {
                    sendErrorMessage(session, "Conversation not found: " + newConversationId);
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
        log.info("🔗 WebSocket: New connection established - Session: {}", session.getId());
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
        
        // 4. Clean up activity tracking
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

    // Scheduled heartbeat and cleanup tasks
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void performConnectionHealthCheck() {
        try {
            // Remove inactive sessions
            LocalDateTime cutoff = LocalDateTime.now().minus(CONNECTION_TIMEOUT_MS, ChronoUnit.MILLIS);
            
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

            // Send heartbeat to active sessions
            Map<String, Object> heartbeatMessage = Map.of(
                "type", "HEARTBEAT",
                "timestamp", System.currentTimeMillis()
            );
            String heartbeatPayload = objectMapper.writeValueAsString(heartbeatMessage);
            TextMessage heartbeatTextMessage = new TextMessage(heartbeatPayload);
            
            int activeSessions = 0;
            for (Set<WebSocketSession> sessions : conversationSessions.values()) {
                for (WebSocketSession session : sessions) {
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(heartbeatTextMessage);
                            activeSessions++;
                        } catch (Exception e) {
                            log.warn("⚠️ WebSocket: Failed to send heartbeat to session {}: {}", session.getId(), e.getMessage());
                        }
                    }
                }
            }
            
            log.debug("💓 WebSocket: Heartbeat sent to {} active sessions", activeSessions);
            
        } catch (Exception e) {
            log.error("❌ WebSocket: Error during health check: {}", e.getMessage());
        }
    }

    /**
     * Gửi tin nhắn đến tất cả các Agent đang xem cuộc hội thoại cụ thể này.
     * Đây là hàm sẽ được gọi từ các service khác (như FacebookMessengerService, FacebookWebhookService).
     */
    public void sendToConversation(String conversationId, TakeoverMessage message) {
        Set<WebSocketSession> sessions = conversationSessions.get(conversationId);
        if (sessions == null || sessions.isEmpty()) {
            log.info("⚠️ WebSocket: Không có Agent nào đang xem Conversation " + conversationId);
            
            // Still save to Redis for history when no agents are watching
            try {
                String json = objectMapper.writeValueAsString(message);
                ListOperations<String, String> ops = redisTemplate.opsForList();
                ops.rightPush("takeover:" + conversationId, json);
                ops.trim("takeover:" + conversationId, -100, -1); // Keep last 100 messages
                redisTemplate.expire("takeover:" + conversationId, 24, TimeUnit.HOURS);
                log.info("💾 Saved message to Redis for history (no active agents). Conversation: " + conversationId);
            } catch (Exception e) {
                log.error("❌ Failed to save message to Redis: {}", e.getMessage());
            }
            return;
        }

        try {
            // Tạo message format phù hợp cho frontend
            Map<String, Object> websocketMessage = Map.of(
                "type", "CONVERSATION_MESSAGE",
                "data", Map.of(
                    "conversationId", conversationId,
                    "sender", message.getSender(),
                    "message", message.getContent(),
                    "timestamp", message.getTimestamp()
                )
            );
            
            String payload = objectMapper.writeValueAsString(websocketMessage);
            TextMessage textMessage = new TextMessage(payload);
            
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
            log.info("✉️ WebSocket: Đã gửi tin nhắn đến " + sessions.size() + " Agent xem Conversation " + conversationId);
            
            // Save ALL messages to database (user, bot, agent) - but avoid duplicates
            // Messages from webhook/bot are already saved in their respective services
            // Only save messages that originate from WebSocket (Agent UI) to avoid duplicates
            if (message.getContent() != null && "agent".equals(message.getSender())) {
                saveMessageToDatabase(conversationId, message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Save agent messages from WebSocket to database
     * User/Bot messages are already saved in their respective services (webhook, bot processing)
     * Only WebSocket-originated agent messages need to be saved here to avoid duplicates
     */
    private void saveMessageToDatabase(String conversationId, TakeoverMessage message) {
        try {
            Long conversationIdLong = Long.parseLong(conversationId);
            
            // Save agent message with proper metadata
            messageService.saveMessage(
                conversationIdLong,
                message.getSender(), // "agent"
                message.getContent(),
                "TEXT",
                Map.of(
                    "source", "agent_websocket",
                    "sender", message.getSender(),
                    "timestamp", message.getTimestamp()
                )
            );
            
            log.info("💾 [WebSocket] Saved {} message to database. Conversation: {}, Sender: {}", 
                message.getSender(), conversationId, message.getSender());
                
        } catch (Exception e) {
            log.error("❌ [WebSocket] Error saving {} message to database: {}", message.getSender(), e.getMessage());
        }
    }
}