package com.chatbot.modules.billing.core.dto;

import lombok.*;

import java.util.UUID;

/**
 * Entitlement response DTO for v0.1
 * App entitlement check result
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntitlementResponse {

    private UUID tenantId;
    private String appCode;
    private boolean allowed;
    private String reason;
}
