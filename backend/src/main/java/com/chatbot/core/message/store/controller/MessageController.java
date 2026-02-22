package com.chatbot.core.message.store.controller;

import com.chatbot.core.message.store.dto.MessageDTO;
import com.chatbot.core.message.store.dto.MessageSendRequest;
import com.chatbot.core.message.store.model.Message;
import com.chatbot.core.message.store.mapper.MessageMapper;
import com.chatbot.core.message.store.service.MessageService;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.responses.ApiResponse; // Use fully qualified name to avoid conflict
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messaging", description = "Message routing and processing operations")
public class MessageController {

    private final MessageService messageService;

    // GET /api/messages?conversationId=...
    @GetMapping
    @Operation(
        summary = "Get messages by conversation",
        description = "Retrieve paginated messages for a specific conversation. Messages are marked as 'mine' if sent by the bot.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Messages retrieved successfully",
                content = @Content(schema = @Schema(implementation = Page.class)))
        }
    )
    public Page<MessageDTO> list(
            @Parameter(description = "Conversation ID", required = true)
            @RequestParam Long conversationId,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "30") int size
    ) {
        // Ánh xạ tin nhắn: isMine=true nếu người gửi là "bot"
        return messageService.getMessages(conversationId, page, size)
                .map(m -> MessageMapper.toDTO(m, "bot".equals(m.getSender())));
    }

    // POST /api/messages/send
    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody MessageSendRequest req) {
        try {
            // Cần truyền đủ các trường mới cho service
            Message msg = messageService.saveMessage(
                    req.getConversationId(),
                    req.getSender(),
                    req.getContent(),
                    req.getMessageType(),
                    null // rawPayload: Vì request chỉ chứa nội dung đơn giản, ta truyền null.
                );

            return ResponseEntity.ok(MessageMapper.toDTO(msg, "bot".equals(req.getSender())));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}