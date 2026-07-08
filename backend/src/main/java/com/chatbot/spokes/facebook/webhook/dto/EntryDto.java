package com.chatbot.spokes.facebook.webhook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntryDto {
    private String id;
    private Long time;
    private List<MessagingDto> messaging;
}
