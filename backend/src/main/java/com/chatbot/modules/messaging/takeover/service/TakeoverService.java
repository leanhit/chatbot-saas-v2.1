package com.chatbot.modules.messaging.takeover.service;

import com.chatbot.modules.messaging.takeover.model.TakeoverMessage;
// !!! Cần Import WebSocket Handler !!!
import com.chatbot.modules.messaging.takeover.websocket.TakeoverWebSocketHandler; 
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TakeoverService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    // 1. INJECT WEBSOCKET HANDLER
    private final TakeoverWebSocketHandler websocketHandler; 

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

            // 4. Gửi qua WebSocket cho Agent đang xem
            sendToConversation(message); 
            
        } catch (Exception e) {
            log.error("❌ Lỗi khi lưu Message vào Redis/gửi WebSocket: " + e.getMessage());
            e.printStackTrace();
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