package com.chatbot.modules.facebook.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacebookUserInfo {
    private String psid;
    private String name;
    private String profilePic;
    private String pageId; // Thêm trường này để xác định page mà user đang tương tác
    private Integer odooPartnerId;
    private LocalDateTime lastInteraction;
}
