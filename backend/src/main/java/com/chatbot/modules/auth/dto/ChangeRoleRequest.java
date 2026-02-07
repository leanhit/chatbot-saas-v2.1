package com.chatbot.modules.auth.dto;

import com.chatbot.modules.auth.model.SystemRole;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
public class ChangeRoleRequest {
    private UUID userId;
    private SystemRole newRole;
}