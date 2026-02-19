package com.chatbot.core.message.store.mapper;

import com.chatbot.core.message.store.dto.ConversationDTO;
import com.chatbot.core.message.store.model.Conversation;
import org.springframework.stereotype.Component;

// Thêm @Component để Spring có thể inject được vào Controller
@Component
public class ConversationMapper {

    /**
     * Chuyển đổi đối tượng Conversation (Entity) sang ConversationDTO (Dữ liệu truyền tải cho UI).
     * * Lưu ý: Các trường lastMessageContent và hasUnreadMessages 
     * không thể ánh xạ trực tiếp từ một đối tượng Conversation duy nhất
     * và cần được set ở tầng Service (sau khi truy vấn thêm từ Message và logic).
     */
    public ConversationDTO toDTO(Conversation c) {
        if (c == null) {
            return null;
        }
        ConversationDTO dto = new ConversationDTO();
        
        // Trường cũ
        dto.setId(c.getId());
        dto.setConnectionId(c.getConnectionId());
        dto.setExternalUserId(c.getExternalUserId());
        
        // Thông tin người dùng
        dto.setUserName(c.getUserName());
        dto.setUserAvatar(c.getUserAvatar());
        
        dto.setStatus(c.getStatus());
        
        // THÊM: Ánh xạ ownerId
        dto.setOwnerId(c.getOwnerId());
        
        // Trường mới
        if (c.getChannel() != null) {
            dto.setChannel(c.getChannel().name()); 
        } else {
            dto.setChannel(null); 
        }
        dto.setAgentAssignedId(c.getAgentAssignedId());
        dto.setIsClosedByAgent(c.getIsClosedByAgent());
        dto.setTags(c.getTags()); // Ánh xạ tags dưới dạng String/JSON

        // =========================================================================
        // THÊM TRƯỜNG HANDOVER (QUAN TRỌNG NHẤT CHO UI)
        // =========================================================================
        dto.setIsTakenOverByAgent(c.getIsTakenOverByAgent());
        // =========================================================================

        // Timestamp
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        
        // Các trường lastMessageContent và hasUnreadMessages sẽ được set ở Service Layer
        // vì chúng cần truy vấn dữ liệu từ bảng Message.

        return dto;
    }
}