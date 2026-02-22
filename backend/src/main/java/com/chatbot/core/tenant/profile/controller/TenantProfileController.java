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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
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
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new RuntimeException("Tenant not found with key: " + tenantKey));
        return ResponseEntity.ok(
                tenantProfileService.updateLogo(tenant.getId(), file)
        );
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
