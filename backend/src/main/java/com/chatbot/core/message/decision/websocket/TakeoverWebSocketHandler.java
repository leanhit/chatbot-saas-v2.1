package com.chatbot.core.message.decision.websocket;

import com.chatbot.core.message.decision.model.TakeoverMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class TakeoverWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    // Thay thế Set<WebSocketSession> bằng Map để lưu trữ session và conversationId
    // Key: conversationId (String)
    // Value: Set<WebSocketSession> - Tập hợp các session đang xem conversation này
    private final ConcurrentMap<String, Set<WebSocketSession>> conversationSessions = new ConcurrentHashMap<>();

    // Map ngược lại: SessionId -> conversationId. Giúp loại bỏ session khi ngắt kết nối
    private final ConcurrentMap<String, String> sessionToConversationMap = new ConcurrentHashMap<>();

    // Hàm nhận tin nhắn từ Client (Agent)
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        
        // Payload sẽ là conversationId mà Agent muốn xem
        if (payload != null && !payload.trim().isEmpty()) {
            // Đây là một lệnh để Agent thông báo họ đang xem conversationId nào
            String newConversationId = payload.trim();
            
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
            log.info("✅ WebSocket: Session " + session.getId() + " đang theo dõi Conversation " + newConversationId);

        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Không cần làm gì ở đây, việc thêm/xóa sẽ diễn ra trong handleTextMessage
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
            log.info("❌ WebSocket: Session " + session.getId() + " đã ngắt kết nối. Xóa khỏi Conversation " + conversationId);
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
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(payload);
            
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
            log.info("✉️ WebSocket: Đã gửi tin nhắn đến " + sessions.size() + " Agent xem Conversation " + conversationId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}