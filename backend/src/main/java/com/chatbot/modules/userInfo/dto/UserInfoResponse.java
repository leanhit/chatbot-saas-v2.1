package com.chatbot.modules.userInfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String email; // Lấy từ Auth entity thông qua UserInfo
    private String fullName;
    private String phoneNumber;
    private String avatar;
    private String gender;
    private String bio;
    
    // Professional fields
    private String jobTitle;
    private String department;
    private String company;
    private String linkedinUrl;
    private String website;
    private String location;
    private String skills;
    private String experience;
    private String education;
    private String certifications;
    private String languages;
    private String availability;
    private String hourlyRate;
    private String portfolioUrl;
}