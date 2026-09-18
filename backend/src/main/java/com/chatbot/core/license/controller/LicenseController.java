package com.chatbot.core.license.controller;

import com.chatbot.core.license.dto.CreateLicenseRequest;
import com.chatbot.core.license.dto.LicenseResponse;
import com.chatbot.core.license.dto.UpdateLicenseRequest;
import com.chatbot.core.license.service.LicenseService;
import com.chatbot.core.identity.security.CustomUserDetails;
import com.chatbot.core.identity.service.JwtService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "License Management", description = "License management APIs")
public class LicenseController {

    private final LicenseService licenseService;
    private final JwtService jwtService;

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
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestHeader(value = "X-License-Token", required = false) String licenseToken) {
        
        Long userId = currentUser.getUser().getId();
        log.info("Fetching license for user: {}", userId);
        
        try {
            LicenseResponse response = licenseService.getLicenseForUser(userId);
            
            // If license token provided, verify it's signed by cloud
            if (licenseToken != null && !licenseToken.isEmpty()) {
                if (!jwtService.verifyLicenseSignedByCloud(licenseToken)) {
                    log.warn("Invalid license token provided for user: {}", userId);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
            }
            
            // Generate License JWT token for local app verification
            String userEmail = currentUser.getUser().getEmail();
            Long expiration = response.getExp() != null 
                ? response.getExp() 
                : (response.getExpiresAt() != null ? response.getExpiresAt().getEpochSecond() : null);
            
            if (expiration != null) {
                String token = jwtService.generateLicenseToken(
                    userEmail,
                    userId,
                    expiration,
                    response.getFeatures(),
                    response.getModules(),
                    response.getLimits()
                );
                response.setToken(token);
                log.info("Generated license JWT token for user: {}", userId);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch license for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLOUD_SERVICE')")
    @Operation(
        summary = "Create new license (Admin or Cloud Service only)",
        description = "Create a new license for a user. Only accessible by administrators or cloud services.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "License created successfully",
                content = @Content(schema = @Schema(implementation = LicenseResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - User already has active license"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Admin or Cloud Service role required")
        }
    )
    public ResponseEntity<LicenseResponse> createLicense(
            @Parameter(description = "License creation details", required = true)
            @Valid @RequestBody CreateLicenseRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        // Additional validation: Only cloud service can create licenses with specific features
        if (request.getFeatures() != null && !request.getFeatures().isEmpty()) {
            // Verify this is a cloud service operation
            boolean isCloudService = currentUser.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_CLOUD_SERVICE".equals(auth.getAuthority()));
            
            if (!isCloudService) {
                log.warn("Non-cloud service user {} attempted to create license with features: {}", 
                    currentUser.getUser().getEmail(), request.getFeatures());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build();
            }
        }
        
        log.info("Creating license for user: {} by {}", request.getUserId(), 
            currentUser.getUser().getEmail());
        
        try {
            LicenseResponse response = licenseService.createLicense(request);
            
            // Verify license was properly signed by cloud
            if (response.getExp() != null && !jwtService.verifyLicenseSignedByCloud(response.toString())) {
                log.error("License creation failed - invalid signature for user: {}", request.getUserId());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("License creation failed for user {}: {}", request.getUserId(), e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
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
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        
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
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        
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
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        Long userId = currentUser.getUser().getId();
        Integer limit = licenseService.getLimit(userId, limitKey);
        
        ApiResponse<Integer> response = ApiResponse.success(limit, "Limit retrieved for: " + limitKey);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activate")
    @Operation(
        summary = "Browser activation endpoint for Local app",
        description = "Receives deviceId and state from Local, generates License JWT, and redirects back to Local callback"
    )
    public ResponseEntity<Void> activateLicenseBrowser(
            @Parameter(description = "Device ID from Local app", required = true)
            @RequestParam String deviceId,
            @Parameter(description = "State/nonce from Local app for CSRF protection", required = true)
            @RequestParam String state,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        log.info("Received browser activation request - deviceId: {}, state: {}", deviceId, state);
        
        try {
            // If user is not authenticated, redirect to login page with deviceId and state
            if (currentUser == null) {
                log.info("User not authenticated, redirecting to login with deviceId and state");
                String targetRedirect = String.format("/api/license/activate?deviceId=%s&state=%s",
                    java.net.URLEncoder.encode(deviceId, java.nio.charset.StandardCharsets.UTF_8),
                    java.net.URLEncoder.encode(state, java.nio.charset.StandardCharsets.UTF_8));
                String loginUrl = "/login?redirect=" + java.net.URLEncoder.encode(targetRedirect, java.nio.charset.StandardCharsets.UTF_8);
                return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", loginUrl)
                    .build();
            }
            
            // User is authenticated, generate License JWT
            Long userId = currentUser.getUser().getId();
            String userEmail = currentUser.getUser().getEmail();
            
            log.info("User authenticated: {} (ID: {}), binding device & generating License JWT for deviceId: {}", userEmail, userId, deviceId);
            licenseService.bindDevice(userId, deviceId, "Local Client (" + deviceId + ")");
            
            // Get or create license for user
            LicenseResponse licenseResponse;
            if (licenseService.hasActiveLicense(userId)) {
                licenseResponse = licenseService.getLicenseForUser(userId);
                log.info("User already has active license, reusing existing license");
            } else {
                // Create default license for user
                CreateLicenseRequest licenseRequest = CreateLicenseRequest.builder()
                    .userId(userId)
                    .planName("Free Plan")
                    .isActive(true)
                    .expiresAt(java.time.Instant.now().plusSeconds(86400 * 30)) // 30 days
                    .features(List.of("facebook", "zalo"))
                    .modules(List.of("reengage", "ai-reply"))
                    .limits(Map.of("bots", 2, "storage", 1000))
                    .build();
                licenseResponse = licenseService.createLicense(licenseRequest);
                log.info("Created new license for user: {}", userEmail);
            }
            
            // Generate License JWT with deviceId claim
            Long expiration = licenseResponse.getExp() != null 
                ? licenseResponse.getExp() 
                : (licenseResponse.getExpiresAt() != null ? licenseResponse.getExpiresAt().getEpochSecond() : null);
            
            if (expiration == null) {
                expiration = System.currentTimeMillis() / 1000 + (86400 * 30);
            }
            
            String licenseJwt = jwtService.generateLicenseToken(
                userEmail,
                userId,
                deviceId,
                expiration,
                licenseResponse.getFeatures(),
                licenseResponse.getModules(),
                licenseResponse.getLimits()
            );
            
            // HTTP 302 Redirect to Local callback with token and state
            String callbackUrl = String.format("http://localhost:1717/callback?token=%s&state=%s", licenseJwt, state);
            
            log.info("Redirecting to Local callback: {}", callbackUrl);
            return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", callbackUrl)
                .build();
            
        } catch (Exception e) {
            log.error("Failed to process browser activation: {}", e.getMessage(), e);
            // Redirect to error page
            return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/error?message=" + e.getMessage())
                .build();
        }
    }

    @PostMapping("/generate-token")
    @Operation(
        summary = "Generate activation token for user",
        description = "Generate JWT activation token for user to activate license on local app",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Activation token generated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "User already has active license"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
        }
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateActivationToken(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser,
            @Parameter(description = "License configuration", required = false)
            @RequestBody(required = false) Map<String, Object> licenseConfig) {
        
        Long userId = currentUser.getUser().getId();
        String userEmail = currentUser.getUser().getEmail();
        
        log.info("Generating activation token for user: {} (ID: {})", userEmail, userId);
        
        try {
            // Check if user already has active license
            if (licenseService.hasActiveLicense(userId)) {
                log.warn("User {} already has active license", userId);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.<Map<String, Object>>error("User already has active license"));
            }
            
            // Default license configuration
            List<String> features = licenseConfig != null && licenseConfig.containsKey("features") 
                ? (List<String>) licenseConfig.get("features")
                : List.of("facebook", "zalo");
                
            List<String> modules = licenseConfig != null && licenseConfig.containsKey("modules")
                ? (List<String>) licenseConfig.get("modules") 
                : List.of("reengage", "ai-reply");
                
            Map<String, Integer> limits = licenseConfig != null && licenseConfig.containsKey("limits")
                ? (Map<String, Integer>) licenseConfig.get("limits")
                : Map.of("bots", 2, "storage", 1000);
            
            // Generate expiration (30 days from now)
            Long expiration = System.currentTimeMillis() / 1000 + (86400 * 30);
            
            // Generate JWT activation token
            String token = jwtService.generateLicenseToken(
                userEmail, userId, expiration, features, modules, limits
            );
            
            // Create redirect URL to local app
            String redirectUrl = String.format("http://localhost:1717/callback?token=%s", token);
            
            Map<String, Object> response = Map.of(
                "token", token,
                "redirectUrl", redirectUrl,
                "expiresAt", expiration,
                "features", features,
                "modules", modules,
                "limits", limits,
                "message", "Activation token generated successfully"
            );
            
            log.info("Activation token generated for user: {}, redirect: {}", userEmail, redirectUrl);
            return ResponseEntity.ok(ApiResponse.success(response, "Token generated successfully"));
            
        } catch (Exception e) {
            log.error("Failed to generate activation token for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.<Map<String, Object>>error("Failed to generate token: " + e.getMessage()));
        }
    }

    @GetMapping("/activate-saas")
    @Operation(
        summary = "SaaS activation page",
        description = "Page for users to generate activation tokens for local app"
    )
    public String activationPage() {
        return "forward:/activation-saas.html";
    }

    @GetMapping("/public-key")
    @Operation(
        summary = "Get public key for license verification",
        description = "Returns the RSA public key in PEM format for local app to verify license JWT signatures"
    )
    public ResponseEntity<ApiResponse<String>> getPublicKey() {
        try {
            String publicKeyPem = jwtService.getPublicKeyPem();
            if (publicKeyPem == null || publicKeyPem.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Public key not configured"));
            }
            return ResponseEntity.ok(ApiResponse.success(publicKeyPem, "Public key retrieved successfully"));
        } catch (Exception e) {
            log.error("Failed to retrieve public key: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve public key: " + e.getMessage()));
        }
    }

    @PostMapping("/callback")
    @Operation(
        summary = "License activation callback",
        description = "Receive activation token from SaaS redirect and activate license",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "License activated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid callback data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already has active license")
        }
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> activationCallback(
            @Parameter(description = "Activation token", required = true)
            @RequestParam String token,
            @Parameter(description = "Redirect URL (optional)", required = false)
            @RequestParam(required = false) String redirectUrl) {
        
        log.info("Received activation callback with token: {}", token.substring(0, Math.min(10, token.length())));
        
        try {
            // Verify token is signed by cloud
            if (!jwtService.verifyLicenseSignedByCloud(token)) {
                log.warn("Invalid callback token - not signed by cloud");
                return ResponseEntity.badRequest()
                    .body(ApiResponse.<Map<String, Object>>error("Invalid activation token - signature verification failed"));
            }
            
            // Check if token is expired
            if (jwtService.isLicenseExpired(token)) {
                log.warn("Activation token has expired");
                return ResponseEntity.badRequest()
                    .body(ApiResponse.<Map<String, Object>>error("Activation token has expired"));
            }
            
            // Extract user info from token
            String userEmail = jwtService.extractEmailFromLicense(token);
            String userIdStr = jwtService.extractUserId(token);
            Long userId = Long.parseLong(userIdStr);
            
            log.info("Processing activation callback for user: {} (ID: {})", userEmail, userId);
            
            // Check if user already has active license
            if (licenseService.hasActiveLicense(userId)) {
                log.warn("User {} already has active license", userId);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.<Map<String, Object>>error("User already has active license"));
            }
            
            // Extract license data from token
            Long expiration = jwtService.extractExpiration(token);
            List<String> features = jwtService.extractFeatures(token);
            List<String> modules = jwtService.extractModules(token);
            Map<String, Integer> limits = jwtService.extractLimits(token);
            
            // Create license from token data
            CreateLicenseRequest licenseRequest = CreateLicenseRequest.builder()
                .userId(userId)
                .planName("Activated License")
                .isActive(true)
                .expiresAt(expiration != null ? 
                    java.time.Instant.ofEpochSecond(expiration) : 
                    java.time.Instant.now().plusSeconds(86400 * 30)) // 30 days default
                .features(features)
                .modules(modules)
                .limits(limits)
                .build();
            
            // Actually activate the license
            LicenseResponse licenseResponse = licenseService.createLicense(licenseRequest);
            
            log.info("License activated successfully for user: {} via callback", userEmail);
            
            Map<String, Object> data = Map.of(
                "license", licenseResponse,
                "redirectUrl", redirectUrl,
                "message", "License activated successfully"
            );
            
            return ResponseEntity.ok(ApiResponse.success(data, "License activated successfully"));
            
        } catch (Exception e) {
            log.error("Failed to process activation callback: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.<Map<String, Object>>error("Failed to activate license: " + e.getMessage()));
        }
    }

    @GetMapping("/devices")
    @Operation(
        summary = "Get user bound devices",
        description = "Returns list of active device bindings for current authenticated user"
    )
    public ResponseEntity<ApiResponse<List<com.chatbot.core.license.dto.DeviceBindingResponse>>> getUserDevices(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Unauthorized"));
        }
        List<com.chatbot.core.license.dto.DeviceBindingResponse> devices = licenseService.getUserDevices(currentUser.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(devices, "User devices retrieved successfully"));
    }

    @DeleteMapping("/devices/{deviceId}")
    @Operation(
        summary = "Unbind device",
        description = "Revokes device binding for specified deviceId"
    )
    public ResponseEntity<ApiResponse<String>> unbindDevice(
            @PathVariable String deviceId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Unauthorized"));
        }
        licenseService.unbindDevice(currentUser.getUser().getId(), deviceId);
        return ResponseEntity.ok(ApiResponse.success("Device unbound successfully", "Hủy liên kết thiết bị thành công"));
    }
}
