package com.chatbot.modules.tenant.professional.mapper;

import com.chatbot.modules.tenant.professional.dto.TenantProfessionalRequest;
import com.chatbot.modules.tenant.professional.dto.TenantProfessionalResponse;
import com.chatbot.modules.tenant.professional.model.TenantProfessional;
import org.springframework.stereotype.Component;

@Component
public class TenantProfessionalMapper {

    public TenantProfessionalResponse toResponse(TenantProfessional professional) {
        if (professional == null) {
            return null;
        }

        return TenantProfessionalResponse.builder()
                .id(professional.getId())
                .tenantId(professional.getTenant() != null ? professional.getTenant().getId() : null)
                .jobTitle(professional.getJobTitle())
                .department(professional.getDepartment())
                .company(professional.getCompany())
                .linkedinUrl(professional.getLinkedinUrl())
                .website(professional.getWebsite())
                .location(professional.getLocation())
                .skills(professional.getSkills())
                .experience(professional.getExperience())
                .education(professional.getEducation())
                .certifications(professional.getCertifications())
                .languages(professional.getLanguages())
                .availability(professional.getAvailability())
                .hourlyRate(professional.getHourlyRate())
                .portfolioUrl(professional.getPortfolioUrl())
                .createdAt(professional.getCreatedAt())
                .updatedAt(professional.getUpdatedAt())
                .build();
    }

    public TenantProfessional toEntity(TenantProfessionalRequest request, Long tenantId) {
        if (request == null) {
            return null;
        }

        return TenantProfessional.builder()
                .jobTitle(request.getJobTitle())
                .department(request.getDepartment())
                .company(request.getCompany())
                .linkedinUrl(request.getLinkedinUrl())
                .website(request.getWebsite())
                .location(request.getLocation())
                .skills(request.getSkills())
                .experience(request.getExperience())
                .education(request.getEducation())
                .certifications(request.getCertifications())
                .languages(request.getLanguages())
                .availability(request.getAvailability())
                .hourlyRate(request.getHourlyRate())
                .portfolioUrl(request.getPortfolioUrl())
                // tenant will be set separately
                .build();
    }
}
