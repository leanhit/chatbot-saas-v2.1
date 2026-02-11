package com.chatbot.modules.userInfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.chatbot.modules.address.dto.AddressDetailResponseDTO;
import com.chatbot.core.identity.model.SystemRole;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFullResponse {
    private Long id;
    private String email;
    private SystemRole systemRole;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Professional profile info (like tenant)
    private UserProfile profile;
    
    // Nested addresses (like tenant)
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
}
