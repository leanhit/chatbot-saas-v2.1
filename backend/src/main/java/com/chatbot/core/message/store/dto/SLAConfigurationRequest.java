package com.chatbot.core.message.store.dto;

import lombok.Data;

@Data
public class SLAConfigurationRequest {
    private String customerTier;
    private Long expectedResponseTime;
    private Integer maxBreachCount;
    private Boolean active;
    private String description;
}
