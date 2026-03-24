package com.chatbot.core.identity.constants;

public final class IdentityConstants {
    
    // JWT Constants
    public static final String BEARER_PREFIX = "Bearer ";
    public static final int BEARER_PREFIX_LENGTH = 7;
    public static final String TOKEN_HEADER = "Authorization";
    
    // Error Messages
    public static final String EMAIL_ALREADY_EXISTS = "Email đã tồn tại trong hệ thống";
    public static final String INVALID_CREDENTIALS = "Email hoặc mật khẩu không chính xác";
    public static final String USER_NOT_FOUND = "Không tìm thấy người dùng";
    public static final String INVALID_OLD_PASSWORD = "Mật khẩu cũ không chính xác";
    public static final String PASSWORD_MISMATCH = "Mật khẩu và xác nhận mật khẩu không khớp";
    public static final String TOKEN_EXPIRED = "Token đã hết hạn";
    public static final String TOKEN_INVALID = "Token không hợp lệ";
    public static final String VALIDATION_ERROR = "Dữ liệu đầu vào không hợp lệ";
    
    // Rate Limiting
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int LOGIN_ATTEMPT_WINDOW_MINUTES = 15;
    public static final String RATE_LIMIT_PREFIX = "rate_limit:";
    public static final String BLACKLIST_PREFIX = "blacklist:";
    
    // Audit Log Messages
    public static final String AUDIT_LOGIN_SUCCESS = "Đăng nhập thành công";
    public static final String AUDIT_LOGIN_FAILED = "Đăng nhập thất bại";
    public static final String AUDIT_REGISTER_SUCCESS = "Đăng ký tài khoản thành công";
    public static final String AUDIT_PASSWORD_CHANGED = "Thay đổi mật khẩu";
    public static final String AUDIT_ROLE_CHANGED = "Thay đổi vai trò người dùng";
    public static final String AUDIT_TOKEN_REVOKED = "Thu hồi token";
    
    // Validation Patterns
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$";
    
    // Cache Keys
    public static final String USER_CACHE_PREFIX = "user:";
    public static final String TOKEN_CACHE_PREFIX = "token:";
    public static final long CACHE_EXPIRATION_HOURS = 24;
    
    // Private constructor to prevent instantiation
    private IdentityConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
