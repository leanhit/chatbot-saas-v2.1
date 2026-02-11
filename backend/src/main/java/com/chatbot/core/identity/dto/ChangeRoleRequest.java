package com.chatbot.core.identity.dto;

import com.chatbot.core.identity.model.SystemRole;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeRoleRequest {
    private Long userId;
    private SystemRole newRole;
}