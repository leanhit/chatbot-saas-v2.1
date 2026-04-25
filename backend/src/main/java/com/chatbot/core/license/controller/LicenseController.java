package com.chatbot.core.license.controller;

import com.chatbot.core.license.dto.CreateLicenseRequest;
import com.chatbot.core.license.dto.LicenseResponse;
import com.chatbot.core.license.dto.UpdateLicenseRequest;
import com.chatbot.core.license.service.LicenseService;
import com.chatbot.core.identity.security.CustomUserDetails;
import com.chatbot.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "License Management", description = "License management APIs")
public class LicenseController {

    private final LicenseService licenseService;

    @GetMapping("/me")
    @Operation(
        summary = "Get current user's license",
        description = "Retrieve the active license for the authenticated user",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "License retrieved successfully",
                content = @Content(schema = @Schema(implementation = LicenseResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No active license found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - License expired or inactive")
        }
    )
    public ResponseEntity<LicenseResponse> getMyLicense(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "user") CustomUserDetails currentUser) {
        
        Long userId = currentUser.getUser().getId();
        log.info("Fetching license for user: {}", userId);
        
        LicenseResponse response = licenseService.getLicenseForUser(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create new license (Admin only)",
        description = "Create a new license for a user. Only accessible by administrators.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "License created successfully",
                content = @Content(schema = @Schema(implementation = LicenseResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - User already has active license"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        }
    )
    public ResponseEntity<LicenseResponse> createLicense(
            @Parameter(description = "License creation details", required = true)
            @Valid @RequestBody CreateLicenseRequest request) {
        
        log.info("Creating license for user: {} by admin", request.getUserId());
        LicenseResponse response = licenseService.createLicense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{licenseId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update license (Admin only)",
        description = "Update an existing license. Only accessible by administrators.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "License updated successfully",
                content = @Content(schema = @Schema(implementation = LicenseResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "License not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        }
    )
    public ResponseEntity<LicenseResponse> updateLicense(
            @Parameter(description = "License ID", required = true)
            @PathVariable Long licenseId,
            @Parameter(description = "License update details", required = true)
            @Valid @RequestBody UpdateLicenseRequest request) {
        
        log.info("Updating license: {} by admin", licenseId);
        LicenseResponse response = licenseService.updateLicense(licenseId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{licenseId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Revoke license (Admin only)",
        description = "Revoke (deactivate) a license. Only accessible by administrators.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "License revoked successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "License not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        }
    )
    public ResponseEntity<ApiResponse<String>> revokeLicense(
            @Parameter(description = "License ID", required = true)
            @PathVariable Long licenseId) {
        
        log.info("Revoking license: {} by admin", licenseId);
        licenseService.revokeLicense(licenseId);
        
        ApiResponse<String> response = ApiResponse.success("License " + licenseId + " has been revoked", "License revoked successfully");
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Revoke all licenses for user (Admin only)",
        description = "Revoke all active licenses for a specific user. Only accessible by administrators.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All licenses revoked successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        }
    )
    public ResponseEntity<ApiResponse<String>> revokeAllLicensesForUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {
        
        log.info("Revoking all licenses for user: {} by admin", userId);
        licenseService.revokeLicenseForUser(userId);
        
        ApiResponse<String> response = ApiResponse.success("All active licenses for user " + userId + " have been revoked", "All licenses revoked successfully for user " + userId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check/feature/{feature}")
    @Operation(
        summary = "Check if user has access to a feature",
        description = "Check if the authenticated user has access to a specific feature",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Feature access checked successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No active license found")
        }
    )
    public ResponseEntity<ApiResponse<Boolean>> checkFeatureAccess(
            @Parameter(description = "Feature name to check", required = true)
            @PathVariable String feature,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "user") CustomUserDetails currentUser) {
        
        Long userId = currentUser.getUser().getId();
        boolean hasFeature = licenseService.hasFeature(userId, feature);
        
        ApiResponse<Boolean> response = ApiResponse.success(hasFeature, "Feature access checked for: " + feature);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check/module/{module}")
    @Operation(
        summary = "Check if user has access to a module",
        description = "Check if the authenticated user has access to a specific module",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Module access checked successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No active license found")
        }
    )
    public ResponseEntity<ApiResponse<Boolean>> checkModuleAccess(
            @Parameter(description = "Module name to check", required = true)
            @PathVariable String module,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "user") CustomUserDetails currentUser) {
        
        Long userId = currentUser.getUser().getId();
        boolean hasModule = licenseService.hasModule(userId, module);
        
        ApiResponse<Boolean> response = ApiResponse.success(hasModule, "Module access checked for: " + module);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check/limit/{limitKey}")
    @Operation(
        summary = "Get user's limit for a specific resource",
        description = "Get the maximum allowed value for a specific limit (e.g., bots, storage)",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Limit retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No active license found")
        }
    )
    public ResponseEntity<ApiResponse<Integer>> getLimit(
            @Parameter(description = "Limit key (e.g., 'bots', 'storage')", required = true)
            @PathVariable String limitKey,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "user") CustomUserDetails currentUser) {
        
        Long userId = currentUser.getUser().getId();
        Integer limit = licenseService.getLimit(userId, limitKey);
        
        ApiResponse<Integer> response = ApiResponse.success(limit, "Limit retrieved for: " + limitKey);
        
        return ResponseEntity.ok(response);
    }
}
