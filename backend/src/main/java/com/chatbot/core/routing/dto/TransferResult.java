package com.chatbot.core.routing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResult {
    private Long conversationId;
    private boolean transferred;
    private Long fromAgentId;
    private Long toAgentId;
    private Long transferId;
    private String transferType;
    private String status;
    private String reason;
}
