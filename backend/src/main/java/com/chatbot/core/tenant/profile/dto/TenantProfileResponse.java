package com.chatbot.core.tenant.profile.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TenantProfileResponse {

    private Long tenantId;

    // business information
    private String description;
    private String industry;
    private String plan;
    private String companySize;

    private String legalName;
    private String taxCode;

    private String contactEmail;
    private String contactPhone;

    private String logoUrl;
    private String faviconUrl;
    private String primaryColor;
}
