package com.chatbot.core.membership.dto;

import com.chatbot.core.membership.model.MembershipStatus;
import lombok.Getter;

@Getter
public class UpdateJoinRequest {
    private MembershipStatus status; // ACTIVE | REJECTED
}
