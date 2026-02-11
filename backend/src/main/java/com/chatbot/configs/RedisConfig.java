package com.chatbot.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // Lấy giá trị từ application.properties
    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    /**
     * Định nghĩa RedisConnectionFactory rõ ràng là Standalone (Độc lập).
     * Điều này ngăn chặn lỗi READONLY do nhầm lẫn Cluster/Replica.
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(config);
    }
    
    // Cấu hình RedisTemplate cho String keys và Object values
    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Sử dụng StringSerializer cho Key (vì bạn dùng StringRedisTemplate để opsForList)
        template.setKeySerializer(new StringRedisSerializer());
        
        // Cài đặt Serializer cho Value theo nhu cầu (ví dụ: JSON)
        // Lưu ý: Nếu bạn đang dùng StringRedisTemplate (opsForList<String, String>),
        // bạn KHÔNG cần cấu hình RedisTemplate này cho việc lưu message.
        
        template.afterPropertiesSet();
        return template;
    }
}