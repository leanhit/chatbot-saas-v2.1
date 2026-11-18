package com.chatbot.config;

import com.chatbot.shared.utils.DateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setTimeZone(java.util.TimeZone.getTimeZone(DateUtils.STANDARD_TIMEZONE));
        mapper.setDateFormat(new SimpleDateFormat(DateUtils.STANDARD_JSON_FORMAT));

        // =======================================================
        // ✨ DÒNG GIẢI QUYẾT VẤN ĐỀ UNRECOGNIZED FIELD ✨
        // Cho phép Jackson bỏ qua các trường mà nó không tìm thấy
        // trong class Java khi nhận JSON (deserialization).
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); 
        // =======================================================

        // Uniform UTC contract for LocalDateTime: serialize as ISO-8601 with 'Z'
        // (value is UTC wall-clock per project convention), deserialize both
        // 'Z'-suffixed and plain local formats.
        SimpleModule utcDateTimeModule = new SimpleModule("UtcDateTimeModule");
        utcDateTimeModule.addSerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(DateUtils.formatUtcLocalDateTime(value));
            }
        });
        utcDateTimeModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String text = p.getText();
                if (text == null || text.isBlank()) return null;
                text = text.trim();
                try {
                    if (text.endsWith("Z") || text.contains("+") || (text.length() > 10 && text.charAt(10) == 'T' && text.matches(".*[+-]\\d{2}:?\\d{2}$"))) {
                        return DateUtils.parseUtcLocalDateTime(text);
                    }
                } catch (Exception ignored) {
                    // fall through to plain local parse
                }
                return LocalDateTime.parse(text);
            }
        });
        mapper.registerModule(utcDateTimeModule);

        return mapper;
    }
}
