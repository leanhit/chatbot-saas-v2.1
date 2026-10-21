package com.chatbot.core.message.store.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Request DTO for Agent update operations
 * Includes validation annotations to prevent invalid data injection
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRequestDTO {

    @NotBlank(message = "Agent name cannot be empty")
    @Size(min = 2, max = 100, message = "Agent name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Agent email cannot be empty")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotNull(message = "Agent role cannot be null")
    private String role; // Will be validated against AgentRole enum in service layer

    private String status; // Will be validated against AgentStatus enum in service layer

    @Min(value = 0, message = "Current load cannot be negative")
    private Integer currentLoad;

    @NotNull(message = "Max concurrent conversations cannot be null")
    @Min(value = 1, message = "Max concurrent conversations must be at least 1")
    @Max(value = 100, message = "Max concurrent conversations cannot exceed 100")
    private Integer maxConcurrentConversations;

    private Set<String> skills;

    private String assignmentPreferences;

    private Boolean active;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Pattern(regexp = "^[+]?[0-9\\-\\s]+$", message = "Invalid phone number format")
    private String phoneNumber;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    @Pattern(regexp = "^https?://.*", message = "Avatar URL must be a valid HTTP/HTTPS URL")
    private String avatarUrl;
}
