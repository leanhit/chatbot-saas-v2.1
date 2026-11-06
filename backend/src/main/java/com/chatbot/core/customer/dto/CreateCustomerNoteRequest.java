package com.chatbot.core.customer.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCustomerNoteRequest(
    @NotBlank String content,
    String authorId,
    String authorName
) {
}
