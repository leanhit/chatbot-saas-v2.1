package com.chatbot.modules.messaging.messStore.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {

    private Long id;
    private Long conversationId;
    private String sender;
    private String content;
    private String rawPayload;
    
    // Thêm các trường mới từ entity Message
    private String messageType;
    private String externalMessageId;
    private Boolean isRead;
    private LocalDateTime sentTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;
    
    private boolean isMine; // UI logic: true nếu bot gửi
    private Long tenantId;
}