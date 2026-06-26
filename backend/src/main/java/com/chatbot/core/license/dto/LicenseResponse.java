package com.chatbot.core.license.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseResponse {
    
    private Long id;
    private String planName;
    private Boolean isActive;
    private Instant expiresAt;
    private List<String> features;
    private List<String> modules;
    private Map<String, Integer> limits;
    private Instant createdAt;
    private Instant updatedAt;
    
    // JWT compatible fields for local app
    private Long exp; // Unix timestamp for expiration
    private String sub; // User ID as string
    private String email; // User email
    
    // Metadata fields
    private Integer schemaVersion;
    private String licenseId;
    
    public static LicenseResponse from(com.chatbot.core.license.model.License license, String userEmail) {
        return LicenseResponse.builder()
                .id(license.getId())
                .planName(license.getPlanName())
                .isActive(license.getIsActive())
                .expiresAt(license.getExpiresAt())
                .features(license.getFeatures() != null ? new ArrayList<>(license.getFeatures()) : null)
                .modules(license.getModules() != null ? new ArrayList<>(license.getModules()) : null)
                .limits(license.getLimits() != null ? new HashMap<>(license.getLimits()) : null)
                .createdAt(license.getCreatedAt())
                .updatedAt(license.getUpdatedAt())
                .exp(license.getExpiresAt() != null ? license.getExpiresAt().getEpochSecond() : null)
                .sub(license.getUserId().toString()) // Application-level join: use userId instead of User object
                .email(userEmail)
                .schemaVersion(1)
                .licenseId(null) // Not available in the current database schema
                .build();
    }
}
