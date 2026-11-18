package com.chatbot.shared.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseContractTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void errorResponse_hasContractFields() throws Exception {
        ErrorResponse error = ErrorResponse.fromUnauthorized("Authentication required")
                .withPath("/api/tenants");

        JsonNode json = mapper.valueToTree(error);

        assertEquals("error", json.get("status").asText());
        assertEquals("UNAUTHORIZED", json.get("code").asText());
        assertEquals("Authentication required", json.get("message").asText());
        assertEquals("/api/tenants", json.get("path").asText());
        assertTrue(json.has("timestamp"));
        String ts = json.get("timestamp").asText();
        assertTrue(ts.endsWith("Z"), "timestamp must carry Z suffix: " + ts);
    }

    @Test
    void forbidden_usesForbiddenCode() throws Exception {
        JsonNode json = mapper.valueToTree(ErrorResponse.fromForbidden("Access denied"));
        assertEquals("FORBIDDEN", json.get("code").asText());
    }
}
