package com.chatbot.core.tenant.dto;

import lombok.Data;
import java.time.LocalDateTime;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.model.TenantVisibility;
import com.chatbot.shared.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class TenantBasicInfoRequest {
    private String name;
    private TenantStatus status; // Enum hoặc String tùy logic của bạn
    private TenantVisibility visibility;
    
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime expiresAt; // Hoặc LocalDate
}