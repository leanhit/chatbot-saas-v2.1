package com.chatbot.modules.tenant.membership.dto;

import com.chatbot.modules.tenant.membership.model.MembershipStatus;
import lombok.Getter;

@Getter
public class UpdateJoinRequest {
    private MembershipStatus status; // ACTIVE | REJECTED
}
