package com.chatbot.modules.userInfo.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BasicInfoRequest {
    @Size(min = 2, max = 50, message = "Full name must be between 2 and 50 characters")
    private String fullName;
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;
    
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
    
    private String gender;
}
