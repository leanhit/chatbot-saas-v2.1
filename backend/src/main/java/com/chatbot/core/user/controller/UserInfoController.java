package com.chatbot.core.user.controller;

import com.chatbot.core.user.dto.UserFullResponse;
import com.chatbot.core.user.dto.UserRequest;
import com.chatbot.core.user.dto.UserProfileResponse;
import com.chatbot.core.user.service.UserService;
import com.chatbot.core.identity.security.CustomUserDetails;
import com.chatbot.core.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

/**
 * User Info Controller - v1 API for user information
 */
@RestController
@RequestMapping("/api/v1/user-info")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Info v1", description = "User information endpoints v1")
public class UserInfoController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(
        summary = "Get current user information",
        description = "Retrieve the complete information for the authenticated user.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User information retrieved successfully",
                content = @Content(schema = @Schema(implementation = UserFullResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    public ResponseEntity<UserFullResponse> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Getting current user info for user: {}", userDetails.getUsername());
        
        try {
            UserFullResponse userResponse = userService.getFullProfile(userDetails.getUser().getId());
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            log.error("Error getting user profile: {}", e.getMessage(), e);
            
            // Fallback: create basic user response
            User user = userDetails.getUser();
            UserFullResponse basicResponse = UserFullResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .systemRole(user.getSystemRole().toString())
                    .isActive(user.getIsActive())
                    .profile(null)
                    .addresses(List.of())
                    .build();
            
            return ResponseEntity.ok(basicResponse);
        }
    }

    @GetMapping("/profile")
    @Operation(
        summary = "Get current user profile",
        description = "Retrieve the profile information for the authenticated user.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully",
                content = @Content(schema = @Schema(implementation = UserFullResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    public ResponseEntity<UserFullResponse> getUserProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Getting user profile for user: {}", userDetails.getUsername());
        
        try {
            UserFullResponse userResponse = userService.getFullProfile(userDetails.getUser().getId());
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            log.error("Error getting user profile: {}", e.getMessage(), e);
            
            // Fallback: create basic user response
            User user = userDetails.getUser();
            UserFullResponse basicResponse = UserFullResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .systemRole(user.getSystemRole().toString())
                    .isActive(user.getIsActive())
                    .profile(null)
                    .addresses(List.of())
                    .build();
            
            return ResponseEntity.ok(basicResponse);
        }
    }

    @PutMapping("/me")
    @Operation(
        summary = "Update current user profile",
        description = "Update the profile information for the authenticated user.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    public ResponseEntity<UserProfileResponse> updateCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserRequest request) {
        
        log.info("Updating current user profile for user: {}", userDetails.getUsername());
        
        try {
            UserProfileResponse response = userService.updateProfile(userDetails.getUser().getId(), request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating user profile: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/me/avatar")
    @Operation(
        summary = "Update user avatar",
        description = "Update the avatar for the authenticated user.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Avatar updated successfully",
                content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    public ResponseEntity<UserProfileResponse> updateAvatar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("avatar") MultipartFile file) {
        
        log.info("Updating avatar for user: {}", userDetails.getUsername());
        
        try {
            UserProfileResponse response = userService.updateAvatar(userDetails.getUser().getId(), file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating avatar: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/me/basic-info")
    @Operation(
        summary = "Update basic user info",
        description = "Update basic information for the authenticated user.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Basic info updated successfully",
                content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    public ResponseEntity<UserProfileResponse> updateBasicInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserRequest request) {
        
        log.info("Updating basic info for user: {}", userDetails.getUsername());
        
        try {
            UserProfileResponse response = userService.updateBasicInfo(userDetails.getUser().getId(), request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating basic info: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/me/professional-info")
    @Operation(
        summary = "Update professional user info",
        description = "Update professional information for the authenticated user.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Professional info updated successfully",
                content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    public ResponseEntity<UserProfileResponse> updateProfessionalInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserRequest request) {
        
        log.info("Updating professional info for user: {}", userDetails.getUsername());
        
        try {
            UserProfileResponse response = userService.updateProfessionalInfo(userDetails.getUser().getId(), request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating professional info: {}", e.getMessage(), e);
            throw e;
        }
    }
}
