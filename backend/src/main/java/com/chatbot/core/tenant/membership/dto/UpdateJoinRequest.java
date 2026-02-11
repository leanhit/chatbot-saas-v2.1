package com.chatbot.core.tenant.membership.dto;

import com.chatbot.core.tenant.membership.model.MembershipStatus;
import lombok.Getter;

@Getter
public class UpdateJoinRequest {
    private MembershipStatus status; // ACTIVE | REJECTED
}
