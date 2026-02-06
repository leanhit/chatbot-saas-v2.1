package com.chatbot.modules.tenant.membership.dto;

import com.chatbot.modules.tenant.core.model.TenantStatus;
import com.chatbot.modules.tenant.core.model.TenantVisibility;

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
