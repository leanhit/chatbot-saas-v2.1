// AuthController.java
package com.chatbot.modules.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatbot.modules.auth.dto.*;
import com.chatbot.modules.auth.security.CustomUserDetails;
import com.chatbot.modules.auth.service.AuthService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        log.info("Received registration request for email: {}", request.getEmail());
        try {
            log.debug("Starting user registration process...");
            UserResponse response = authService.register(request);
            log.info("User registered successfully with email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Registration failed for email: {}", request.getEmail(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        UserResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<UserResponse> changePassword(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody ChangePasswordRequest request) {
        
        // Kiểm tra khớp mật khẩu mới ở tầng Controller để giảm tải cho Service
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Xác nhận mật khẩu mới không khớp");
        }

        UserResponse response = authService.changePassword(currentUser.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-role")
    @PreAuthorize("hasRole('ADMIN')") // Chốt chặn bảo mật quan trọng nhất
    public ResponseEntity<UserDto> changeRole(@RequestBody ChangeRoleRequest request) {
        UserDto updatedUser = authService.changeRole(request.getUserId(), request.getNewRole());
        return ResponseEntity.ok(updatedUser);
    }
}
