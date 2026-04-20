package com.chatbot.core.message.decision.service;

import com.chatbot.core.message.decision.model.TakeoverMessage;
// !!! Cần Import WebSocket Handler !!!
import com.chatbot.core.message.decision.websocket.TakeoverWebSocketHandler; 
import com.chatbot.core.message.store.service.MessageService;
import com.chatbot.core.message.router.service.AgentMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TakeoverService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    // 1. INJECT WEBSOCKET HANDLER
    private final TakeoverWebSocketHandler websocketHandler;
    private final MessageService messageService;
    private final AgentMessageService agentMessageService; 

    private final long MESSAGE_TTL_HOURS = 24;
    private final long MAX_MESSAGE_COUNT = 100; // Giới hạn 100 tin nhắn lịch sử

    private String key(String conversationId) {
        return "takeover:" + conversationId;
    }

    public void saveMessage(TakeoverMessage message) {
        String conversationId = message.getConversationId();
        String redisKey = key(conversationId);
        
        try {
            String json = objectMapper.writeValueAsString(message);
            ListOperations<String, String> ops = redisTemplate.opsForList();
            
            // 1. Lưu tin nhắn vào cuối danh sách
            ops.rightPush(redisKey, json);
            
            // 2. Giới hạn danh sách (TRIM) chỉ giữ lại 100 tin nhắn gần nhất
            // Giữ lại từ index -MAX_MESSAGE_COUNT đến -1 (100 phần tử cuối)
            ops.trim(redisKey, -MAX_MESSAGE_COUNT, -1); 
            
            // 3. Đặt thời gian hết hạn (Expire)
            redisTemplate.expire(redisKey, MESSAGE_TTL_HOURS, TimeUnit.HOURS);

            // Note: WebSocket sending is handled by the caller to avoid duplicates
            // sendToConversation(message); // Removed to prevent duplicate messages 
            
        } catch (Exception e) {
            log.error("❌ Lỗi khi lưu Message vào Redis/gửi WebSocket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Centralized method for handling agent messages:
     * 1. Save to database (permanent storage)
     * 2. Save to Redis (for takeover history)
     * 3. Send to Facebook (via AgentMessageService)
     * 4. Send WebSocket (real-time UI updates)
     */
    public void saveAndSendAgentMessage(TakeoverMessage message, Long conversationIdLong) {
        try {
            String conversationIdStr = message.getConversationId();
            
            // 1. L message t agent vào database (l u dài)
            try {
                System.out.println("=== DEBUG TAKEOVER SERVICE SAVING AGENT MESSAGE ===");
                System.out.println("Message ID: " + message.getId());
                System.out.println("Conversation ID: " + conversationIdLong);
                System.out.println("Content: " + message.getContent());
                System.out.println("External Message ID: " + message.getId());
                
                messageService.saveMessage(
                    conversationIdLong, 
                    "agent", 
                    message.getContent(), 
                    "TEXT", 
                    Map.of("externalMessageId", message.getId()) // Save external message ID for idempotency
                );
                
                System.out.println("=== TAKEOVER SERVICE: Agent message SAVED to DB ===");
                log.info(" [Takeover] Saved agent message to DB. Conversation ID: {}", conversationIdLong);
            } catch (Exception e) {
                log.error("❌ [Takeover] Error saving agent message to DB: {}", e.getMessage(), e);
            }

            // 2. Lưu vào Redis cho takeover history
            saveMessage(message);
            log.info("💾 [Takeover] Saved agent message to Redis. Conversation ID: {}", conversationIdStr);

            // 3. Gửi tin nhắn đến Facebook user
            try {
                agentMessageService.sendAgentTextMessage(
                    conversationIdLong, 
                    message.getContent(), 
                    null // agentId đang là null, có thể cần lấy từ context sau này
                );
                log.info("📤 [Takeover] Agent message sent to Facebook. Conversation ID: {}", conversationIdLong);
            } catch (Exception e) {
                log.error("❌ [Takeover] Error sending agent message to Facebook: {}", e.getMessage(), e);
            }

            // 4. Gửi tin nhắn qua WebSocket (real-time UI)
            sendToConversation(message);
            log.info("📡 [Takeover] Agent message sent via WebSocket. Conversation ID: {}", conversationIdStr);
            
        } catch (Exception e) {
            log.error("❌ [Takeover] Error in saveAndSendAgentMessage: {}", e.getMessage(), e);
        }
    }

    public List<String> getMessages(String conversationId) {
        ListOperations<String, String> ops = redisTemplate.opsForList();
        // Lấy tất cả tin nhắn (0 đến -1), sẽ không quá MAX_MESSAGE_COUNT do hàm saveMessage có trim
        return ops.range(key(conversationId), 0, -1);
    }
    
    /**
     * Phương thức mới được thêm vào để hỗ trợ gửi tin nhắn qua WebSocket.
     * Phương thức này giải quyết lỗi "cannot find symbol" trong FacebookWebhookService.
     */
    public void sendToConversation(TakeoverMessage message) {
        // Gọi handler thực tế để gửi tin nhắn
        websocketHandler.sendToConversation(message.getConversationId(), message);
    }
}