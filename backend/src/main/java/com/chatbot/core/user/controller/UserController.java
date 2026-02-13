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

/**
 * User Controller - REST API cho Frontend
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // ===== NEW ENDPOINTS (/api/users) =====
    
    /**
     * Get current user profile
     */
    @GetMapping("/api/users/me")
    public ResponseEntity<UserFullResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        User user = currentUser.getUser();
        
        return ResponseEntity.ok(userService.getFullProfile(user.getId()));
    }

    /**
     * Get user profile by ID
     */
    @GetMapping("/api/users/{id}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getProfile(id));
    }

    /**
     * Update current user profile
     */
    @PutMapping("/api/users/me")
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
    @PutMapping("/api/users/me/basic-info")
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
    @PutMapping("/api/users/me/professional-info")
    public ResponseEntity<UserProfileResponse> updateProfessionalInfo(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UserRequest request) {
        
        User user = currentUser.getUser();
        
        UserProfileResponse response = userService.updateProfessionalInfo(user.getId(), request);
        log.info("Updated professional info for user: {}", currentUser.getUsername());
        
        return ResponseEntity.ok(response);
    }
}
