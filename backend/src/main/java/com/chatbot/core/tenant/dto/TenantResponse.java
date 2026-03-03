package com.chatbot.core.tenant.dto;

import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.model.TenantVisibility;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class TenantResponse {
    private Long id;
    private String tenantKey;
    private String name;
    private TenantStatus status;
    private TenantVisibility visibility;
    
    // Instant sẽ được serialize thành ISO 8601 tự động bởi Jackson
    private Instant expiresAt;
    private Instant createdAt;
    
    // Profile fields
    private String logoUrl;
    private String contactEmail;
    private String contactPhone;
}
