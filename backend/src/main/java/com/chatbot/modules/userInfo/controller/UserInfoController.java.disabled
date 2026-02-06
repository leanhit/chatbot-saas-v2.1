package com.chatbot.modules.userInfo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chatbot.modules.userInfo.dto.UserInfoRequest;
import com.chatbot.modules.userInfo.dto.UserInfoResponse;
import com.chatbot.modules.userInfo.service.UserInfoService;

@RestController
@RequestMapping("/api/v1/user-info")
@RequiredArgsConstructor
public class UserInfoController {
    private final UserInfoService userInfoService;

    @GetMapping("/me/{userId}")
    public ResponseEntity<UserInfoResponse> getMyProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userInfoService.getProfile(userId));
    }

    @PutMapping("/me/{userId}")
    public ResponseEntity<UserInfoResponse> updateProfile(
            @PathVariable Long userId, 
            @RequestBody UserInfoRequest request) {
        return ResponseEntity.ok(userInfoService.updateProfile(userId, request));
    }
}