package com.chatbot.modules.userInfo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatbot.modules.userInfo.dto.UserInfoRequest;
import com.chatbot.modules.userInfo.dto.UserInfoResponse;
import com.chatbot.modules.userInfo.model.UserInfo;
import com.chatbot.modules.userInfo.repository.UserInfoRepository;

@Service
@RequiredArgsConstructor
public class UserInfoService {
    private final UserInfoRepository userInfoRepository;

    public UserInfoResponse getProfile(Long userId) {
        UserInfo profile = userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        return mapToResponse(profile);
    }

    @Transactional
    public UserInfoResponse updateProfile(Long userId, UserInfoRequest request) {
        UserInfo profile = userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        profile.setFullName(request.getFullName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setAvatar(request.getAvatar());
        profile.setBio(request.getBio());
        // Nếu UserInfoRequest có thêm gender, hãy set ở đây
        
        UserInfo updatedProfile = userInfoRepository.save(profile);
        return mapToResponse(updatedProfile);
    }

    private UserInfoResponse mapToResponse(UserInfo userInfo) {
        return UserInfoResponse.builder()
                .id(userInfo.getId())
                .email(userInfo.getAuth() != null ? userInfo.getAuth().getEmail() : null)
                .fullName(userInfo.getFullName())
                .phoneNumber(userInfo.getPhoneNumber())
                .avatar(userInfo.getAvatar())
                .gender(userInfo.getGender())
                .bio(userInfo.getBio())
                .build();
    }
}