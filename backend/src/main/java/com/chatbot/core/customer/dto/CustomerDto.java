package com.chatbot.core.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private String psid;
    private String displayName;
    private String displayAvatar;
    private String primaryPhone;
    private String email;
    private Integer totalPhones;
    private String status;
    private LocalDateTime updatedAt;
}
