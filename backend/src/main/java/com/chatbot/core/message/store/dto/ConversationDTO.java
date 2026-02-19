package com.chatbot.core.message.store.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ConversationDTO {

    private Long id;
    private UUID connectionId;
    private String ownerId;
    private String externalUserId;
    
    // Thông tin người dùng
    private String userName;
    private String userAvatar;
    
    private String status;
    
    // Thêm các trường mới từ entity Conversation
    private String channel;
    private String tags; // JSON string
    private Long lastMessageId;
    private Long agentAssignedId;
    private Boolean isClosedByAgent;
    
    // Trạng thái chuyển giao. True nếu Agent đã tiếp quản, Botpress sẽ bị ngắt.
    private Boolean isTakenOverByAgent;

    // Trường bổ sung để hiển thị tóm tắt tin nhắn cuối (từ lastMessageId)
    private String lastMessageContent;
    private Boolean hasUnreadMessages; // Thay thế cho isRead trên entity, dùng logic tổng hợp
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;
    
    private Long tenantId;
}