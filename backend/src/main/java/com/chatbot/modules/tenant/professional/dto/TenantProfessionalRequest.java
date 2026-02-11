package com.chatbot.modules.tenant.professional.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TenantProfessionalRequest {
    private String jobTitle;
    private String department;
    private String company;
    private String linkedinUrl;
    private String website;
    private String location;
    
    @Size(max = 1000, message = "Skills must not exceed 1000 characters")
    private String skills;
    
    @Size(max = 1000, message = "Experience must not exceed 1000 characters")
    private String experience;
    
    @Size(max = 500, message = "Education must not exceed 500 characters")
    private String education;
    
    @Size(max = 500, message = "Certifications must not exceed 500 characters")
    private String certifications;
    
    @Size(max = 200, message = "Languages must not exceed 200 characters")
    private String languages;
    
    private String availability;
    private String hourlyRate;
    private String portfolioUrl;
}
