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
import org.springframework.security.access.prepost.PreAuthorize;
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

    // UI gửi tin nhắn → centralized xử lý qua TakeoverService
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody TakeoverMessage message) {
        try {
            System.out.println("=== DEBUG TAKEOVER CONTROLLER RECEIVED AGENT MESSAGE ===");
            System.out.println("Message ID: " + message.getId());
            System.out.println("Conversation ID: " + message.getConversationId());
            System.out.println("Content: " + message.getContent());
            System.out.println("Sender: " + message.getSender());
            
            // Check for duplicate message (idempotency)
            if (message.getId() != null && messageService.messageExists(message.getId())) {
                System.out.println("=== TAKEOVER CONTROLLER: Message ALREADY PROCESSED, skipping ===");
                log.info("Message already processed: {}", message.getId());
                return ResponseEntity.ok().body("{\"message\": \"Message already processed\"}");
            }
            
            // Generate unique ID for the message if not provided
            if (message.getId() == null) {
                message.setId("agent_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000));
                System.out.println("Generated new Message ID: " + message.getId());
            }
            message.setTimestamp(System.currentTimeMillis());
            message.setSender("agent"); // Ensure sender is set to agent
            
            String conversationIdStr = message.getConversationId();
            Long conversationIdLong = Long.parseLong(conversationIdStr);

            System.out.println("=== TAKEOVER CONTROLLER: Calling TakeoverService ===");
            // 1. Centralized: Lưu vào DB + Gửi đến Facebook + Push WebSocket thông qua TakeoverService
            takeoverService.saveAndSendAgentMessage(message, conversationIdLong);
            
            log.info("✅ [Takeover] Agent message processed centrally. ID: {}, Conversation: {}", 
                message.getId(), conversationIdLong);
            
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
                "system_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000),
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
            // 1. Get conversation to check ownership
            Conversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
            
            // 2. Use updateTakenOverStatus method to release
            conversationService.updateTakenOverStatus(conversationId, false, conversation.getOwnerId());
            
            // 3. G thông báo qua WebSocket
            TakeoverMessage releaseNotification = new TakeoverMessage(
                "system_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000),
                String.valueOf(conversationId),
                "system",
                "Conversation has been released - bot can now respond",
                System.currentTimeMillis()
            );
            websocketHandler.sendToConversation(String.valueOf(conversationId), releaseNotification);
            
            log.info("Conversation {} released successfully", conversationId);
            return ResponseEntity.ok().body(Map.of(
                "message", "Conversation released successfully",
                "conversationId", conversationId,
                "isTakenOver", false,
                "releasedAt", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("Cannot release conversation: {}", e.getMessage());
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
    @PreAuthorize("isAuthenticated()")
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