package com.chatbot.modules.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword; // Dùng để kiểm tra khớp mật khẩu ở phía Client/Server
}