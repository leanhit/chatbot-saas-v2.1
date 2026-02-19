package com.chatbot.core.message.store.dto;

import lombok.Data;

@Data
public class MessageSendRequest {

    private Long conversationId;
    private String sender;  // bot | user
    private String content;
    
    // Thêm trường loại tin nhắn để biết cách xử lý (text là phổ biến nhất)
    private String messageType = "text"; // Mặc định là text

    // Nếu cần gửi các cấu trúc phức tạp (template, button...), trường này sẽ được sử dụng
    private String rawPayload;
}