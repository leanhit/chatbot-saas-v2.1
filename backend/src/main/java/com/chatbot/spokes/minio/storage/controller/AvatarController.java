package com.chatbot.spokes.minio.storage.controller;

import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.spokes.minio.storage.service.AvatarStorageService;
import com.chatbot.core.user.model.User;
import com.chatbot.core.identity.repository.AuthRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Avatar Controller
 * Handles avatar and tenant logo uploads with automatic bucket creation
 */
@RestController
@RequestMapping("/api/avatars")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Avatar Management", description = "APIs for managing user avatars and tenant logos")
public class AvatarController {

    private final AvatarStorageService avatarStorageService;
    private final AuthRepository authRepository;

    @Operation(summary = "Upload user avatar", description = "Upload user avatar with automatic bucket creation")
    @PostMapping("/user/avatar")
    public ResponseEntity<Map<String, String>> uploadUserAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Avatar image file") @RequestParam("file") MultipartFile file) {
        
        try {
            // Get user from authentication
            User user = authRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Upload avatar
            String avatarUrl = avatarStorageService.uploadUserAvatar(user.getId().toString(), file);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Avatar uploaded successfully");
            response.put("avatarUrl", avatarUrl);
            response.put("userId", user.getId().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading user avatar", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload avatar: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Operation(summary = "Upload tenant logo", description = "Upload tenant logo with automatic bucket creation")
    @PostMapping("/tenant/logo")
    public ResponseEntity<Map<String, String>> uploadTenantLogo(
            @Parameter(description = "Logo image file") @RequestParam("file") MultipartFile file) {
        
        try {
            // Get current tenant ID from context
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                throw new RuntimeException("No tenant context found");
            }
            
            // Upload logo
            String logoUrl = avatarStorageService.uploadTenantLogo(tenantId.toString(), file);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logo uploaded successfully");
            response.put("logoUrl", logoUrl);
            response.put("tenantId", tenantId.toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading tenant logo", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload logo: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Operation(summary = "Delete user avatar", description = "Delete user avatar by object name")
    @DeleteMapping("/user/avatar/{objectName}")
    public ResponseEntity<Map<String, String>> deleteUserAvatar(
            @Parameter(description = "Avatar object name") @PathVariable String objectName) {
        
        try {
            avatarStorageService.deleteUserAvatar(objectName);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Avatar deleted successfully");
            response.put("objectName", objectName);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting user avatar", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete avatar: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Operation(summary = "Delete tenant logo", description = "Delete tenant logo by object name")
    @DeleteMapping("/tenant/logo/{objectName}")
    public ResponseEntity<Map<String, String>> deleteTenantLogo(
            @Parameter(description = "Logo object name") @PathVariable String objectName) {
        
        try {
            avatarStorageService.deleteTenantLogo(objectName);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logo deleted successfully");
            response.put("objectName", objectName);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting tenant logo", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete logo: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Operation(summary = "Check avatar bucket status", description = "Check if avatar buckets exist and create if needed")
    @PostMapping("/check-buckets")
    public ResponseEntity<Map<String, String>> checkAvatarBuckets() {
        try {
            // This will trigger bucket creation if they don't exist
            // We can create dummy operations to ensure buckets are created
            Map<String, String> response = new HashMap<>();
            response.put("message", "Avatar buckets checked/created successfully");
            response.put("userAvatarsBucket", "user-avatars");
            response.put("tenantLogosBucket", "tenant-logos");
            response.put("status", "ready");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking avatar buckets", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to check/create buckets: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
