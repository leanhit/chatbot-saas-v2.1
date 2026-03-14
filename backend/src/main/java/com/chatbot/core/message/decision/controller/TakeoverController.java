package com.chatbot.core.message.decision.controller;

import com.chatbot.core.message.decision.model.TakeoverMessage;
import com.chatbot.core.message.decision.service.TakeoverService;
import com.chatbot.core.message.decision.websocket.TakeoverWebSocketHandler;
import com.chatbot.core.message.store.service.MessageService;
import com.chatbot.core.message.store.service.ConversationService;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.router.service.AgentMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/takeover")
@RequiredArgsConstructor
@Slf4j
public class TakeoverController {

    private final TakeoverService takeoverService;
    private final TakeoverWebSocketHandler websocketHandler;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final AgentMessageService agentMessageService;
    private final ConversationService conversationService;
    private final ConversationRepository conversationRepo;

    // UI gửi tin nhắn → lưu DB (lâu dài) → push WebSocket (real-time)
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody TakeoverMessage message) {
        try {
            message.setTimestamp(System.currentTimeMillis());
            
            String conversationIdStr = message.getConversationId();
            Long conversationIdLong = Long.parseLong(conversationIdStr);

            // 1. Lưu message từ agent vào database (trước khi gửi)
            try {
                messageService.saveMessage(
                    conversationIdLong, 
                    "agent", 
                    message.getContent(), 
                    "TEXT", 
                    null
                );
                log.info("💾 [Takeover] Saved agent message to DB. Conversation ID: {}", conversationIdLong);
            } catch (Exception e) {
                log.error("❌ [Takeover] Error saving agent message to DB: {}", e.getMessage(), e);
            }

            // 2. Gửi tin nhắn qua AgentMessageService (sẽ xử lý gửi đến Facebook)
            agentMessageService.sendAgentTextMessage(
                conversationIdLong, 
                message.getContent(), 
                null // agentId đang là null, có thể cần lấy từ context sau này
            );
            
            // 3. Push WebSocket cho real-time (hiển thị cho các agent khác)
            websocketHandler.sendToConversation(conversationIdStr, message);
            
            return ResponseEntity.ok().body("{\"message\": \"Message sent successfully\"}");
            
        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("{\"error\": \"Failed to send message: " + e.getMessage() + "\"}");
        }
    }

    // Lấy lịch sử tin nhắn
    @GetMapping("/history/{conversationId}")
    public List<TakeoverMessage> getHistory(@PathVariable String conversationId) {
        return takeoverService.getMessages(conversationId)
                .stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, TakeoverMessage.class);
                    } catch (Exception e) {
                        e.printStackTrace(); 
                        return null;
                    }
                })
                .filter(msg -> msg != null)
                .collect(Collectors.toList());
    }

    // --------------------------------------------------------------------------
    // ENDPOINT MỚI: TAKEOVER CONVERSATION
    // --------------------------------------------------------------------------
    @PostMapping("/take/{conversationId}")
    public ResponseEntity<?> takeOverConversation(@PathVariable Long conversationId) {
        try {
            // 1. Use updateTakenOverStatus method which exists and works
            // We need to get the conversation first to get the ownerId for permission check
            Conversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
            
            conversationService.updateTakenOverStatus(conversationId, true, conversation.getOwnerId());
            
            // 2. Gửi thông báo qua WebSocket về tất cả sessions
            TakeoverMessage takeoverNotification = new TakeoverMessage(
                String.valueOf(conversationId),
                "system",
                "🔒 Conversation has been taken over by agent",
                System.currentTimeMillis()
            );
            websocketHandler.sendToConversation(String.valueOf(conversationId), takeoverNotification);
            
            log.info("🔒 Conversation {} taken over successfully", conversationId);
            return ResponseEntity.ok().body(Map.of(
                "message", "Conversation taken over successfully",
                "conversationId", conversationId,
                "isTakenOver", true,
                "takenAt", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("❌ Cannot takeover conversation: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot takeover conversation", e);
        }
    }

    // --------------------------------------------------------------------------
    // ENDPOINT MỚI: RELEASE CONVERSATION
    // --------------------------------------------------------------------------
    @PostMapping("/release/{conversationId}")
    public ResponseEntity<?> releaseConversation(@PathVariable Long conversationId) {
        try {
            // Logic để release conversation
            // Cần implement release logic trong service
            return ResponseEntity.ok().body("{\"message\": \"Conversation released successfully\"}");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot release conversation", e);
        }
    }

    // --------------------------------------------------------------------------
    // ENDPOINT MỚI: GET TAKEOVER STATUS
    // --------------------------------------------------------------------------
    @GetMapping("/status/{conversationId}")
    public ResponseEntity<?> getTakeoverStatus(@PathVariable Long conversationId) {
        try {
            // Logic để check takeover status
            // Cần implement status check logic trong service
            return ResponseEntity.ok().body("{\"isTakenOver\": false, \"agentId\": null}");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot get takeover status", e);
        }
    }

    // --------------------------------------------------------------------------
    // ENDPOINT MỚI: GET ACTIVE TAKEOVERS
    // --------------------------------------------------------------------------
    @GetMapping("/active")
    public ResponseEntity<?> getActiveTakeovers() {
        try {
            // Logic để get active takeovers
            // Cần implement active takeovers logic trong service
            return ResponseEntity.ok().body("[]");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot get active takeovers", e);
        }
    }
}