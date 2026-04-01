package com.chatbot.core.tenant.profile.controller;

import com.chatbot.core.tenant.profile.dto.TenantProfileRequest;
import com.chatbot.core.tenant.profile.dto.TenantProfileResponse;
import com.chatbot.core.tenant.profile.service.TenantProfileService;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.responses.ApiResponse; // Use fully qualified name to avoid conflict
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tenant Profile Management", description = "Tenant profile and branding operations")
public class TenantProfileController {

    private final TenantProfileService tenantProfileService;
    private final TenantRepository tenantRepository;

    @GetMapping("/{tenantKey}/profile")
    @Operation(
        summary = "Get tenant profile",
        description = "Retrieve profile information for a specific tenant",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                content = @Content(schema = @Schema(implementation = TenantProfileResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tenant not found")
        }
    )
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
    @Operation(
        summary = "Update tenant profile",
        description = "Update profile information for a specific tenant",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated successfully",
                content = @Content(schema = @Schema(implementation = TenantProfileResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid profile data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tenant not found")
        }
    )
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
    @Operation(
        summary = "Update tenant logo",
        description = "Upload and update logo for a specific tenant",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logo updated successfully",
                content = @Content(schema = @Schema(implementation = TenantProfileResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file format or size"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tenant not found")
        }
    )
    public ResponseEntity<TenantProfileResponse> updateLogo(
            @PathVariable String tenantKey,
            @RequestParam("logo") MultipartFile file
    ) {
        try {
            log.info("🔄 [TENANT PROFILE CONTROLLER] Logo upload request - tenantKey: {}, fileName: {}, fileSize: {}", 
                    tenantKey, file.getOriginalFilename(), file.getSize());
            
            // Validate file
            if (file == null) {
                log.error("❌ [TENANT PROFILE CONTROLLER] File is null");
                throw new IllegalArgumentException("File cannot be null");
            }
            
            // Temporarily allow empty files for debugging
            if (file.isEmpty()) {
                log.warn("⚠️ [TENANT PROFILE CONTROLLER] File is empty (size: 0), but allowing for debugging");
                // throw new IllegalArgumentException("File cannot be empty");
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.error("❌ [TENANT PROFILE CONTROLLER] Invalid file type: {}", contentType);
                throw new IllegalArgumentException("Only image files are allowed");
            }
            
            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                log.error("❌ [TENANT PROFILE CONTROLLER] File too large: {} bytes", file.getSize());
                throw new IllegalArgumentException("File size cannot exceed 5MB");
            }
            
            log.info("✅ [TENANT PROFILE CONTROLLER] File validation passed, finding tenant");
            
            Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                    .orElseThrow(() -> {
                        log.error("❌ [TENANT PROFILE CONTROLLER] Tenant not found: {}", tenantKey);
                        return new RuntimeException("Tenant not found with key: " + tenantKey);
                    });
            
            log.info("✅ [TENANT PROFILE CONTROLLER] Found tenant: {} (ID: {}), calling service", tenant.getTenantKey(), tenant.getId());
            
            TenantProfileResponse response = tenantProfileService.updateLogo(tenant.getId(), file);
            
            log.info("✅ [TENANT PROFILE CONTROLLER] Logo updated successfully for tenant: {}", tenantKey);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("❌ [TENANT PROFILE CONTROLLER] Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("💥 [TENANT PROFILE CONTROLLER] Unexpected error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update logo: " + e.getMessage(), e);
        }
    }

    @PutMapping(value = "/{tenantKey}", consumes = "application/json")
    @Operation(
        summary = "Update tenant profile data",
        description = "Update tenant profile information using JSON data",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated successfully",
                content = @Content(schema = @Schema(implementation = TenantProfileResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid profile data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tenant not found")
        }
    )
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

    @PutMapping(value = "/{tenantKey}", consumes = "multipart/form-data")
    @Operation(
        summary = "Update tenant with logo",
        description = "Update tenant profile information and optionally upload logo using multipart form data",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant updated successfully",
                content = @Content(schema = @Schema(implementation = TenantProfileResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid data or file format"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tenant not found")
        }
    )
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
