package com.chatbot.core.tenant.professional.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantProfessionalResponse {
    @JsonIgnore
    private Long id;
    @JsonIgnore
    private Long tenantId;
    private String jobTitle;
    private String department;
    private String company;
    private String linkedinUrl;
    private String website;
    private String location;
    private String skills;
    private String experience;
    private String education;
    private String certifications;
    private String languages;
    private String availability;
    private String hourlyRate;
    private String portfolioUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
