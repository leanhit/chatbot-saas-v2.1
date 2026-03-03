package com.chatbot.core.tenant.dto;

import lombok.Data;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.model.TenantVisibility;
import com.chatbot.shared.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSetter;

@Data
public class TenantBasicInfoRequest {
    private String name;
    private TenantStatus status; // Enum hoặc String tùy logic của bạn
    private TenantVisibility visibility;
    
    // Sử dụng Instant với format chuẩn API để handle ISO 8601 với timezone tự động
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT)
    private Instant expiresAtInstant;
    
    // Getter/Setter để chuyển đổi Instant -> LocalDateTime cho service layer
    public LocalDateTime getExpiresAt() {
        return expiresAtInstant != null ? 
            LocalDateTime.ofInstant(expiresAtInstant, ZoneId.systemDefault()) : null;
    }
    
    @JsonSetter("expiresAt")
    public void setExpiresAt(String expiresAtString) {
        if (expiresAtString != null && !expiresAtString.trim().isEmpty()) {
            try {
                // Try parsing as ISO 8601 Instant (preferred)
                this.expiresAtInstant = Instant.parse(expiresAtString);
            } catch (Exception e) {
                // Fallback: try parsing as LocalDateTime without timezone
                try {
                    LocalDateTime localDateTime = LocalDateTime.parse(expiresAtString.replace("Z", ""));
                    this.expiresAtInstant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
                } catch (Exception e2) {
                    throw new IllegalArgumentException("Invalid datetime format. Expected ISO 8601 format: " + expiresAtString);
                }
            }
        }
    }
}