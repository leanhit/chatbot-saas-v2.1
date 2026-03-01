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
    private LocalDateTime sentTime;

    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime createdAt;
    
    private boolean isMine; // UI logic: true nếu bot gửi
    private Long tenantId;
}