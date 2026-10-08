package com.chatbot.core.tenant.dto;

import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.model.TenantVisibility;
import com.chatbot.shared.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;

@Getter
@Setter
@Builder
public class TenantResponse {
    @JsonIgnore
    private Long id;
    private String tenantKey;
    private String name;
    private TenantStatus status;
    private TenantVisibility visibility;
    
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    private Instant expiresAt;
    
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    private Instant createdAt;
    
    // Profile fields
    private String logoUrl;
    private String contactEmail;
    private String contactPhone;
    private String website;
    // Package fields
    private String currentPackageId;
    private String currentPackageName;
    
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    private Instant packageActivatedAt;
}
