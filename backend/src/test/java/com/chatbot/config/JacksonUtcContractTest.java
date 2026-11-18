package com.chatbot.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JacksonUtcContractTest {

    private final ObjectMapper mapper = new JacksonConfig().objectMapper();

    static class Holder {
        public LocalDateTime at;
        public LocalDateTime nil;
    }

    @Test
    void localDateTime_serializesWithZ() throws Exception {
        Holder h = new Holder();
        h.at = LocalDateTime.of(2026, 11, 17, 9, 30, 0);
        JsonNode json = mapper.valueToTree(h);
        String ts = json.get("at").asText();
        assertTrue(ts.endsWith("Z"), ts);
        assertTrue(ts.startsWith("2026-11-17T09:30:00"), ts);
    }

    @Test
    void localDateTime_deserializesBothFormats() throws Exception {
        Holder h1 = mapper.readValue("{\"at\":\"2026-11-17T09:30:00Z\"}", Holder.class);
        assertEquals(LocalDateTime.of(2026, 11, 17, 9, 30), h1.at);

        Holder h2 = mapper.readValue("{\"at\":\"2026-11-17T09:30:00\"}", Holder.class);
        assertEquals(LocalDateTime.of(2026, 11, 17, 9, 30), h2.at);

        Holder h3 = mapper.readValue("{\"at\":\"2026-11-17T09:30:00.123Z\"}", Holder.class);
        assertEquals(LocalDateTime.of(2026, 11, 17, 9, 30, 0, 123_000_000), h3.at);
    }
}
