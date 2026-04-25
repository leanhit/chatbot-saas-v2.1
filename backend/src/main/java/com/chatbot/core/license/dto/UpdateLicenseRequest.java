package com.chatbot.core.license.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLicenseRequest {
    
    private String planName;
    
    private Boolean isActive;
    
    private Instant expiresAt;
    
    private List<String> features;
    
    private List<String> modules;
    
    private Map<String, Integer> limits;
}
