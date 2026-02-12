package com.chatbot.core.user.dto;

import com.chatbot.modules.address.dto.AddressDetailResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Full User Response DTO - Include profile and addresses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFullResponse {
    private Long id;
    private String email;
    private String systemRole;
    private Boolean isActive;
    
    private UserProfile profile;
    private List<AddressDetailResponseDTO> addresses;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfile {
        private Long userId;
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
}
