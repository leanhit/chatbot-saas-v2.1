package com.chatbot.core.tenant.membership.dto;

import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.model.TenantVisibility;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TenantPendingResponse {
    private Long id;
    private String name;    
    private TenantStatus status;
    private TenantVisibility visibility;
    private LocalDateTime requestedAt;
}
