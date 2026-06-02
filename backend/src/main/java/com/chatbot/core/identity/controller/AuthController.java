package com.chatbot.core.identity.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.chatbot.core.identity.dto.*;
import com.chatbot.core.identity.security.CustomUserDetails;
import com.chatbot.core.identity.service.AuthService;
import com.chatbot.core.identity.service.JwtService;
import com.chatbot.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping({"/api/auth", "/api/api/auth"})
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Identity and authentication management APIs")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    @Operation(
        summary = "Register new user",
        description = "Register a new user with email and password. Returns user information and JWT token.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User registered successfully",
                content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already exists")
        })
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Parameter(description = "User registration details", required = true)
            @RequestBody RegisterRequest request) {
        log.info("Received registration request for email: {}", request.getEmail());
        UserResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Registration successful"));
    }

    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticate user with email and password. Returns user information and JWT token.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful",
                content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
        })
    public ResponseEntity<ApiResponse<UserResponse>> login(
            @Parameter(description = "Login credentials", required = true)
            @RequestBody LoginRequest request) {
        UserResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/change-password")
    @Operation(
        summary = "Change user password",
        description = "Change password for authenticated user. Requires current password for verification.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed successfully",
                content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid current password or passwords don't match"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
        })
    public ResponseEntity<ApiResponse<UserResponse>> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "user") CustomUserDetails currentUser,
            @Parameter(description = "Password change details", required = true)
            @RequestBody ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password confirmation does not match");
        }
        UserResponse response = authService.changePassword(currentUser.getUser().getEmail(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Password changed successfully"));
    }

    @PutMapping("/change-role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Change user role",
        description = "Change the role of a user. Only accessible by administrators.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role changed successfully",
                content = @Content(schema = @Schema(implementation = UserDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
        })
    public ResponseEntity<ApiResponse<UserDto>> changeRole(
            @Parameter(description = "Role change details", required = true)
            @RequestBody ChangeRoleRequest request) {
        UserDto updatedUser = authService.changeRole(request.getUserId(), request.getNewRole());
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Role changed successfully"));
    }

    @PostMapping("/refresh-token")
    @Operation(
        summary = "Refresh access token",
        description = "Generate new access token using refresh token",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
        })
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(
            @Parameter(description = "Refresh token", required = true)
            @RequestBody RefreshTokenRequest request) {
        TokenRefreshResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Revoke refresh token and logout user")
    public ResponseEntity<ApiResponse<String>> logout(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "user") CustomUserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User not authenticated"));
        }
        authService.logout(currentUser.getUser().getEmail());
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }

    @PostMapping("/logout-simple")
    @Operation(summary = "Logout user (alternative)", description = "Logout user using token from Authorization header")
    public ResponseEntity<ApiResponse<String>> logoutSimple(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtService.extractEmail(token);
                authService.logout(email);
                return ResponseEntity.ok(ApiResponse.success("Logout successful"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid authorization header"));
            }
        } catch (Exception e) {
            log.error("Simple logout failed", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Logout failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logout-by-token")
    @Operation(summary = "Logout by access token", description = "Revoke specific access token")
    public ResponseEntity<ApiResponse<String>> logoutByToken(
            @Parameter(description = "Access token to revoke", required = true)
            @RequestBody Map<String, String> request) {
        String token = request.get("token");
        authService.logoutByToken(token);
        return ResponseEntity.ok(ApiResponse.success("Token revoked successfully"));
    }
}
