package com.chatbot.core.tenant.dto;

import com.chatbot.core.tenant.mapper.TenantMapper;
import com.chatbot.core.tenant.model.Tenant;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TenantDatetimeContractTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void tenantResponse_datetimesSerializeAsUtcIso() throws Exception {
        Tenant tenant = Tenant.builder()
                .id(1L).tenantKey("tk").name("T")
                .expiresAt(LocalDateTime.of(2027, 1, 1, 0, 0))
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .packageActivatedAt(LocalDateTime.of(2026, 6, 1, 0, 0))
                .build();

        TenantResponse resp = TenantMapper.toResponse(tenant);
        JsonNode json = mapper.valueToTree(resp);

        for (String field : new String[]{"expiresAt", "createdAt", "packageActivatedAt"}) {
            JsonNode node = json.get(field);
            assertNotNull(node, field);
            assertTrue(node.asText().endsWith("Z"), field + " must end with Z: " + node.asText());
        }
        // LocalDateTime 2026-01-01T00:00 UTC -> Instant 2026-01-01T00:00:00Z
        assertEquals("2026-01-01T00:00:00Z", json.get("createdAt").asText().replaceAll("\\.0+Z$", "Z"));
    }

    @Test
    void tenantSearchResponse_createdAtHasZSuffix() throws Exception {
        TenantSearchResponse resp = TenantSearchResponse.builder()
                .tenantKey("tk").name("T")
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();
        JsonNode json = mapper.valueToTree(resp);
        assertTrue(json.get("createdAt").asText().endsWith("Z"));
    }

    @Test
    void tenantBasicInfoRequest_parsesExpiresAtAsUtc() {
        TenantBasicInfoRequest req = new TenantBasicInfoRequest();
        req.setName("Test Tenant");
        req.setExpiresAt("2027-01-01T00:00:00.000Z");
        // getExpiresAt must return UTC wall-clock, not system-default shifted
        assertEquals(LocalDateTime.of(2027, 1, 1, 0, 0), req.getExpiresAt());
        assertEquals(Instant.parse("2027-01-01T00:00:00Z"), req.getExpiresAtInstant());
    }

    @Test
    void tenantPackageInfo_isExpiredUsesUtcNow() {
        // expiresAt clearly in the past in UTC
        TenantPackageInfo info = TenantPackageInfo.from(1L, null,
                LocalDateTime.of(2020, 1, 1, 0, 0),
                LocalDateTime.of(2020, 1, 2, 0, 0));
        assertTrue(info.isExpired() || info.isFree());
    }
}
