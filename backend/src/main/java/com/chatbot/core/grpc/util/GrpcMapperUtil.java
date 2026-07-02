package com.chatbot.core.grpc.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Utility class for common gRPC mapping operations
 * Provides reusable methods for domain-to-gRPC and gRPC-to-domain conversions
 */
public final class GrpcMapperUtil {

    private GrpcMapperUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Convert Long ID to String for gRPC
     */
    public static String longToString(Long value) {
        return value != null ? value.toString() : "";
    }

    /**
     * Convert String ID to Long from gRPC
     */
    public static Long stringToLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Long format: " + value, e);
        }
    }

    /**
     * Convert LocalDateTime to epoch milliseconds for gRPC timestamp
     */
    public static long localDateTimeToTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return 0;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Convert epoch milliseconds to LocalDateTime from gRPC timestamp
     */
    public static LocalDateTime timestampToLocalDateTime(long timestamp) {
        if (timestamp == 0) {
            return null;
        }
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        );
    }

    /**
     * Null-safe string getter
     */
    public static String nullSafeString(String value) {
        return value != null ? value : "";
    }

    /**
     * Null-safe optional string getter
     */
    public static Optional<String> optionalString(String value) {
        return Optional.ofNullable(value).filter(s -> !s.trim().isEmpty());
    }

    /**
     * Convert enum to string, handling null
     */
    public static String enumToString(Enum<?> enumValue) {
        return enumValue != null ? enumValue.name() : "";
    }

    /**
     * Convert string to enum, handling null and invalid values
     */
    public static <T extends Enum<T>> T stringToEnum(String value, Class<T> enumClass, T defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}
