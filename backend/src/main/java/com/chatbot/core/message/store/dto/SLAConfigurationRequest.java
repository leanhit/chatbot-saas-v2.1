package com.chatbot.core.message.store.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SLAConfigurationRequest {
    @NotBlank(message = "customerTier is required")
    private String customerTier;
    private Long expectedResponseTime;
    private Integer maxBreachCount;
    private Boolean active;
    private String description;
}
