package com.chatbot.core.message.router.service;

import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.spokes.facebook.messenger.service.FacebookMessengerService;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.service.MessageService;
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

    private final FacebookMessengerService facebookMessengerService;
    private final ConversationRepository conversationRepository;
    private final FacebookConnectionRepository connectionRepository;
    private final MessageService messageService;

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
        String pageAccessToken = connection.getPageAccessToken();

        log.info("🤖 [AgentMsg] Bắt đầu gửi tin nhắn Agent ra Facebook. Page ID: {}", pageId);
        
        // 3. Gửi trực tiếp đến Facebook (traloitudongV2 logic)
        try {
            facebookMessengerService.sendMessageToUser(pageId, recipientId, content, "agent");
            log.info("📤 [AgentMsg] Agent message sent to Facebook user: {}", content);
        } catch (Exception e) {
            log.error("❌ [AgentMsg] Error sending agent message to Facebook: {}", e.getMessage());
        }

        log.info("✅ [AgentMsg] Hoàn tất luồng gửi tin nhắn Agent.");
    }
}