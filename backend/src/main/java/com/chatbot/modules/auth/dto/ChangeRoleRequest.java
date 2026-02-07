package com.chatbot.modules.auth.dto;

import com.chatbot.modules.auth.model.SystemRole;
import lombok.*;

@Getter
@Setter
public class ChangeRoleRequest {
    private Long userId;
    private SystemRole newRole;
}