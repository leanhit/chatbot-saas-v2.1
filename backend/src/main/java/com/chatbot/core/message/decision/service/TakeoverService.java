package com.chatbot.core.message.decision.service;

import com.chatbot.core.message.decision.model.TakeoverMessage;
// !!! Cần Import WebSocket Handler !!!\
import com.chatbot.core.message.decision.websocket.TakeoverWebSocketHandler;
import com.chatbot.core.message.decision.exception.ConversationNotFoundException;
import com.chatbot.core.message.store.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.shared.messenger.ChannelMessengerService;
import java.util.UUID;
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
    private final ChannelMessengerService channelMessengerService;
    private final ConversationRepository conversationRepository;

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
            log.error("❌ Lỗi khi lưu Message vào Redis/gửi WebSocket: {}", e.getMessage(), e);
        }
    }

    private void sendAgentTextMessage(Long conversationDbId, String content, Long agentId) {
        Conversation conversation = conversationRepository.findById(conversationDbId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationDbId));

        log.info("🤖 [AgentMsg] Dispatching agent message to channel. ConnectionId: {}, AgentId: {}", conversation.getConnectionId(), agentId);
        
        try {
            boolean success = channelMessengerService.sendMessage(conversation.getConnectionId(), conversation.getExternalUserId(), content);
            if (success) {
                log.info("📤 [AgentMsg] Agent (ID: {}) sent message to user: {}", agentId, content);
            }
        } catch (Exception e) {
            log.error("❌ [AgentMsg] Error sending agent message: {}", e.getMessage());
        }

        log.info("✅ [AgentMsg] Finished agent message workflow (ID: {}).", agentId);
    }

    /**
     * Centralized method for handling agent messages:
     * 1. Save to database (permanent storage)
     * 2. Save to Redis (for takeover history)
     * 3. Send to Facebook (via AgentMessageService)
     * 4. Send WebSocket (real-time UI updates)
     *
     * @param message         The message to process
     * @param conversationIdLong The conversation ID (Long)
     * @param agentId         ID of the agent sending the message (for audit logging)
     */
    public void saveAndSendAgentMessage(TakeoverMessage message, Long conversationIdLong, Long agentId) {
        try {
            String conversationIdStr = message.getConversationId();
            
            // 1. Lưu message từ agent vào database (lưu dài hạn)
            try {
                
                messageService.saveMessage(
                    conversationIdLong, 
                    "agent", 
                    message.getContent(), 
                    "TEXT", 
                    Map.of("externalMessageId", message.getId()) // Save external message ID for idempotency
                );
                
                log.debug("=== Agent message saved to DB ===");
                log.info("✅ [Takeover] Saved agent message to DB. Conversation ID: {}, AgentId: {}", conversationIdLong, agentId);
            } catch (Exception e) {
                log.error("❌ [Takeover] Error saving agent message to DB: {}", e.getMessage(), e);
            }

            // 2. Lưu vào Redis cho takeover history
            saveMessage(message);
            log.info("💾 [Takeover] Saved agent message to Redis. Conversation ID: {}", conversationIdStr);

            // 3. Gửi tin nhắn đến Facebook user
            try {
                this.sendAgentTextMessage(conversationIdLong, message.getContent(), agentId);
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