package com.chatbot.shared.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import com.chatbot.shared.utils.DateUtils;

public class DateUtils {

    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String ISO_DATETIME_WITH_ZONE = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final String ISO_DATETIME_WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final String ISO_DATETIME_WITH_NANOS = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX";
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String STANDARD_JSON_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String STANDARD_TIMEZONE = "UTC";
    
    // New standardized format for API consistency
    public static final String API_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String API_TIMEZONE = "UTC";

    public static String formatLocalDate(LocalDate date, String pattern) {
        if (date == null) return null;
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String formatLocalDateTime(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String formatInstant(Instant instant, String pattern) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, DateUtils.UTC_ZONE)
                .format(DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDate parseLocalDate(String dateString, String pattern) {
        if (dateString == null || dateString.trim().isEmpty()) return null;
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDateTime parseLocalDateTime(String dateTimeString, String pattern) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) return null;
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(pattern));
    }

    public static Instant parseInstant(String instantString, String pattern) {
        if (instantString == null || instantString.trim().isEmpty()) return null;
        LocalDateTime localDateTime = LocalDateTime.parse(instantString, DateTimeFormatter.ofPattern(pattern));
        return localDateTime.atZone(DateUtils.UTC_ZONE).toInstant();
    }

    public static LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(DateUtils.UTC_ZONE).toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(DateUtils.UTC_ZONE).toLocalDateTime();
    }

    public static Date toDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(DateUtils.UTC_ZONE).toInstant());
    }

    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(DateUtils.UTC_ZONE).toInstant());
    }

    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) return 0;
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public static long hoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) return 0;
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }

    public static long minutesBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) return 0;
        return ChronoUnit.MINUTES.between(startDateTime, endDateTime);
    }

    public static boolean isBetween(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null || startDate == null || endDate == null) return false;
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (dateTime == null || startDateTime == null || endDateTime == null) return false;
        return !dateTime.isBefore(startDateTime) && !dateTime.isAfter(endDateTime);
    }

    public static LocalDate addDays(LocalDate date, long days) {
        return date != null ? date.plusDays(days) : null;
    }

    public static LocalDate subtractDays(LocalDate date, long days) {
        return date != null ? date.minusDays(days) : null;
    }

    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        return dateTime != null ? dateTime.plusHours(hours) : null;
    }

    public static LocalDateTime subtractHours(LocalDateTime dateTime, long hours) {
        return dateTime != null ? dateTime.minusHours(hours) : null;
    }

    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime != null ? dateTime.plusMinutes(minutes) : null;
    }

    public static LocalDateTime subtractMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime != null ? dateTime.minusMinutes(minutes) : null;
    }

    public static String getAgeString(LocalDate birthDate) {
        if (birthDate == null) return null;
        long years = ChronoUnit.YEARS.between(birthDate, LocalDate.now(DateUtils.UTC_ZONE));
        return years + " years";
    }

    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now(DateUtils.UTC_ZONE));
    }

    public static boolean isYesterday(LocalDate date) {
        return date != null && date.equals(LocalDate.now(DateUtils.UTC_ZONE).minusDays(1));
    }

    public static boolean isThisWeek(LocalDate date) {
        if (date == null) return false;
        LocalDate now = LocalDate.now(DateUtils.UTC_ZONE);
        LocalDate weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        return !date.isBefore(weekStart) && !date.isAfter(weekEnd);
    }

    public static boolean isThisMonth(LocalDate date) {
        return date != null && 
               date.getYear() == LocalDate.now(DateUtils.UTC_ZONE).getYear() &&
               date.getMonth() == LocalDate.now(DateUtils.UTC_ZONE).getMonth();
    }

    public static String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        
        LocalDateTime now = DateUtils.nowUtcLocalDateTime();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);
        
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " minutes ago";
        if (hours < 24) return hours + " hours ago";
        if (days < 30) return days + " days ago";
        
        return formatLocalDateTime(dateTime, "MMM dd, yyyy");
    }

    // ===== UTC contract helpers (Phase 1) =====
    // Use these for all new code. Existing methods above keep legacy
    // DateUtils.UTC_ZONE behavior until migrated per-domain.

    public static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    /** Injectable clock for tests; production default is system UTC. */
    private static volatile Clock clock = Clock.systemUTC();

    public static void setClock(Clock testClock) {
        clock = testClock != null ? testClock : Clock.systemUTC();
    }

    public static Clock getClock() {
        return clock;
    }

    /** Current instant in UTC. */
    public static Instant nowUtc() {
        return Instant.now(clock);
    }

    /** Current LocalDateTime in UTC (for TIMESTAMP-without-tz columns storing UTC). */
    public static LocalDateTime nowUtcLocalDateTime() {
        return LocalDateTime.now(clock.withZone(UTC_ZONE));
    }

    /** Convert Instant to UTC LocalDateTime. */
    public static LocalDateTime toUtcLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, UTC_ZONE);
    }

    /** Convert UTC LocalDateTime to Instant. */
    public static Instant toInstant(LocalDateTime utcLocalDateTime) {
        return utcLocalDateTime == null ? null : utcLocalDateTime.atZone(UTC_ZONE).toInstant();
    }

    /** Format Instant as ISO-8601 UTC string (e.g. 2026-11-17T09:30:00.123Z). */
    public static String formatInstantUtc(Instant instant) {
        return instant == null ? null : instant.toString();
    }

    /** Format UTC LocalDateTime as ISO-8601 UTC string with Z suffix. */
    public static String formatUtcLocalDateTime(LocalDateTime utcLocalDateTime) {
        if (utcLocalDateTime == null) return null;
        return utcLocalDateTime.atZone(UTC_ZONE).toInstant().toString();
    }

    /** Parse ISO-8601 instant string (with offset/Z) to Instant. */
    public static Instant parseInstantUtc(String instantString) {
        if (instantString == null || instantString.trim().isEmpty()) return null;
        return Instant.parse(instantString);
    }

    /** Parse ISO-8601 UTC string ending with 'Z' to UTC LocalDateTime. */
    public static LocalDateTime parseUtcLocalDateTime(String utcString) {
        if (utcString == null || utcString.trim().isEmpty()) return null;
        return LocalDateTime.ofInstant(Instant.parse(utcString), UTC_ZONE);
    }

    /** Parse a datetime string in the given pattern as UTC wall-clock to Instant. */
    public static Instant parseInstantUtc(String value, String pattern) {
        if (value == null || value.trim().isEmpty()) return null;
        return LocalDateTime.parse(value.trim(), DateTimeFormatter.ofPattern(pattern))
                .atZone(UTC_ZONE)
                .toInstant();
    }
}
