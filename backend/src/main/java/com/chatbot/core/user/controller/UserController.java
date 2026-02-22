package com.chatbot.core.user.controller;

import com.chatbot.core.user.dto.*;
import com.chatbot.core.user.service.UserService;
import com.chatbot.core.user.model.User;
import com.chatbot.core.identity.security.CustomUserDetails;
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

    // ===== NEW ENDPOINTS (/api/users) =====
    
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
    @PutMapping("/api/users/me/avatar")
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
    @GetMapping("/api/users/{id}/basic")
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
    @GetMapping("/api/v1/user-info/me")
    public ResponseEntity<UserFullResponse> getMyProfileLegacy(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        log.warn("Using legacy endpoint /api/v1/user-info/me - please migrate to /api/users/me");
        
        User user = currentUser.getUser();
        
        return ResponseEntity.ok(userService.getFullProfile(user.getId()));
    }

    /**
     * LEGACY: Update current user profile (backward compatibility)
     */
    @PutMapping("/api/v1/user-info/me")
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
    @PutMapping("/api/v1/user-info/me/avatar")
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
    @PutMapping("/api/v1/user-info/me/basic-info")
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
    @PutMapping("/api/v1/user-info/me/professional-info")
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
     * Update Professional Info Only - Separate endpoint for professional information
     */
    @PutMapping("/me/professional-info")
    public ResponseEntity<UserProfileResponse> updateProfessionalInfo(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UserRequest request) {
        
        User user = currentUser.getUser();
        
        UserProfileResponse response = userService.updateProfessionalInfo(user.getId(), request);
        log.info("Updated professional info for user: {}", currentUser.getUsername());
        
        return ResponseEntity.ok(response);
    }
}
