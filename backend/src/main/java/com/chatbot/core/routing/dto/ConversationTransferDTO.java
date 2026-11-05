package com.chatbot.core.routing.dto;

import com.chatbot.core.routing.model.ConversationTransfer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationTransferDTO {
    private Long id;
    private Long tenantId;
    private Long conversationId;
    private Long fromAgentId;
    private Long toAgentId;
    private String transferReason;
    private ConversationTransfer.TransferType transferType;
    private ConversationTransfer.TransferStatus status;
    private String notes;
    private Long transferredBy;
    private LocalDateTime acceptedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
