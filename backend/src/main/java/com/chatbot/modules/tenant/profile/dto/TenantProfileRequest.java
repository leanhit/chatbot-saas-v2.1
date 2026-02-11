package com.chatbot.modules.tenant.profile.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TenantProfileRequest {

    // business information
    private String description;
    private String industry;
    private String plan;
    private String companySize;

    // legal
    private String legalName;
    private String taxCode;

    // contact
    private String contactEmail;
    private String contactPhone;

    // branding
    private String logoUrl;
    private String faviconUrl;
    private String primaryColor;
}
