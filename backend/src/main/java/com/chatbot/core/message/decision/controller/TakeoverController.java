package com.chatbot.core.message.decision.controller;

import com.chatbot.core.message.decision.model.TakeoverMessage;
import com.chatbot.core.message.decision.service.TakeoverService;
import com.chatbot.core.message.decision.websocket.TakeoverWebSocketHandler;
import com.chatbot.core.message.store.service.MessageService; // Import Service từ gói messStore
import com.chatbot.core.message.router.service.AgentMessageService; //Server gửi tin nhắn cho fb
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/takeover")
@RequiredArgsConstructor
public class TakeoverController {

    private final TakeoverService takeoverService;
    private final TakeoverWebSocketHandler websocketHandler;
    private final ObjectMapper objectMapper;
    private final MessageService messageService; // Inject MessageService để lưu DB
    private final AgentMessageService agentMessageService;

    // UI gửi tin nhắn → lưu DB (lâu dài) → lưu Redis (cache) → push WebSocket (real-time)
    @PostMapping("/send")
    public void sendMessage(@RequestBody TakeoverMessage message) {
        message.setTimestamp(System.currentTimeMillis());
        
        String conversationIdStr = message.getConversationId();
        Long conversationIdLong = Long.parseLong(conversationIdStr); // Sử dụng String để gọi sendToConversation

        agentMessageService.sendAgentTextMessage(
            conversationIdLong, 
            message.getContent(), 
            null // agentId đang là null, có thể cần lấy từ context sau này
        );
        // // 1. Lưu vào DB dài hạn (Persistence Layer - messStore)
        // // Tin nhắn gửi từ UI/Agent thường là text
        // messageService.saveMessage(
        //         conversationIdLong, 
        //         message.getSender(), 
        //         message.getContent(), 
        //         "text", 
        //         null 
        // );
        
        // // 2. Lưu vào Redis tạm thời
        // takeoverService.saveMessage(message);
        
        // // 3. Push WebSocket cho real-time
        // // SỬA LỖI: Thay thế sendToAll bằng sendToConversation
        // websocketHandler.sendToConversation(conversationIdStr, message);
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
            // Logic để takeover conversation
            // Cần implement takeover logic trong service
            return ResponseEntity.ok().body("{\"message\": \"Conversation taken over successfully\"}");
        } catch (Exception e) {
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