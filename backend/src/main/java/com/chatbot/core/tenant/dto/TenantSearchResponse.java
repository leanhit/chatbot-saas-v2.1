package com.chatbot.core.tenant.dto;

import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.model.TenantVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantSearchResponse {
    private Long id;
    private String name;
    private TenantStatus status;
    private TenantVisibility visibility;
    private LocalDateTime createdAt;
    private TenantMembershipStatus membershipStatus;
    
    // Các trường bổ sung giúp nhận diện
    private String logoUrl;      // Từ Profile
    private String contactEmail; // Từ Profile (nên che mờ ở FE hoặc BE)
    private String province;     // Từ Address
}
