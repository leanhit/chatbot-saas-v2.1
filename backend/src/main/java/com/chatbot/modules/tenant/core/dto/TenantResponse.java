package com.chatbot.modules.tenant.core.dto;

import com.chatbot.modules.tenant.core.model.TenantStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tenant response DTO for v0.1
 * Simplified tenant information for API responses
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantResponse {

    private UUID id;
    private String name;
    private TenantStatus status;
    private String defaultLocale;
    private LocalDateTime createdAt;
}
