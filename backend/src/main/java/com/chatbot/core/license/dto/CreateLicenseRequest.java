package com.chatbot.core.license.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateLicenseRequest {
    
    @NotNull(message = "User ID cannot be null")
    private Long userId;
    
    @NotBlank(message = "Plan name cannot be blank")
    private String planName;
    
    @Builder.Default
    private Boolean isActive = true;
    
    private Instant expiresAt;
    
    private List<String> features;
    
    private List<String> modules;
    
    private Map<String, Integer> limits;
}
