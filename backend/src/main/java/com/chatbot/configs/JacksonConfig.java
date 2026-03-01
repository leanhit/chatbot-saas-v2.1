package com.chatbot.configs;

import com.chatbot.shared.utils.DateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.text.SimpleDateFormat;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new SimpleDateFormat(DateUtils.STANDARD_JSON_FORMAT));

        // =======================================================
        // ✨ DÒNG GIẢI QUYẾT VẤN ĐỀ UNRECOGNIZED FIELD ✨
        // Cho phép Jackson bỏ qua các trường mà nó không tìm thấy
        // trong class Java khi nhận JSON (deserialization).
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); 
        // =======================================================

        
        return mapper;
    }
}
