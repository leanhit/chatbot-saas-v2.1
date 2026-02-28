package com.chatbot.core.message.store.service;
import lombok.extern.slf4j.Slf4j;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.Message;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepo;
    private final ConversationRepository conversationRepo; // Cần inject ConversationRepository để cập nhật lastMessageId
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Lưu tin nhắn và cập nhật Conversation tương ứng.
     */
    @Transactional // Đảm bảo việc lưu message và cập nhật conversation là 1 transaction
    public Message saveMessage(Long conversationId, String sender, String content, String messageType, Map<String, Object> raw) {
        // Kiểm tra nếu người gửi là agent, đảm bảo họ đã take over conversation
        if ("agent".equals(sender)) {
            Conversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));
            
            if (!Boolean.TRUE.equals(conversation.getIsTakenOverByAgent())) {
                throw new IllegalStateException("Agent must take over the conversation before sending messages");
            }
        }
        
        String rawJson = null;
        String imageUrl = null;
        
        try {
            if (raw != null) {
                rawJson = objectMapper.writeValueAsString(raw);
            }
            
            // Xử lý riêng cho image messages
            if ("image".equals(messageType) && content != null && content.startsWith("http")) {
                imageUrl = content; // Extract URL from content
                log.info("🖼️ [MessageService] Detected image message with URL: {}", imageUrl);
                
                // Lưu URL vào rawPayload để frontend có thể hiển thị
                Map<String, Object> imageRawPayload = new java.util.HashMap<>();
                imageRawPayload.put("imageUrl", imageUrl);
                imageRawPayload.put("messageType", "image");
                rawJson = objectMapper.writeValueAsString(imageRawPayload);
            }
        } catch (Exception e) {
            log.error("Error converting raw payload to JSON: " + e.getMessage());
            // Có thể throw exception hoặc log lại tùy thuộc vào yêu cầu nghiệp vụ
        }

        // 1. Lưu Message
        Message m = Message.builder()
                .conversationId(conversationId)
                .sender(sender)
                .content(content)
                .rawPayload(rawJson)
                .messageType(messageType) // <--- Trường mới BẮT BUỘC
                .isRead(false) // <--- Trường mới BẮT BUỘC, mặc định là false (cho Agent/Bot)
                .sentTime(LocalDateTime.now()) // Có thể set sentTime ở đây, hoặc lấy từ payload
                .build();
        Message savedMessage = messageRepo.save(m);

        // 2. Cập nhật lastMessageId và updatedAt cho Conversation
        conversationRepo.findById(conversationId)
                .ifPresent(c -> {
                    c.setLastMessageId(savedMessage.getId());
                    // Cập nhật updatedAt để đảm bảo Conversation đó lên đầu danh sách
                    // @UpdateTimestamp sẽ tự động cập nhật updatedAt, nhưng chúng ta gọi save() để kích hoạt
                    conversationRepo.save(c);
                });

        return savedMessage;
    }

    /**
     * Lấy tin nhắn theo ConversationId.
     * Thêm logic đánh dấu tin nhắn là đã đọc (Mark as Read)
     */
    @Transactional
    public Page<Message> getMessages(Long conversationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        Page<Message> messages = messageRepo.findByConversationIdAndTenantIdOrderByCreatedAtDesc(conversationId, tenantId, pageable);

        // Đánh dấu các tin nhắn chưa đọc của user là đã đọc khi Agent xem conversation
        messages.stream()
                .filter(m -> "user".equals(m.getSender()) && !m.getIsRead())
                .forEach(m -> {
                    m.setIsRead(true);
                    messageRepo.save(m);
                });
        
        return messages;
    }

    /**
     * Xóa một message theo ID
     * @param messageId ID của message cần xóa
     * @throws RuntimeException nếu không tìm thấy message
     */
    @Transactional
    public void deleteMessage(Long messageId) {
        if (!messageRepo.existsById(messageId)) {
            throw new RuntimeException("Message not found with id: " + messageId);
        }
        messageRepo.deleteById(messageId);
    }

    /**
     * Xóa nhiều message cùng lúc
     * @param messageIds Danh sách ID của các message cần xóa
     * @return Số lượng message đã xóa
     */
    @Transactional
    public int deleteMessages(Iterable<Long> messageIds) {
        int count = 0;
        for (Long id : messageIds) {
            if (messageRepo.existsById(id)) {
                messageRepo.deleteById(id);
                count++;
            }
        }
        return count;
    }

    /**
     * Xóa tất cả message của một conversation
     * @param conversationId ID của conversation
     * @return Số lượng message đã xóa
     */
    @Transactional
    public int deleteAllMessagesInConversation(Long conversationId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        List<Message> messages = messageRepo.findByConversationIdAndTenantId(conversationId, tenantId);
        int count = messages.size();
        messageRepo.deleteAll(messages);
        return count;
    }
}