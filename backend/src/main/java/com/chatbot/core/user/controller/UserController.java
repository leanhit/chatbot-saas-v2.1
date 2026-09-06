package com.chatbot.core.user.controller;

import com.chatbot.core.user.dto.*;
import com.chatbot.core.user.service.UserService;
import com.chatbot.core.user.model.User;
import com.chatbot.core.identity.security.CustomUserDetails;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.service.TenantPackageService;
import com.chatbot.core.payment.plan.model.Package;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.responses.ApiResponse; // Use fully qualified name to avoid conflict
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * User Controller - REST API cho Frontend
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User profile and management operations")
public class UserController {

    private final UserService userService;
    private final TenantPackageService tenantPackageService;

    // ===== NEW ENDPOINTS (/api/users) =====
    
    /**
     * Search all users with pagination
     */
    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<UserDto>> searchUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) com.chatbot.core.identity.model.SystemRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("id").descending());
        return ResponseEntity.ok(userService.searchUsers(search, role, pageable));
    }

    /**
     * Update user active status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        Boolean isActive = request.get("isActive");
        if (isActive != null) {
            userService.updateUserStatus(id, isActive);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    @Operation(
        summary = "Get current user profile",
        description = "Retrieve the complete profile information for the authenticated user.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                content = @Content(schema = @Schema(implementation = UserFullResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    public ResponseEntity<UserFullResponse> getMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        User user = currentUser.getUser();
        
        return ResponseEntity.ok(userService.getFullProfile(user.getId()));
    }

    /**
     * Get user profile by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getProfile(id));
    }

    /**
     * Update current user profile
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody UserRequest request) {
        
        User user = currentUser.getUser();
        
        UserProfileResponse response = userService.updateProfile(user.getId(), request);
        log.info("Updated profile for user: {}", currentUser.getUsername());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update user avatar
     */
    @PutMapping("/me/avatar")
    public ResponseEntity<UserProfileResponse> updateAvatar(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam("avatar") MultipartFile file) {
        
        User user = currentUser.getUser();
        
        UserProfileResponse response = userService.updateAvatar(user.getId(), file);
        log.info("Updated avatar for user: {}", currentUser.getUsername());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get user basic info (for internal calls)
     */
    @GetMapping("/{id}/basic")
    public ResponseEntity<UserDto> getUserBasicInfo(@PathVariable Long id) {
        com.chatbot.core.user.model.User user = userService.getUser(id);
        
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .systemRole(user.getSystemRole().name())
                .isActive(user.getIsActive())
                .build();
        
        return ResponseEntity.ok(userDto);
    }

    // ===== LEGACY ENDPOINTS (/api/v1/user-info) - BACKWARD COMPATIBILITY =====
    
    /**
     * LEGACY: Get current user profile (backward compatibility)
     */
    @GetMapping("/v1/user-info/me")
    public ResponseEntity<UserFullResponse> getMyProfileLegacy(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        log.warn("Using legacy endpoint /api/v1/user-info/me - please migrate to /api/users/me");
        
        User user = currentUser.getUser();
        
        return ResponseEntity.ok(userService.getFullProfile(user.getId()));
    }

    /**
     * LEGACY: Update current user profile (backward compatibility)
     */
    @PutMapping("/v1/user-info/me")
    public ResponseEntity<UserProfileResponse> updateMyProfileLegacy(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody UserRequest request) {
        
        log.warn("Using legacy endpoint /api/v1/user-info/me - please migrate to /api/users/me");
        
        User user = currentUser.getUser();
        
        UserProfileResponse response = userService.updateProfile(user.getId(), request);
        log.info("Updated profile for user: {}", currentUser.getUsername());
        
        return ResponseEntity.ok(response);
    }

    /**
     * LEGACY: Update user avatar (backward compatibility)
     */
    @PutMapping("/v1/user-info/me/avatar")
    public ResponseEntity<UserProfileResponse> updateAvatarLegacy(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam("avatar") MultipartFile file) {
        
        log.warn("Using legacy endpoint /api/v1/user-info/me/avatar - please migrate to /api/users/me/avatar");
        
        User user = currentUser.getUser();
        
        UserProfileResponse response = userService.updateAvatar(user.getId(), file);
        log.info("Updated avatar for user: {}", currentUser.getUsername());
        
        return ResponseEntity.ok(response);
    }

    // ===== LEGACY ENDPOINTS FOR BASIC/PROFESSIONAL INFO =====
    
    /**
     * LEGACY: Update Basic Info Only (backward compatibility)
     */
    @PutMapping("/v1/user-info/me/basic-info")
    public ResponseEntity<UserProfileResponse> updateBasicInfoLegacy(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UserRequest request) {
        
        log.warn("Using legacy endpoint /api/v1/user-info/me/basic-info - please migrate to /api/users/me/basic-info");
        
        User user = currentUser.getUser();
        
        UserProfileResponse response = userService.updateBasicInfo(user.getId(), request);
        log.info("Updated basic info for user: {}", currentUser.getUsername());
        
        return ResponseEntity.ok(response);
    }

    /**
     * LEGACY: Update Professional Info Only (backward compatibility)
     */
    @PutMapping("/v1/user-info/me/professional-info")
    public ResponseEntity<UserProfileResponse> updateProfessionalInfoLegacy(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UserRequest request) {
        
        log.warn("Using legacy endpoint /api/v1/user-info/me/professional-info - please migrate to /api/users/me/professional-info");
        
        User user = currentUser.getUser();
        
        UserProfileResponse response = userService.updateProfessionalInfo(user.getId(), request);
        log.info("Updated professional info for user: {}", currentUser.getUsername());
        
        return ResponseEntity.ok(response);
    }

    // ===== NEW ENDPOINTS FOR BASIC/PROFESSIONAL INFO =====
    
    /**
     * Update Basic Info Only - Separate endpoint for basic information
     */
    @PutMapping("/me/basic-info")
    public ResponseEntity<UserProfileResponse> updateBasicInfo(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UserRequest request) {
        
        User user = currentUser.getUser();
        
        UserProfileResponse response = userService.updateBasicInfo(user.getId(), request);
        log.info("Updated basic info for user: {}", currentUser.getUsername());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel user's own join request
     */
    @DeleteMapping("/join-requests/{requestId}")
    public ResponseEntity<Void> cancelJoinRequest(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long requestId
    ) {
        User user = currentUser.getUser();
        userService.cancelJoinRequest(requestId, user);
        log.info("Cancelled join request: {} for user: {}", requestId, currentUser.getUsername());
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Get current user's package information
     */
    @GetMapping("/current-package")
    @Operation(
        summary = "Get current user package",
        description = "Retrieve the current package information for the authenticated user."
    )
    public ResponseEntity<Map<String, Object>> getCurrentPackage(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        User user = currentUser.getUser();
        
        try {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null && user != null && user.getEmail() != null) {
                try {
                    tenantId = tenantPackageService.extractTenantIdWithFallback(null, user.getEmail());
                } catch (Exception ex) {
                    log.warn("User {} has no tenant association: {}", user.getId(), ex.getMessage());
                }
            }
            
            if (tenantId == null) {
                log.warn("User {} has no tenant association", user != null ? user.getId() : "null");
                Map<String, Object> response = new HashMap<>();
                response.put("currentPackage", null);
                response.put("packageHistory", List.of());
                response.put("error", "No tenant association found");
                return ResponseEntity.ok(response);
            }
            
            // Get current package from TenantPackageService
            Package currentPackage = tenantPackageService.getCurrentTenantPackage(tenantId);
            
            // Package history list
            List<Map<String, Object>> packageHistory = List.of();
            
            Map<String, Object> currentPackageData = new HashMap<>();
            if (currentPackage != null) {
                currentPackageData.put("id", currentPackage.getPackageId());
                currentPackageData.put("name", currentPackage.getName());
                currentPackageData.put("price", currentPackage.getPrice());
                currentPackageData.put("duration", "Vĩnh viễn"); // Could be enhanced with actual duration logic
                currentPackageData.put("messageLimit", currentPackage.getMessageLimit());
                currentPackageData.put("chatbotLimit", currentPackage.getChatbotLimit());
                currentPackageData.put("hasPrioritySupport", currentPackage.getHasPrioritySupport());
                currentPackageData.put("hasAnalytics", currentPackage.getHasAnalytics());
                currentPackageData.put("hasAdvancedAnalytics", currentPackage.getHasAdvancedAnalytics());
                currentPackageData.put("hasCustomIntegrations", currentPackage.getHasCustomIntegrations());
                currentPackageData.put("hasDedicatedSupport", currentPackage.getHasDedicatedSupport());
                currentPackageData.put("hasCustomFeatures", currentPackage.getHasCustomFeatures());
                currentPackageData.put("hasSlaGuarantee", currentPackage.getHasSlaGuarantee());
            } else {
                currentPackageData.put("id", "free");
                currentPackageData.put("name", "Free");
                currentPackageData.put("price", 0);
                currentPackageData.put("duration", "Vĩnh viễn");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("currentPackage", currentPackageData);
            response.put("packageHistory", packageHistory != null ? packageHistory : List.of());
            
            log.info("Retrieved package information for user: {}, tenant: {}", user.getId(), tenantId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving package information for user {}: {}", user.getId(), e.getMessage(), e);
            
            // Return error response with fallback data
            Map<String, Object> response = new HashMap<>();
            response.put("currentPackage", Map.of(
                "id", "free",
                "name", "Free",
                "price", 0,
                "duration", "Vĩnh viễn"
            ));
            response.put("packageHistory", List.of());
            response.put("error", e.getMessage());
            
            return ResponseEntity.ok(response);
        }
    }
}
