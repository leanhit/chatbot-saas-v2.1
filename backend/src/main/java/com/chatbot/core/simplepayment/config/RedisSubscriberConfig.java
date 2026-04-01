package com.chatbot.core.simplepayment.config;

import com.chatbot.core.simplepayment.listener.PaymentEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisSubscriberConfig {

    @Bean
    public ChannelTopic paymentStatusTopic() {
        return new ChannelTopic("payment:status");
    }
    
    @Bean
    public ChannelTopic paymentSimulatedTopic() {
        return new ChannelTopic("payment:simulated");
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            PaymentEventListener paymentEventListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // Subscribe to payment status channel
        container.addMessageListener(paymentEventListener, paymentStatusTopic());
        
        // Subscribe to payment simulated channel
        container.addMessageListener(paymentEventListener, paymentSimulatedTopic());
        
        return container;
    }
}
