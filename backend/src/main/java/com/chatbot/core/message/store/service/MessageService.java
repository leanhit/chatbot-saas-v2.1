package com.chatbot.core.message.store.service;
import lombok.extern.slf4j.Slf4j;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.Message;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.message.usage.service.MessageUsageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepo;
    private final ConversationRepository conversationRepo; // Cần inject ConversationRepository để cập nhật lastMessageId
    private final MessageUsageService messageUsageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Lưu tin nhắn và cập nhật Conversation tương ứng.
     */
    @Transactional // Đảm bảo việc lưu message và cập nhật conversation là 1 transaction
    public Message saveMessage(Long conversationId, String sender, String content, String messageType, Map<String, Object> raw) {
        // Validate message limit for user messages only
        if ("user".equals(sender)) {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId != null) {
                try {
                    messageUsageService.validateAndIncrementMessageCount(tenantId);
                    log.debug("✅ Message limit validation passed for tenant: {}", tenantId);
                } catch (MessageUsageService.MessageLimitExceededException e) {
                    log.warn("❌ Message limit exceeded for tenant {}: {}", tenantId, e.getMessage());
                    throw e; // Re-throw to stop message processing
                } catch (Exception e) {
                    // Handle tenant not found or other errors gracefully
                    log.error("❌ Error validating message limit for tenant {}: {}. Skipping validation and allowing message.", 
                        tenantId, e.getMessage());
                    // Continue with message saving even if validation fails
                    // This prevents message loss when tenant configuration is missing
                }
            }
        }
        
        // Kiểm tra nếu người gửi là agent, cho phép gửi message mà không cần takeover
        // Agent có thể gửi message bất cứ lúc nào để hỗ trợ user
        if ("agent".equals(sender)) {
            Conversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));
            
            // Agent có thể gửi message mà không cần takeover
            // Điều này cho phép agent hỗ trợ user mà không cần takeover conversation
            log.info("🤖 [MessageService] Agent sending message to conversation {} without takeover requirement", conversationId);
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

        // Extract externalMessageId from raw Map if present
        String externalMessageId = null;
        if (raw != null && raw.containsKey("externalMessageId")) {
            externalMessageId = (String) raw.get("externalMessageId");
        }
        
        // 1. Lưu Message
        Message m = Message.builder()
                .conversationId(conversationId)
                .sender(sender)
                .content(content)
                .rawPayload(rawJson)
                .messageType(messageType) // <--- Trường mới BẮT BUỘC
                .externalMessageId(externalMessageId) // Set external message ID for idempotency
                .isRead(false) // <--- Trường mới BẮT BUỘC, mặc định là false (cho Agent/Bot)
                .sentTime(LocalDateTime.now()) // Có thể set sentTime ở đây, hoặc lấy từ payload
                .build();
        Message savedMessage = messageRepo.save(m);

        // Increment message usage count in Redis for non-user messages
        if (!"user".equals(sender) && savedMessage.getTenantId() != null) {
            messageUsageService.incrementMessageCount(savedMessage.getTenantId());
        }

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
            throw new com.chatbot.shared.exceptions.BaseException(com.chatbot.shared.exceptions.ErrorCode.TENANT_CONTEXT_MISSING, "Tenant ID not found in context");
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
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            messageUsageService.evictMessageCountCache(tenantId);
        }
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
        if (count > 0) {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId != null) {
                messageUsageService.evictMessageCountCache(tenantId);
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
            throw new com.chatbot.shared.exceptions.BaseException(com.chatbot.shared.exceptions.ErrorCode.TENANT_CONTEXT_MISSING, "Tenant ID not found in context");
        }
        List<Message> messages = messageRepo.findByConversationIdAndTenantId(conversationId, tenantId);
        int count = messages.size();
        messageRepo.deleteAll(messages);
        if (count > 0) {
            messageUsageService.evictMessageCountCache(tenantId);
        }
        return count;
    }

    /**
     * Lấy message theo ID
     * @param messageId ID của message
     * @return Optional<Message>
     */
    @Transactional(readOnly = true)
    public Optional<Message> getMessageById(Long messageId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new com.chatbot.shared.exceptions.BaseException(com.chatbot.shared.exceptions.ErrorCode.TENANT_CONTEXT_MISSING, "Tenant ID not found in context");
        }
        return messageRepo.findByIdAndTenantId(messageId, tenantId);
    }

    /**
     * Cập nhật message
     * @param message Message cần cập nhật
     * @return Message đã được cập nhật
     */
    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
    public Message updateMessage(Message message) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new com.chatbot.shared.exceptions.BaseException(com.chatbot.shared.exceptions.ErrorCode.TENANT_CONTEXT_MISSING, "Tenant ID not found in context");
        }
        
        // Validate message exists và thuộc tenant
        Message existingMessage = messageRepo.findByIdAndTenantId(message.getId(), tenantId)
            .orElseThrow(() -> new RuntimeException("Message not found with id: " + message.getId()));
        
        // Cập nhật các trường cho phép
        existingMessage.setContent(message.getContent());
        existingMessage.setMessageType(message.getMessageType());
        existingMessage.setExternalMessageId(message.getExternalMessageId());
        // Không update sender, conversationId, createdAt để maintain data integrity
        
        return messageRepo.save(existingMessage);
    }
    
    /**
     * Check if message exists by external message ID (for idempotency)
     */
    public boolean messageExists(String externalMessageId) {
        if (externalMessageId == null || externalMessageId.trim().isEmpty()) {
            return false;
        }
        
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.warn("Tenant context not found for messageExists check");
            return false;
        }
        
        return messageRepo.findByExternalMessageIdAndTenantId(externalMessageId, tenantId).isPresent();
    }
}