package com.chatbot.core.tenant.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

/**
 * DTO để cập nhật thông tin liên hệ của tenant (contact email, phone, website).
 */
@Data
public class TenantContactInfoRequest {

    @Email(message = "Email không đúng định dạng")
    private String email;

    private String phone;

    private String website;
}
