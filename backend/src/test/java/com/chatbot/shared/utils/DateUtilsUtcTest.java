package com.chatbot.shared.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsUtcTest {

    @Test
    void toUtcLocalDateTime_convertsInstant() {
        Instant instant = Instant.parse("2026-11-17T09:30:00Z");
        assertEquals(LocalDateTime.of(2026, 11, 17, 9, 30), DateUtils.toUtcLocalDateTime(instant));
    }

    @Test
    void toInstant_treatsLocalDateTimeAsUtc() {
        LocalDateTime ldt = LocalDateTime.of(2026, 11, 17, 9, 30);
        assertEquals(Instant.parse("2026-11-17T09:30:00Z"), DateUtils.toInstant(ldt));
    }

    @Test
    void roundTrip_instantToLocalDateTimeToInstant() {
        Instant instant = Instant.parse("2026-11-17T09:30:15.123Z");
        assertEquals(instant, DateUtils.toInstant(DateUtils.toUtcLocalDateTime(instant)));
    }

    @Test
    void formatUtcLocalDateTime_appendsZ() {
        LocalDateTime ldt = LocalDateTime.of(2026, 11, 17, 9, 30, 0, 123_000_000);
        String out = DateUtils.formatUtcLocalDateTime(ldt);
        assertTrue(out.endsWith("Z"), out);
        assertEquals(instantOf(out), DateUtils.toInstant(ldt));
    }

    @Test
    void parseUtcLocalDateTime_readsZ() {
        LocalDateTime ldt = DateUtils.parseUtcLocalDateTime("2026-11-17T09:30:00Z");
        assertEquals(LocalDateTime.of(2026, 11, 17, 9, 30), ldt);
    }

    @Test
    void nullSafety() {
        assertNull(DateUtils.toUtcLocalDateTime(null));
        assertNull(DateUtils.toInstant(null));
        assertNull(DateUtils.formatInstantUtc(null));
        assertNull(DateUtils.formatUtcLocalDateTime(null));
        assertNull(DateUtils.parseInstantUtc(null));
        assertNull(DateUtils.parseUtcLocalDateTime(null));
        assertNull(DateUtils.parseUtcLocalDateTime(""));
    }

    @Test
    void nowUtc_isUtcInstant() {
        Instant now = DateUtils.nowUtc();
        assertNotNull(now);
        assertEquals(ZoneOffset.UTC.getRules().getOffset(now), ZoneOffset.UTC);
    }

    private static Instant instantOf(String iso) {
        return Instant.parse(iso);
    }
}
