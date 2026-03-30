package com.chatbot.core.simplepayment.config;

import com.chatbot.core.simplepayment.listener.PaymentEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisSubscriberConfig {

    private final PaymentEventListener paymentEventListener;

    @Bean
    public ChannelTopic paymentStatusTopic() {
        return new ChannelTopic("payment:status");
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // Subscribe to payment status channel
        container.addMessageListener(paymentEventListener, paymentStatusTopic());
        
        return container;
    }
}
