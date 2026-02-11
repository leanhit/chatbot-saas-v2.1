package com.chatbot.modules.facebook.connection.dto;

import com.chatbot.modules.facebook.connection.model.ChatbotProvider;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

@Data
public class CreateFacebookConnectionRequest {
    private String botId;
    private String botName;
    private String pageId;
    private String fanpageUrl;
    private String pageAccessToken;
    private boolean isEnabled; // Thêm trường này để tạo kết nối ban đầu
    
    private ChatbotProvider chatbotProvider = ChatbotProvider.BOTPRESS; // Mặc định là BOTPRESS
}