package com.chatbot.core.payment.transaction.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * Redis message listener for payment events
 * Handles payment status updates and simulated payment notifications
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener implements MessageListener {

    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());
            
            log.info("📨 Redis message received on channel: {}", channel);
            
            // Parse and handle payment events
            // This is a placeholder for event-driven payment processing
            // In the new architecture, events are handled via Spring Events
            
        } catch (Exception e) {
            log.error("❌ Failed to process Redis message", e);
        }
    }
}
