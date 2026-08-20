package com.chatbot.config;

import com.chatbot.core.simplepayment.listener.PaymentEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * Redis Pub/Sub configuration for WebSocket cluster-mode broadcasting and payment events.
 * Enables WebSocket events to be broadcast across multiple backend instances.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisPubSubConfig {

    public static final String WEBSOCKET_TAKEOVER_TOPIC = "websocket:takeover";
    public static final String WEBSOCKET_NOTIFICATION_TOPIC = "websocket:notification";
    public static final String WEBSOCKET_PRESENCE_TOPIC = "websocket:presence";

    @Bean
    public ChannelTopic paymentStatusTopic() {
        return new ChannelTopic("payment:status");
    }

    @Bean
    public ChannelTopic paymentSimulatedTopic() {
        return new ChannelTopic("payment:simulated");
    }

    @Bean
    RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
                                                                MessageListenerAdapter takeoverListener,
                                                                MessageListenerAdapter notificationListener,
                                                                MessageListenerAdapter presenceListener,
                                                                PaymentEventListener paymentEventListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // Subscribe to takeover events
        container.addMessageListener(takeoverListener, new PatternTopic(WEBSOCKET_TAKEOVER_TOPIC));

        // Subscribe to notification events
        container.addMessageListener(notificationListener, new PatternTopic(WEBSOCKET_NOTIFICATION_TOPIC));

        // Subscribe to presence events
        container.addMessageListener(presenceListener, new PatternTopic(WEBSOCKET_PRESENCE_TOPIC));

        // Subscribe to payment status channel
        container.addMessageListener(paymentEventListener, paymentStatusTopic());

        // Subscribe to payment simulated channel
        container.addMessageListener(paymentEventListener, paymentSimulatedTopic());

        log.info("✅ Redis Pub/Sub container configured for topics: {}, {}, {}, {}, {}",
                 WEBSOCKET_TAKEOVER_TOPIC, WEBSOCKET_NOTIFICATION_TOPIC, WEBSOCKET_PRESENCE_TOPIC,
                 "payment:status", "payment:simulated");

        return container;
    }

    @Bean
    MessageListenerAdapter takeoverListener(RedisTakeoverMessageListener listener) {
        return new MessageListenerAdapter(listener, "handleMessage");
    }

    @Bean
    MessageListenerAdapter notificationListener(RedisNotificationMessageListener listener) {
        return new MessageListenerAdapter(listener, "handleMessage");
    }

    @Bean
    MessageListenerAdapter presenceListener(RedisPresenceMessageListener listener) {
        return new MessageListenerAdapter(listener, "handleMessage");
    }
}
