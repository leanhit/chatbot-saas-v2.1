// AuthController.java
package com.chatbot.core.identity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.chatbot.core.identity.dto.*;
import com.chatbot.core.identity.security.CustomUserDetails;
import com.chatbot.core.identity.service.AuthService;
import com.chatbot.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.responses.ApiResponse; // Use fully qualified name to avoid conflict
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Identity and authentication management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Transactional
    @Operation(
        summary = "Register new user",
        description = "Register a new user with email and password. Returns user information and JWT token.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User registered successfully",
                content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already exists")
        }
    )
    public ResponseEntity<UserResponse> register(
            @Parameter(description = "User registration details", required = true)
            @RequestBody RegisterRequest request) {
        log.info("Received registration request for email: {}", request.getEmail());
        try {
            log.debug("Starting user registration process...");
            UserResponse response = authService.register(request);
            log.info("User registered successfully with email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Registration failed for email: {}. Error: {}", request.getEmail(), e.getMessage(), e);
            throw e; // Re-throw to let the global exception handler handle it
        }
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
        }
    )
    public ResponseEntity<UserResponse> login(
            @Parameter(description = "Login credentials", required = true)
            @RequestBody LoginRequest request) {
        UserResponse response = authService.login(request);
        return ResponseEntity.ok(response);
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
        }
    )
    public ResponseEntity<UserResponse> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "user") CustomUserDetails currentUser,
            @Parameter(description = "Password change details", required = true)
            @RequestBody ChangePasswordRequest request) {
        
        // Kiểm tra khớp mật khẩu mới ở tầng Controller để giảm tải cho Service
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Xác nhận mật khẩu mới không khớp");
        }

        UserResponse response = authService.changePassword(currentUser.getUser().getEmail(), request);
        return ResponseEntity.ok(response);
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
        }
    )
    public ResponseEntity<UserDto> changeRole(
            @Parameter(description = "Role change details", required = true)
            @RequestBody ChangeRoleRequest request) {
        UserDto updatedUser = authService.changeRole(request.getUserId(), request.getNewRole());
        return ResponseEntity.ok(updatedUser);
    }

}
