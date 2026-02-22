package com.chatbot.core.config.runtime.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConfigResponse {
    
    private Long id;
    private String configKey;
    private String configValue;
    private String defaultValue;
    private String configType;
    private String configScope;
    private Long tenantId;
    private Long userId;
    private Boolean isEncrypted;
    private Boolean isReadonly;
    private String description;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
