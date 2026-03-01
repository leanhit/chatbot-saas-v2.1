package com.chatbot.shared.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DateUtils {

    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String ISO_DATETIME_WITH_ZONE = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String STANDARD_JSON_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String STANDARD_TIMEZONE = "Asia/Ho_Chi_Minh";

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
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
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
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    public static LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date toDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
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
        long years = ChronoUnit.YEARS.between(birthDate, LocalDate.now());
        return years + " years";
    }

    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }

    public static boolean isYesterday(LocalDate date) {
        return date != null && date.equals(LocalDate.now().minusDays(1));
    }

    public static boolean isThisWeek(LocalDate date) {
        if (date == null) return false;
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        return !date.isBefore(weekStart) && !date.isAfter(weekEnd);
    }

    public static boolean isThisMonth(LocalDate date) {
        return date != null && 
               date.getYear() == LocalDate.now().getYear() &&
               date.getMonth() == LocalDate.now().getMonth();
    }

    public static String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);
        
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " minutes ago";
        if (hours < 24) return hours + " hours ago";
        if (days < 30) return days + " days ago";
        
        return formatLocalDateTime(dateTime, "MMM dd, yyyy");
    }
}
