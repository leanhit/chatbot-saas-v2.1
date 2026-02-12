package com.chatbot.core.tenant.professional.controller;

import com.chatbot.core.tenant.professional.dto.TenantProfessionalRequest;
import com.chatbot.core.tenant.professional.dto.TenantProfessionalResponse;
import com.chatbot.core.tenant.professional.service.TenantProfessionalService;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenant")
@RequiredArgsConstructor
public class TenantProfessionalController {

    private final TenantProfessionalService tenantProfessionalService;
    private final TenantRepository tenantRepository;

    @GetMapping("/professional/{tenantKey}")
    public ResponseEntity<TenantProfessionalResponse> getProfessional(
            @PathVariable String tenantKey
    ) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new RuntimeException("Tenant not found with key: " + tenantKey));
        
        TenantProfessionalResponse response = tenantProfessionalService.getProfessional(tenant.getId());
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/professional/{tenantKey}")
    public ResponseEntity<TenantProfessionalResponse> updateProfessional(
            @PathVariable String tenantKey,
            @RequestBody TenantProfessionalRequest request
    ) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new RuntimeException("Tenant not found with key: " + tenantKey));
        
        return ResponseEntity.ok(
                tenantProfessionalService.upsertProfessional(tenant.getId(), request)
        );
    }

    @DeleteMapping("/professional/{tenantKey}")
    public ResponseEntity<Void> deleteProfessional(
            @PathVariable String tenantKey
    ) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new RuntimeException("Tenant not found with key: " + tenantKey));
        
        tenantProfessionalService.deleteProfessional(tenant.getId());
        return ResponseEntity.noContent().build();
    }
}
