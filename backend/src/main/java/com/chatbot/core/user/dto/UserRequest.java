package com.chatbot.core.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Request DTO - For updating user profile
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private String fullName;
    private String phoneNumber;
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
