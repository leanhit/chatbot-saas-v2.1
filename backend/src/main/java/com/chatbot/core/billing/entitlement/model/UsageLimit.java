package com.chatbot.core.billing.entitlement.model;

/**
 * Usage Limit Type Enumeration
 */
public enum UsageLimit {
    // User Limits
    MAX_USERS("max_users", "Số lượng người dùng tối đa"),
    MAX_ADMINS("max_admins", "Số lượng admin tối đa"),
    
    // Bot Limits
    MAX_BOTS("max_bots", "Số lượng bot tối đa"),
    MAX_INTENTS("max_intents", "Số lượng intent tối đa"),
    MAX_DIALOGS("max_dialogs", "Số lượng luồng hội thoại tối đa"),
    
    // Message Limits
    MAX_MESSAGES_PER_MONTH("max_messages_per_month", "Số tin nhắn tối đa mỗi tháng"),
    MAX_MESSAGES_PER_DAY("max_messages_per_day", "Số tin nhắn tối đa mỗi ngày"),
    MAX_API_CALLS_PER_MONTH("max_api_calls_per_month", "Số API call tối đa mỗi tháng"),
    
    // Storage Limits
    MAX_STORAGE_MB("max_storage_mb", "Dung lượng lưu trữ tối đa (MB)"),
    MAX_FILE_SIZE_MB("max_file_size_mb", "Kích thước file tối đa (MB)"),
    MAX_FILES_PER_MONTH("max_files_per_month", "Số file tải lên tối đa mỗi tháng"),
    
    // Integration Limits
    MAX_FACEBOOK_PAGES("max_facebook_pages", "Số trang Facebook tối đa"),
    MAX_WHATSAPP_NUMBERS("max_whatsapp_numbers", "Số WhatsApp tối đa"),
    MAX_WEBSITE_DOMAINS("max_website_domains", "Số domain website tối đa"),
    
    // Feature Limits
    MAX_LANGUAGES("max_languages", "Số ngôn ngữ tối đa"),
    MAX_CUSTOM_RESPONSES("max_custom_responses", "Số phản hồi tùy chỉnh tối đa"),
    MAX_TRAINING_PHRASES("max_training_phrases", "Số cụm từ huấn luyện tối đa"),
    
    // Rate Limits
    API_RATE_LIMIT_PER_MINUTE("api_rate_limit_per_minute", "Giới hạn API mỗi phút"),
    API_RATE_LIMIT_PER_HOUR("api_rate_limit_per_hour", "Giới hạn API mỗi giờ"),
    
    // Session Limits
    MAX_CONCURRENT_SESSIONS("max_concurrent_sessions", "Số phiên đồng thời tối đa"),
    MAX_SESSION_DURATION_MINUTES("max_session_duration_minutes", "Thời lượng phiên tối đa (phút)");

    private final String code;
    private final String description;

    UsageLimit(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static UsageLimit fromCode(String code) {
        for (UsageLimit limit : values()) {
            if (limit.getCode().equals(code)) {
                return limit;
            }
        }
        throw new IllegalArgumentException("Unknown usage limit code: " + code);
    }
}
