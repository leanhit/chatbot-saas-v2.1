package com.chatbot.spokes.facebook.autoconnect.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionError {
    private String pageName;
    private String errorMessage;
}