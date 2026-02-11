package com.chatbot.modules.userInfo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.chatbot.modules.userInfo.dto.UserInfoRequest;
import com.chatbot.modules.userInfo.dto.BasicInfoRequest;
import com.chatbot.modules.userInfo.dto.ProfessionalInfoRequest;
import com.chatbot.modules.userInfo.dto.UserInfoResponse;
import com.chatbot.modules.userInfo.dto.UserFullResponse;
import com.chatbot.modules.userInfo.service.UserInfoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.chatbot.core.identity.security.CustomUserDetails;
import com.chatbot.core.identity.model.Auth;
import com.chatbot.core.identity.repository.AuthRepository;

@RestController
@RequestMapping("/api/v1/user-info")
@RequiredArgsConstructor
public class UserInfoController {
    private final UserInfoService userInfoService;
    private final AuthRepository authRepository;

    @GetMapping("/me")
    public ResponseEntity<UserFullResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Auth auth = authRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(userInfoService.getFullProfile(auth.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserInfoResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UserInfoRequest request) {
        Auth auth = authRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(userInfoService.updateProfile(auth.getId(), request));
    }

    @PutMapping("/me/avatar")
    public ResponseEntity<UserInfoResponse> updateAvatar(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam("avatar") MultipartFile file) {
        Auth auth = authRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(userInfoService.updateAvatar(auth.getId(), file));
    }

    // 1. Update Basic Info Only - Tách biệt không gộp chung
    @PutMapping("/me/basic-info")
    public ResponseEntity<UserInfoResponse> updateBasicInfo(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody BasicInfoRequest request) {
        Auth auth = authRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(userInfoService.updateBasicInfo(auth.getId(), request));
    }

    // 2. Update Professional Info Only - Tách biệt không gộp chung
    @PutMapping("/me/professional-info")
    public ResponseEntity<UserInfoResponse> updateProfessionalInfo(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody ProfessionalInfoRequest request) {
        Auth auth = authRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(userInfoService.updateProfessionalInfo(auth.getId(), request));
    }

    // 3. Update Address Only - Đã có sẵn trong AddressController
    // Endpoints: PUT /api/addresses/{id}, POST /api/addresses
}