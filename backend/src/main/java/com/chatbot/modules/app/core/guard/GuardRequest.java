package com.chatbot.modules.app.core.guard;

import com.chatbot.modules.app.core.model.AppCode;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Request object for App Service Guard
 */
@Data
@Builder
public class GuardRequest {
    
    private UUID tenantId;
    private UUID userId;
    private AppCode appCode;
    private String action;
    
    public static GuardRequest of(UUID tenantId, UUID userId, AppCode appCode, String action) {
        return GuardRequest.builder()
                .tenantId(tenantId)
                .userId(userId)
                .appCode(appCode)
                .action(action)
                .build();
    }
}
