package com.chatbot.modules.userInfo.dto;

import lombok.Data;

@Data
public class UserInfoRequest {
    private String fullName;
    private String phoneNumber;
    private String avatar;
    private String bio;
}