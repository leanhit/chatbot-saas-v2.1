package com.chatbot.core.customer.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class CustomerProfileDTO {
    private Long id;
    private String externalId;
    private String name;
    private String email;
    private String phone;
    private String avatarUrl;
    private String status;
    private String tier;
    private LocalDateTime firstContactAt;
    private LocalDateTime lastContactAt;
    private Integer totalConversations;
    private Integer totalMessages;
    private Integer avgResponseTimeSeconds;
    private Double satisfactionScore;
    private Map<String, Object> customAttributes;
    private Map<String, Object> tags;
    private String preferredLanguage;
    private String timezone;
    private Boolean isBlocked;
    private String blockedReason;
    private LocalDateTime blockedAt;
}
