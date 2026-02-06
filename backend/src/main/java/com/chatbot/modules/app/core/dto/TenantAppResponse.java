package com.chatbot.modules.app.core.dto;

import com.chatbot.modules.app.core.model.AppCode;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TenantApp response DTO for v0.1
 * App status information for API responses
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantAppResponse {

    private UUID id;
    private UUID tenantId;
    private AppCode appCode;
    private String appDisplayName;
    private String appDescription;
    private Boolean enabled;
    private LocalDateTime enabledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
