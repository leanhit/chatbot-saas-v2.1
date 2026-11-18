package com.chatbot.core.tenant.dto;

import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.model.TenantVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import com.chatbot.shared.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantSearchResponse {
    @JsonIgnore
    private Long id;
    private String tenantKey;
    private String name;
    private TenantStatus status;
    private TenantVisibility visibility;
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    private LocalDateTime createdAt;
    private TenantMembershipStatus membershipStatus;
    
    // Các trường bổ sung giúp nhận diện
    private String logoUrl;      // Từ Profile
    private String contactEmail; // Từ Profile (nên che mờ ở FE hoặc BE)
    private String province;     // Từ Address
}
