package com.chatbot.core.message.store.mapper;

import com.chatbot.core.message.store.dto.MessageDTO;
import com.chatbot.core.message.store.model.Message;

public class MessageMapper {

    /**
     * Chuyển đổi đối tượng Message (Entity) sang MessageDTO.
     * * @param m Đối tượng Message (Entity)
     * @param isMine Logic để xác định tin nhắn hiển thị bên phải (isMine = true) hay bên trái (isMine = false).
     * @return MessageDTO
     */
    public static MessageDTO toDTO(Message m, boolean isMine) {
        MessageDTO dto = new MessageDTO();
        
        // Trường cũ
        dto.setId(m.getId());
        dto.setSender(m.getSender());
        dto.setContent(m.getContent());
        dto.setRawPayload(m.getRawPayload());

        // Trường mới
        dto.setMessageType(m.getMessageType());
        dto.setExternalMessageId(m.getExternalMessageId());
        dto.setSentTime(m.getSentTime()); // Thời gian gửi trên nền tảng gốc

        // Timestamp
        dto.setCreatedAt(m.getCreatedAt());
        
        // Logic UI
        dto.setMine(isMine);
        
        return dto;
    }
}