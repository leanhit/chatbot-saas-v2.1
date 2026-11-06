package com.chatbot.core.routing.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class RoutingTestConfiguration {

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate() {
        return new RedisTemplate<>();
    }

    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate() {
        return new StringRedisTemplate();
    }

    @Bean
    @Primary
    public KafkaTemplate<String, String> kafkaTemplate() {
        Map<String, Object> configs = new HashMap<>();
        configs.put("bootstrap.servers", "localhost:9092");
        ProducerFactory<String, String> factory = new org.springframework.kafka.core.DefaultKafkaProducerFactory<>(configs);
        return new KafkaTemplate<>(factory);
    }

    @Bean
    @Primary
    public MultipartFile mockMultipartFile() {
        return new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());
    }
}
