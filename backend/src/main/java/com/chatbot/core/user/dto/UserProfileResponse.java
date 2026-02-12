package com.chatbot.core.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Profile Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String avatar;
    private String gender;
    private String bio;
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
