package com.chatbot.modules.messaging.takeover.controller;

import com.chatbot.modules.messaging.takeover.model.TakeoverMessage;
import com.chatbot.modules.messaging.takeover.service.TakeoverService;
import com.chatbot.modules.messaging.takeover.websocket.TakeoverWebSocketHandler;
import com.chatbot.modules.messaging.messStore.service.MessageService; // Import Service từ gói messStore
import com.chatbot.modules.messaging.agent.service.AgentMessageService; //Server gửi tin nhắn cho fb
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}