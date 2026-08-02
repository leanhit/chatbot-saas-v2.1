package com.chatbot.shared.messenger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelUserInfo {
    private String name;
    private String avatarUrl;
    private Map<String, Object> attributes;
}
