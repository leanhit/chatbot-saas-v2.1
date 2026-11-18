package com.chatbot.core.message.store.dto;

import lombok.Data;

@Data
public class EscalationTierRequest {
    private Integer level;
    private String name;
    private Long timeoutSeconds;
    private Boolean active;
    private String description;
    private String requiredRole;
}
