package com.chatbot.core.tenant.professional.service;

import com.chatbot.core.tenant.professional.dto.TenantProfessionalRequest;
import com.chatbot.core.tenant.professional.dto.TenantProfessionalResponse;
import com.chatbot.core.tenant.professional.mapper.TenantProfessionalMapper;
import com.chatbot.core.tenant.professional.model.TenantProfessional;
import com.chatbot.core.tenant.professional.repository.TenantProfessionalRepository;
import com.chatbot.core.tenant.core.model.Tenant;
import com.chatbot.core.tenant.core.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TenantProfessionalService {

    private final TenantProfessionalRepository tenantProfessionalRepository;
    private final TenantProfessionalMapper tenantProfessionalMapper;
    private final TenantRepository tenantRepository;

    public TenantProfessionalResponse getProfessional(Long tenantId) {
        TenantProfessional professional = tenantProfessionalRepository.findByTenantId(tenantId)
                .orElse(null);
        
        if (professional == null) {
            return null;
        }
        
        return tenantProfessionalMapper.toResponse(professional);
    }

    public TenantProfessionalResponse upsertProfessional(Long tenantId, TenantProfessionalRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found with id: " + tenantId));

        TenantProfessional professional = tenantProfessionalRepository.findByTenantId(tenantId)
                .orElse(TenantProfessional.builder()
                        .tenant(tenant)
                        .build());

        // Update fields from request
        professional.setJobTitle(request.getJobTitle());
        professional.setDepartment(request.getDepartment());
        professional.setCompany(request.getCompany());
        professional.setLinkedinUrl(request.getLinkedinUrl());
        professional.setWebsite(request.getWebsite());
        professional.setLocation(request.getLocation());
        professional.setSkills(request.getSkills());
        professional.setExperience(request.getExperience());
        professional.setEducation(request.getEducation());
        professional.setCertifications(request.getCertifications());
        professional.setLanguages(request.getLanguages());
        professional.setAvailability(request.getAvailability());
        professional.setHourlyRate(request.getHourlyRate());
        professional.setPortfolioUrl(request.getPortfolioUrl());

        professional = tenantProfessionalRepository.save(professional);
        return tenantProfessionalMapper.toResponse(professional);
    }

    public void deleteProfessional(Long tenantId) {
        tenantProfessionalRepository.deleteByTenantId(tenantId);
    }
}
