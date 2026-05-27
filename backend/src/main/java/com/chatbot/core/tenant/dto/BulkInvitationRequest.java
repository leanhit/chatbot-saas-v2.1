package com.chatbot.core.tenant.dto;

import java.util.List;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * Request payload for bulk tenant invitations.
 */
@Data
public class BulkInvitationRequest {
    @NotEmpty(message = "Invitation list must not be empty")
    private List<Invitation> invitations;

    @Data
    public static class Invitation {
        @Email(message = "Invalid email address")
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Role is required")
        private String role; // Expected values: OWNER, ADMIN, MEMBER etc.
    }
}
