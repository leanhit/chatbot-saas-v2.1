package com.chatbot.modules.userInfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String email; // Lấy từ Auth entity thông qua UserInfo
    private String fullName;
    private String phoneNumber;
    private String avatar;
    private String gender;
    private String bio;
}