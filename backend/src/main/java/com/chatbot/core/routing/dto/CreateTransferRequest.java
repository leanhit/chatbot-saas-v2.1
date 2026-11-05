package com.chatbot.core.routing.dto;

import com.chatbot.core.routing.model.ConversationTransfer;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransferRequest {
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    @NotNull(message = "From agent ID is required")
    private Long fromAgentId;

    @NotNull(message = "To agent ID is required")
    private Long toAgentId;

    private String transferReason;

    @Builder.Default
    private ConversationTransfer.TransferType transferType = ConversationTransfer.TransferType.MANUAL;

    private String notes;

    private Long transferredBy;
}
