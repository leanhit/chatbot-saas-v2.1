package com.chatbot.core.customer.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TimelineEventDTO(
    String id,
    String type, // conversation | message | feedback | note
    String title,
    String description,
    String badge,
    LocalDateTime timestamp,
    Long referenceId
) {
}
