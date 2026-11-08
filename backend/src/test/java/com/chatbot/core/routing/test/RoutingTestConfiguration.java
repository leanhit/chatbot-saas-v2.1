package com.chatbot.core.routing.test;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
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

    // Placeholder beans satisfying @DependsOn("*Flyway") on hub EntityManagerFactory
    // beans when MultiFlywayConfig is disabled (app.flyway.enabled=false) in tests.
    @Bean(name = "identityFlyway") public Object identityFlyway() { return new Object(); }
    @Bean(name = "userFlyway") public Object userFlyway() { return new Object(); }
    @Bean(name = "tenantFlyway") public Object tenantFlyway() { return new Object(); }
    @Bean(name = "appFlyway") public Object appFlyway() { return new Object(); }
    @Bean(name = "configFlyway") public Object configFlyway() { return new Object(); }
    @Bean(name = "messageFlyway") public Object messageFlyway() { return new Object(); }
    @Bean(name = "sharedFlyway") public Object sharedFlyway() { return new Object(); }
    @Bean(name = "spokesFlyway") public Object spokesFlyway() { return new Object(); }

    @Bean
    @Primary
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
}
