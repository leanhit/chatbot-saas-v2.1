package com.chatbot.modules.auth.dto;

import com.chatbot.modules.auth.model.SystemRole;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeRoleRequest {
    private Long userId;
    private SystemRole newRole;
}