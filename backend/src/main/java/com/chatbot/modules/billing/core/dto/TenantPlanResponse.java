package com.chatbot.modules.billing.core.dto;

import com.chatbot.modules.billing.core.model.PlanCode;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tenant plan response DTO for v0.1
 * Plan information for API responses
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantPlanResponse {

    private UUID id;
    private UUID tenantId;
    private PlanCode planCode;
    private String planDisplayName;
    private String planDescription;
    private LocalDateTime assignedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
