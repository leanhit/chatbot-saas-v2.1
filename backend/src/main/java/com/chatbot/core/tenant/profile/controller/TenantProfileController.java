package com.chatbot.core.tenant.profile.controller;

import com.chatbot.core.tenant.profile.dto.TenantProfileRequest;
import com.chatbot.core.tenant.profile.dto.TenantProfileResponse;
import com.chatbot.core.tenant.profile.service.TenantProfileService;
import com.chatbot.core.tenant.core.model.Tenant;
import com.chatbot.core.tenant.core.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class TenantProfileController {

    private final TenantProfileService tenantProfileService;
    private final TenantRepository tenantRepository;

    @GetMapping("/{tenantKey}/profile")
    public ResponseEntity<TenantProfileResponse> getProfile(
            @PathVariable String tenantKey
    ) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new RuntimeException("Tenant not found with key: " + tenantKey));
        return ResponseEntity.ok(
                tenantProfileService.getProfile(tenant.getId())
        );
    }

    @PutMapping("/{tenantKey}/profile")
    public ResponseEntity<TenantProfileResponse> updateProfile(
            @PathVariable String tenantKey,
            @RequestBody TenantProfileRequest request
    ) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new RuntimeException("Tenant not found with key: " + tenantKey));
        return ResponseEntity.ok(
                tenantProfileService.upsertProfile(tenant.getId(), request)
        );
    }

    @PutMapping("/{tenantKey}/logo")
    public ResponseEntity<TenantProfileResponse> updateLogo(
            @PathVariable String tenantKey,
            @RequestParam("logo") MultipartFile file
    ) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new RuntimeException("Tenant not found with key: " + tenantKey));
        return ResponseEntity.ok(
                tenantProfileService.updateLogo(tenant.getId(), file)
        );
    }

    /**
     * Update tenant profile data only (JSON)
     * Usage: PUT /api/tenant/{tenantKey} with JSON body
     */
    @PutMapping(value = "/{tenantKey}", consumes = "application/json")
    public ResponseEntity<TenantProfileResponse> updateTenantProfile(
            @PathVariable String tenantKey,
            @RequestBody TenantProfileRequest request
    ) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new RuntimeException("Tenant not found with key: " + tenantKey));
        
        return ResponseEntity.ok(
            tenantProfileService.upsertProfile(tenant.getId(), request)
        );
    }

    /**
     * Update tenant with logo upload (multipart/form-data)
     * Usage: PUT /api/tenant/{tenantKey} with multipart/form-data
     */
    @PutMapping(value = "/{tenantKey}", consumes = "multipart/form-data")
    public ResponseEntity<TenantProfileResponse> updateTenantWithLogo(
            @PathVariable String tenantKey,
            @RequestPart(value = "request", required = false) TenantProfileRequest request,
            @RequestParam(value = "logo", required = false) MultipartFile file
    ) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new RuntimeException("Tenant not found with key: " + tenantKey));
        
        // Handle logo upload first if provided
        if (file != null && !file.isEmpty()) {
            TenantProfileResponse logoResponse = tenantProfileService.updateLogo(tenant.getId(), file);
            
            // If no profile data to update, return logo update result
            if (request == null) {
                return ResponseEntity.ok(logoResponse);
            }
            
            // If both logo and profile data, merge the logo URL into request
            request.setLogoUrl(logoResponse.getLogoUrl());
        }
        
        // Handle profile data update
        if (request != null) {
            return ResponseEntity.ok(
                tenantProfileService.upsertProfile(tenant.getId(), request)
            );
        }
        
        // Neither file nor request provided
        throw new IllegalArgumentException("Either profile data or logo file must be provided");
    }
}
