package com.chatbot.core.simplepayment.listener;

import com.chatbot.core.simplepayment.service.PaymentExpirationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private final PaymentExpirationService paymentExpirationService;
    private static final String PAYMENT_TTL_PREFIX = "payment:ttl:";

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer,
                                      PaymentExpirationService paymentExpirationService) {
        super(listenerContainer);
        this.paymentExpirationService = paymentExpirationService;
        log.info("⏰ RedisKeyExpirationListener initialized and registered to RedisMessageListenerContainer");
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("⏰ Redis key expired event received: {}", expiredKey);

        if (expiredKey.startsWith(PAYMENT_TTL_PREFIX)) {
            String referenceCode = expiredKey.substring(PAYMENT_TTL_PREFIX.length());
            log.info("⏰ Payment TTL expired for reference: {}, triggering expiration", referenceCode);
            try {
                paymentExpirationService.expirePayment(referenceCode);
            } catch (Exception e) {
                log.error("❌ Failed to expire payment {} on Redis key expiration: {}", referenceCode, e.getMessage(), e);
            }
        }
    }
}
