package com.chatbot.core.message.store.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import com.chatbot.shared.utils.DateUtils;
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
    
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    private LocalDateTime sentTime;

    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    private LocalDateTime createdAt;
    
    private boolean isMine; // UI logic: true nếu bot gửi
    private Long tenantId;
}