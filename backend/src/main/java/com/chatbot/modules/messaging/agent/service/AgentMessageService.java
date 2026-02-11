package com.chatbot.modules.messaging.agent.service;

import com.chatbot.modules.facebook.connection.model.FacebookConnection;
import com.chatbot.modules.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.modules.facebook.webhook.service.FacebookMessengerService;
import com.chatbot.modules.messaging.messStore.model.Conversation;
import com.chatbot.modules.messaging.messStore.repository.ConversationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * Service xử lý logic gửi tin nhắn đi từ Agent tới người dùng Facebook.
 * Tách biệt khỏi luồng của Bot (Botpress).
 */
@Service
@Slf4j
public class AgentMessageService {

    private final FacebookMessengerService facebookMessengerService;
    private final ConversationRepository conversationRepository;
    private final FacebookConnectionRepository connectionRepository; // Cần thiết để lấy pageId

    public AgentMessageService(
            FacebookMessengerService facebookMessengerService,
            ConversationRepository conversationRepository,
            FacebookConnectionRepository connectionRepository) {
        this.facebookMessengerService = facebookMessengerService;
        this.conversationRepository = conversationRepository;
        this.connectionRepository = connectionRepository;
    }

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
        
        // Tạm thời gọi hàm đã có sẵn trong FacebookMessengerService
        // (Bạn cần đảm bảo hàm này sử dụng sender="agent" khi tin nhắn này được lưu)
        facebookMessengerService.sendMessageToUser(pageId, recipientId, content, "agent");
        
        // LƯU Ý QUAN TRỌNG: Hàm sendMessageToUser hiện tại dùng sender="bot" trong hàm saveBotMessage. 
        // Bạn cần sửa lại như hướng dẫn ở mục 3 để dùng sender="agent" cho tin nhắn này.

        log.info("✅ [AgentMsg] Hoàn tất luồng gửi tin nhắn Agent.");
    }
}