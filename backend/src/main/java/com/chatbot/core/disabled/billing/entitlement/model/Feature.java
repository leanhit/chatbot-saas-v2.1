package com.chatbot.core.billing.entitlement.model;

/**
 * Feature Enumeration
 * Defines available features for entitlement management
 */
public enum Feature {
    // Core Features
    USER_MANAGEMENT("user_management", "Quản lý người dùng"),
    TENANT_MANAGEMENT("tenant_management", "Quản lý tenant"),
    API_ACCESS("api_access", "Truy cập API"),
    
    // Bot Features
    CHATBOT_CREATION("chatbot_creation", "Tạo chatbot"),
    CHATBOT_CUSTOMIZATION("chatbot_customization", "Tùy chỉnh chatbot"),
    MULTILINGUAL_SUPPORT("multilingual_support", "Hỗ trợ đa ngôn ngữ"),
    
    // Integration Features
    FACEBOOK_INTEGRATION("facebook_integration", "Tích hợp Facebook"),
    WHATSAPP_INTEGRATION("whatsapp_integration", "Tích hợp WhatsApp"),
    WEBSITE_WIDGET("website_widget", "Widget website"),
    
    // Storage & Limits
    FILE_UPLOAD("file_upload", "Tải lên file"),
    CLOUD_STORAGE("cloud_storage", "Lưu trữ đám mây"),
    
    // Analytics & Reporting
    ANALYTICS("analytics", "Phân tích"),
    CUSTOM_REPORTS("custom_reports", "Báo cáo tùy chỉnh"),
    EXPORT_DATA("export_data", "Xuất dữ liệu"),
    
    // Advanced Features
    AI_TRAINING("ai_training", "Huấn luyện AI"),
    CUSTOM_INTENTS("custom_intents", "Intent tùy chỉnh"),
    DIALOG_FLOW("dialog_flow", "Luồng hội thoại"),
    
    // Support Features
    PRIORITY_SUPPORT("priority_support", "Hỗ trợ ưu tiên"),
    DEDICATED_SUPPORT("dedicated_support", "Hỗ trợ chuyên biệt"),
    SLA_GUARANTEE("sla_guarantee", "Đảm bảo SLA"),
    
    // Security Features
    ADVANCED_SECURITY("advanced_security", "Bảo mật nâng cao"),
    SSO_INTEGRATION("sso_integration", "Tích hợp SSO"),
    AUDIT_LOGS("audit_logs", "Nhật ký kiểm toán");

    private final String code;
    private final String description;

    Feature(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static Feature fromCode(String code) {
        for (Feature feature : values()) {
            if (feature.getCode().equals(code)) {
                return feature;
            }
        }
        throw new IllegalArgumentException("Unknown feature code: " + code);
    }
}
