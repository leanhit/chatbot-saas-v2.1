package com.chatbot.core.tenant.core.dto;

import com.chatbot.core.tenant.core.model.TenantStatus;
import com.chatbot.core.tenant.core.model.TenantVisibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TenantResponse {
    private String tenantKey; // ✅ Thay vì id
    private String name;
    private TenantStatus status;
    private TenantVisibility visibility;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    
    // Profile fields
    private String logoUrl;
    private String contactEmail;
    private String contactPhone;
}
