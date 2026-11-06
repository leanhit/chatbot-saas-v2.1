package com.chatbot.core.customer.dto;

import com.chatbot.core.customer.model.CustomerNote;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CustomerNoteDTO(
    Long id,
    Long customerProfileId,
    String authorId,
    String authorName,
    String content,
    LocalDateTime createdAt
) {
    public static CustomerNoteDTO from(CustomerNote n) {
        return CustomerNoteDTO.builder()
            .id(n.getId())
            .customerProfileId(n.getCustomerProfileId())
            .authorId(n.getAuthorId())
            .authorName(n.getAuthorName())
            .content(n.getContent())
            .createdAt(n.getCreatedAt())
            .build();
    }
}
