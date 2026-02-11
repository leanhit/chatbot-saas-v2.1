package com.chatbot.modules.messaging.agent.dto; // Hoặc package bạn đã chọn

import lombok.Data; // Hoặc thêm Getters/Setters thủ công
import java.util.Map;

/**
 * Payload dùng để nhận dữ liệu từ Frontend (Agent UI) khi Agent gửi tin nhắn.
 * Chứa đủ thông tin để Backend xác định Conversation và nội dung tin nhắn.
 */
@Data
public class MessageSendPayload {

    // ID Conversation trong DB (Cần thiết để tìm Page ID và External User ID)
    private Long conversationId; 
    
    // Luôn là 'agent' khi gửi từ luồng này
    // Frontend nên gửi giá trị này: 'agent'
    private String sender; 
    
    // Nội dung tin nhắn (Text, hoặc URL/path cho Image/Attachment)
    private String content; 
    
    // Loại tin nhắn: 'text', 'image', 'attachment',...
    private String messageType; 
    
    // Payload thô (Nếu cần gửi cấu trúc phức tạp như Quick Replies, Cards)
    private Map<String, Object> rawPayload; 

    // Không cần các trường như id, sentTime, createdAt, isMine vì đây là DTO gửi đi
}