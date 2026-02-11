package com.chatbot.modules.messaging.agent.controller;

import com.chatbot.modules.messaging.agent.dto.MessageSendPayload;
import com.chatbot.modules.messaging.agent.service.AgentMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller xử lý các yêu cầu gửi tin nhắn đi từ Agent.
 */
@RestController
@RequestMapping("/api/agent")
public class AgentMessageController {

    private final AgentMessageService agentMessageService;

    public AgentMessageController(AgentMessageService agentMessageService) {
        this.agentMessageService = agentMessageService;
    }

    @PostMapping("/send-message")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody MessageSendPayload payload) {
        System.out.println("message from UI: " + payload.getContent());
        if (!"text".equalsIgnoreCase(payload.getMessageType())) {
            // Tạm thời chỉ hỗ trợ tin nhắn text
            return ResponseEntity.badRequest().body(Map.of("message", "Only text messages are supported currently."));
        }

        try {
            // Giả định Agent ID là 101 cho mục đích demo (Bạn cần lấy ID Agent thực tế từ Session/JWT)
            Integer mockAgentId = 101; 
            
            agentMessageService.sendAgentTextMessage(
                payload.getConversationId(), 
                payload.getContent(), 
                mockAgentId
            );

            return ResponseEntity.ok(Map.of("status", "success", "message", "Message sent and logged."));

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi Agent gửi tin nhắn: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
    
    // Tạo DTO cho payload
    public static class MessageSendPayload {
        private Long conversationId;
        private String sender;
        private String content;
        private String messageType;
        private Map<String, Object> rawPayload;

        // Getters and Setters... (Bạn cần tự thêm Getters/Setters hoặc dùng Lombok)
        // Ví dụ:
        public Long getConversationId() { return conversationId; }
        public String getContent() { return content; }
        public String getMessageType() { return messageType; }
        // ...
    }
}