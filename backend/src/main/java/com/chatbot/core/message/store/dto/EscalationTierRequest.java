package com.chatbot.core.message.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EscalationTierRequest {
    @NotNull(message = "level is required")
    private Integer level;
    @NotBlank(message = "name is required")
    private String name;
    private Long timeoutSeconds;
    private Boolean active;
    private String description;
    private String requiredRole;
}
