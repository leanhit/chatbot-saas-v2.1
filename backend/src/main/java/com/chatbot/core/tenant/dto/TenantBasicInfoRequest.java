package com.chatbot.core.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.model.TenantVisibility;
import com.chatbot.shared.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSetter;

@Data
public class TenantBasicInfoRequest {
    
    @NotBlank(message = "Tenant name không được để trống")
    @Size(min = 2, max = 100, message = "Tenant name phải từ 2-100 ký tự")
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
                // Use DateUtils for consistent datetime parsing
                this.expiresAtInstant = DateUtils.parseInstant(expiresAtString, DateUtils.API_DATETIME_FORMAT);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid datetime format. Expected: " + DateUtils.API_DATETIME_FORMAT + ", got: " + expiresAtString);
            }
        }
    }
}