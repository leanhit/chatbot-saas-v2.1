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
            log.info("📨 Received raw message: {}", messageBody);
            
            PaymentEvent event;
            
            // Try to parse as JSON string first
            if (messageBody.startsWith("{")) {
                event = objectMapper.readValue(messageBody, PaymentEvent.class);
            } else {
                // If it's already deserialized, convert it
                event = objectMapper.convertValue(messageBody, PaymentEvent.class);
            }
            
            log.info("📨 Parsed payment event: {} for payment: {}", event.getType(), event.getReferenceCode());
            
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
                case "PAYMENT_SIMULATED":
                    handlePaymentSimulated(event);
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
            // Ignore "Payment not found" errors - payment already completed
            if (e.getMessage() != null && e.getMessage().contains("Payment not found")) {
                log.info("Payment {} already completed, skipping duplicate completion", event.getReferenceCode());
            } else {
                log.error("Error handling payment completion: {}", e.getMessage(), e);
            }
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
            
            // Check if this is a simulated payment that needs completion
            if (event.getType().equals("PAYMENT_SIMULATED") && event.getBankTransactionId() != null) {
                log.info("🧪 Handling simulated payment completion: {}", event.getReferenceCode());
                simplePaymentService.completePayment(event.getReferenceCode(), event.getBankTransactionId());
            }
            
        } catch (Exception e) {
            log.error("❌ Error handling payment update: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle simulated payment completion
     */
    private void handlePaymentSimulated(PaymentEvent event) {
        log.info("🧪 Simulated payment event received: {}", event.getReferenceCode());
        log.info("🧪 Event details: bankTransactionId={}, amount={}", event.getBankTransactionId(), event.getAmount());
        
        try {
            // Use the transaction ID from the event
            String transactionId = event.getBankTransactionId();
            
            if (transactionId != null) {
                log.info("🔄 Calling completePayment for: {} with transaction: {}", event.getReferenceCode(), transactionId);
                
                // Complete the payment
                simplePaymentService.completePayment(event.getReferenceCode(), transactionId);
                log.info("✅ Simulated payment completed: {}", event.getReferenceCode());
                
                // Remove from pending tracking
                redisPaymentService.removeFromPendingPayments(event.getReferenceCode());
                
                // Update stored payment event
                PaymentEvent updatedEvent = redisPaymentService.createStatusUpdateEvent(
                    event.getReferenceCode(), PaymentStatus.COMPLETED, transactionId);
                redisPaymentService.storePayment(event.getReferenceCode(), updatedEvent);
            } else {
                log.warn("⚠️ No transaction ID in simulated payment event: {}", event.getReferenceCode());
            }
            
        } catch (Exception e) {
            log.error("❌ Error handling simulated payment: {}", e.getMessage(), e);
        }
    }
}
