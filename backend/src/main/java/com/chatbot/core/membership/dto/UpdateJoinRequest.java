package com.chatbot.core.membership.dto;

import com.chatbot.core.membership.model.MembershipStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJoinRequest {
    private MembershipStatus status; // ACTIVE | REJECTED | APPROVED
}
