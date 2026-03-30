package com.chatbot.core.simplepayment.listener;

import com.chatbot.core.simplepayment.dto.PaymentEvent;
import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.service.RedisPaymentService;
import com.chatbot.core.simplepayment.service.SimplePaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener implements MessageListener {

    private final SimplePaymentService simplePaymentService;
    private final RedisPaymentService redisPaymentService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String messageBody = new String(message.getBody());
            PaymentEvent event = objectMapper.readValue(messageBody, PaymentEvent.class);
            
            log.debug("📨 Received payment event: {} for payment: {}", event.getType(), event.getReferenceCode());
            
            switch (event.getType()) {
                case "PAYMENT_CREATED":
                    handlePaymentCreated(event);
                    break;
                case "PAYMENT_COMPLETED":
                    handlePaymentCompleted(event);
                    break;
                case "PAYMENT_EXPIRED":
                    handlePaymentExpired(event);
                    break;
                case "PAYMENT_UPDATED":
                    handlePaymentUpdated(event);
                    break;
                default:
                    log.warn("⚠️ Unknown payment event type: {}", event.getType());
            }
        } catch (Exception e) {
            log.error("❌ Error processing payment event: {}", e.getMessage(), e);
        }
    }

    private void handlePaymentCreated(PaymentEvent event) {
        log.debug("📝 Payment created event received: {}", event.getReferenceCode());
        // Store in Redis for tracking
        redisPaymentService.storePayment(event.getReferenceCode(), event);
        redisPaymentService.addToPendingPayments(event.getReferenceCode());
    }

    private void handlePaymentCompleted(PaymentEvent event) {
        log.info("✅ Payment completed event received: {}", event.getReferenceCode());
        
        try {
            // Update payment in database
            if (event.getBankTransactionId() != null) {
                simplePaymentService.completePayment(event.getReferenceCode(), event.getBankTransactionId());
            }
            
            // Remove from pending tracking
            redisPaymentService.removeFromPendingPayments(event.getReferenceCode());
            
            // Update stored payment event
            PaymentEvent updatedEvent = redisPaymentService.createStatusUpdateEvent(
                event.getReferenceCode(), PaymentStatus.COMPLETED, event.getBankTransactionId());
            redisPaymentService.storePayment(event.getReferenceCode(), updatedEvent);
            
        } catch (Exception e) {
            log.error("❌ Error handling payment completion: {}", e.getMessage(), e);
        }
    }

    private void handlePaymentExpired(PaymentEvent event) {
        log.info("⏰ Payment expired event received: {}", event.getReferenceCode());
        
        try {
            // Remove from pending tracking
            redisPaymentService.removeFromPendingPayments(event.getReferenceCode());
            
            // Update stored payment event
            PaymentEvent updatedEvent = redisPaymentService.createStatusUpdateEvent(
                event.getReferenceCode(), PaymentStatus.EXPIRED, null);
            redisPaymentService.storePayment(event.getReferenceCode(), updatedEvent);
            
        } catch (Exception e) {
            log.error("❌ Error handling payment expiration: {}", e.getMessage(), e);
        }
    }

    private void handlePaymentUpdated(PaymentEvent event) {
        log.debug("🔄 Payment updated event received: {}", event.getReferenceCode());
        
        try {
            // Update stored payment event
            redisPaymentService.storePayment(event.getReferenceCode(), event);
            
        } catch (Exception e) {
            log.error("❌ Error handling payment update: {}", e.getMessage(), e);
        }
    }
}
