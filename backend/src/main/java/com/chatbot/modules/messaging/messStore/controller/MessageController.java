package com.chatbot.modules.messaging.messStore.controller;

import com.chatbot.modules.messaging.messStore.dto.MessageDTO;
import com.chatbot.modules.messaging.messStore.dto.MessageSendRequest;
import com.chatbot.modules.messaging.messStore.model.Message;
import com.chatbot.modules.messaging.messStore.mapper.MessageMapper;
import com.chatbot.modules.messaging.messStore.service.MessageService;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // GET /api/messages?conversationId=...
    @GetMapping
    public Page<MessageDTO> list(
            @RequestParam Long conversationId,
            @RequestParam(defaultValue = "0") int page,
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