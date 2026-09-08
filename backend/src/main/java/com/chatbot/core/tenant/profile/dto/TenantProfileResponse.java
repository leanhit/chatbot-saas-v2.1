package com.chatbot.core.tenant.profile.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantProfileResponse {
    @JsonIgnore
    private Long tenantId;
    private String description;
    private String industry;
    private String plan;
    private String companySize;
    private String legalName;
    private String taxCode;
    private String contactEmail;
    private String contactPhone;
    private String website;
    private String logoUrl;
    private String faviconUrl;
    private String primaryColor;
}
