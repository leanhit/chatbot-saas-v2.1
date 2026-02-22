package com.chatbot.core.message.router.service;

import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.spokes.facebook.webhook.service.ChatbotServiceWrapper;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * Service xử lý logic gửi tin nhắn đi từ Agent tới người dùng Facebook.
 * Tách biệt khỏi luồng của Bot (Botpress).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgentMessageService {

    private final ChatbotServiceWrapper chatbotServiceWrapper;
    private final ConversationRepository conversationRepository;
    private final FacebookConnectionRepository connectionRepository; // Cần thiết để lấy pageId
    private final FacebookConnectionRepository facebookConnectionRepository;

    /**
     * Gửi tin nhắn TEXT từ Agent tới người dùng và xử lý việc lưu trữ.
     * @param conversationDbId ID Conversation (Long) từ DB.
     * @param content Nội dung tin nhắn.
     * @param agentId ID Agent (hiện tại chưa dùng nhưng cần cho tương lai).
     */
    public void sendAgentTextMessage(Long conversationDbId, String content, Integer agentId) {
        // 1. Tìm Conversation và Connection
        Conversation conversation = conversationRepository.findById(conversationDbId)
                .orElseThrow(() -> new RuntimeException("Conversation not found for ID: " + conversationDbId));

        UUID connectionId = conversation.getConnectionId();
        FacebookConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Connection not found for ID: " + connectionId));

        // 2. Lấy thông tin cần thiết để gửi Facebook
        String pageId = connection.getPageId();
        String recipientId = conversation.getExternalUserId(); // ID người nhận trên Facebook

        log.info("🤖 [AgentMsg] Bắt đầu gửi tin nhắn Agent ra Facebook. Page ID: {}", pageId);
        
        // 3. Gọi hàm gửi tin nhắn (Hàm này đã có logic gửi Facebook VÀ lưu trữ)
        // Lưu ý: Chúng ta phải sử dụng hàm sendMessageToUser/sendImageToUser có sẵn. 
        // Logic saveBotMessage trong FacebookMessengerService cần được tách ra để dùng chung, 
        // nhưng tạm thời, ta vẫn gọi nó vì nó thực hiện cả 2 việc: Gửi và Lưu trữ.
        // Cần chỉnh sửa: Hàm saveBotMessage trong FacebookMessengerService phải được sửa tên
        // thành saveOutgoingMessage và chấp nhận sender là 'agent' hoặc 'bot'.
        
        // Tìm FacebookConnection từ pageId
        FacebookConnection fbConnection = facebookConnectionRepository
                .findByTenantIdAndPageIdAndIsActiveTrue(TenantContext.getTenantId(), pageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Facebook connection"));
        
        // Gửi tin nhắn qua ChatbotServiceWrapper
        chatbotServiceWrapper.sendMessageToUser(fbConnection, recipientId, content);
        
        // LƯU Ý QUAN TRỌNG: Hàm sendMessageToUser hiện tại dùng sender="bot" trong hàm saveBotMessage. 
        // Bạn cần sửa lại như hướng dẫn ở mục 3 để dùng sender="agent" cho tin nhắn này.

        log.info("✅ [AgentMsg] Hoàn tất luồng gửi tin nhắn Agent.");
    }
}