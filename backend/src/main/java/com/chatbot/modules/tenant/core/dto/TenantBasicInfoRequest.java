package com.chatbot.modules.tenant.core.dto;

import lombok.Data;
import java.time.LocalDateTime;
import com.chatbot.modules.tenant.core.model.TenantStatus;
import com.chatbot.modules.tenant.core.model.TenantVisibility;

@Data
public class TenantBasicInfoRequest {
    private String name;
    private TenantStatus status; // Enum hoặc String tùy logic của bạn
    private TenantVisibility visibility;
    private LocalDateTime expiresAt; // Hoặc LocalDate
}