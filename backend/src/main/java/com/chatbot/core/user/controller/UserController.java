package com.chatbot.core.user.controller;

import com.chatbot.core.user.dto.*;
import com.chatbot.core.user.service.UserService;
import com.chatbot.core.identity.security.CustomUserDetails;
import com.chatbot.core.identity.model.Auth;
import com.chatbot.core.identity.repository.AuthRepository;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final AuthRepository authRepository;

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserFullResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        Auth auth = authRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return ResponseEntity.ok(userService.getFullProfile(auth.getId()));
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
        
        Auth auth = authRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserProfileResponse response = userService.updateProfile(auth.getId(), request);
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
        
        Auth auth = authRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserProfileResponse response = userService.updateAvatar(auth.getId(), file);
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

    // ===== Methods from UserInfoController (Migrated) =====
    
    /**
     * Update Basic Info Only - Separate endpoint for basic information
     */
    @PutMapping("/me/basic-info")
    public ResponseEntity<UserProfileResponse> updateBasicInfo(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UserRequest request) {
        
        Auth auth = authRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Extract basic info from request
        UserProfileResponse response = userService.updateBasicInfo(auth.getId(), request);
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
        
        Auth auth = authRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Extract professional info from request
        UserProfileResponse response = userService.updateProfessionalInfo(auth.getId(), request);
        log.info("Updated professional info for user: {}", currentUser.getUsername());
        
        return ResponseEntity.ok(response);
    }
}
