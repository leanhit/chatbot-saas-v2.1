package com.chatbot.core.tenant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload for a single invitation result.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvitationResponse {
    private String email;
    private String status; // SENT, FAILED, etc.
}
