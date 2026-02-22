package com.chatbot.shared.constants;

public class SystemConstants {

    public static final String APPLICATION_NAME = "Chatbot SaaS";
    public static final String APPLICATION_VERSION = "2.1.0";
    public static final String BUILD_VERSION = "v2.1.0";
    public static final String ENVIRONMENT = "environment";
    public static final String SPRING_PROFILES_ACTIVE = "spring.profiles.active";

    public static final class Environment {
        public static final String DEVELOPMENT = "development";
        public static final String TESTING = "testing";
        public static final String STAGING = "staging";
        public static final String PRODUCTION = "production";
        public static final String LOCAL = "local";
        public static final String DEV = "dev";
        public static final String TEST = "test";
        public static final String STAGE = "stage";
        public static final String PROD = "prod";
    }

    public static final class Database {
        public static final String IDENTITY_DB = "chatbot_identity_db";
        public static final String USER_DB = "chatbot_user_db";
        public static final String TENANT_DB = "chatbot_tenant_db";
        public static final String APP_DB = "chatbot_app_db";
        public static final String BILLING_DB = "chatbot_billing_db";
        public static final String WALLET_DB = "chatbot_wallet_db";
        public static final String CONFIG_DB = "chatbot_config_db";
        public static final String MESSAGE_DB = "chatbot_message_db";
        public static final String SPOKES_DB = "chatbot_spokes_db";

        public static final String DEFAULT_SCHEMA = "public";
        public static final String DEFAULT_CATALOG = "chatbot";
        public static final String DEFAULT_DIALECT = "org.hibernate.dialect.PostgreSQLDialect";
        public static final String DEFAULT_DRIVER = "org.postgresql.Driver";
        public static final String DEFAULT_URL_FORMAT = "jdbc:postgresql://%s:%d/%s";
        public static final int DEFAULT_PORT = 5432;
    }

    public static final class Security {
        public static final String JWT_SECRET_DEFAULT = "mySecretKey";
        public static final int JWT_EXPIRATION_DEFAULT = 86400; // 24 hours
        public static final int JWT_REFRESH_EXPIRATION_DEFAULT = 604800; // 7 days
        public static final int PASSWORD_MIN_LENGTH = 8;
        public static final int PASSWORD_MAX_LENGTH = 128;
        public static final int USERNAME_MIN_LENGTH = 3;
        public static final int USERNAME_MAX_LENGTH = 20;
        public static final int MAX_LOGIN_ATTEMPTS = 5;
        public static final int LOCKOUT_DURATION_MINUTES = 30;
        public static final int SESSION_TIMEOUT_MINUTES = 30;
        public static final int CSRF_TOKEN_LENGTH = 32;
        public static final int API_KEY_LENGTH = 32;
    }

    public static final class Pagination {
        public static final int DEFAULT_PAGE_SIZE = 20;
        public static final int MAX_PAGE_SIZE = 100;
        public static final int MIN_PAGE_SIZE = 1;
        public static final int DEFAULT_PAGE_NUMBER = 0;
        public static final String DEFAULT_SORT_PROPERTY = "id";
        public static final String DEFAULT_SORT_DIRECTION = "asc";
    }

    public static final class File {
        public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
        public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
        public static final long MAX_DOCUMENT_SIZE = 20 * 1024 * 1024; // 20MB
        public static final String[] ALLOWED_IMAGE_TYPES = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
        public static final String[] ALLOWED_DOCUMENT_TYPES = {"pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"};
        public static final String[] ALLOWED_ARCHIVE_TYPES = {"zip", "rar", "7z", "tar", "gz"};
        public static final String DEFAULT_UPLOAD_PATH = "/uploads";
        public static final String TEMP_UPLOAD_PATH = "/temp";
        public static final String PROFILE_IMAGE_PATH = "/profiles/images";
        public static final String APP_ICON_PATH = "/apps/icons";
    }

    public static final class Email {
        public static final String DEFAULT_FROM_EMAIL = "noreply@chatbot-saas.com";
        public static final String DEFAULT_FROM_NAME = "Chatbot SaaS";
        public static final String SUPPORT_EMAIL = "support@chatbot-saas.com";
        public static final String NO_REPLY_EMAIL = "noreply@chatbot-saas.com";
        public static final String EMAIL_VERIFICATION_SUBJECT = "Verify your email address";
        public static final String PASSWORD_RESET_SUBJECT = "Reset your password";
        public static final String ACCOUNT_LOCKED_SUBJECT = "Account locked";
        public static final String WELCOME_SUBJECT = "Welcome to Chatbot SaaS";
        public static final String SUBSCRIPTION_EXPIRED_SUBJECT = "Subscription expired";
        public static final String PAYMENT_FAILED_SUBJECT = "Payment failed";
    }

    public static final class SMS {
        public static final String DEFAULT_FROM_NUMBER = "+1234567890";
        public static final String VERIFICATION_TEMPLATE = "Your verification code is: %s";
        public static final String PASSWORD_RESET_TEMPLATE = "Your password reset code is: %s";
        public static final String LOGIN_ALERT_TEMPLATE = "New login detected from %s at %s";
        public static final int VERIFICATION_CODE_LENGTH = 6;
        public static final int PASSWORD_RESET_CODE_LENGTH = 6;
        public static final int CODE_EXPIRY_MINUTES = 10;
    }

    public static final class RateLimit {
        public static final int DEFAULT_REQUEST_LIMIT = 100;
        public static final int DEFAULT_WINDOW_SECONDS = 60;
        public static final int AUTH_REQUEST_LIMIT = 5;
        public static final int AUTH_WINDOW_SECONDS = 300; // 5 minutes
        public static final int PASSWORD_RESET_LIMIT = 3;
        public static final int PASSWORD_RESET_WINDOW_SECONDS = 900; // 15 minutes
        public static final int EMAIL_REQUEST_LIMIT = 10;
        public static final int EMAIL_WINDOW_SECONDS = 3600; // 1 hour
        public static final int SMS_REQUEST_LIMIT = 5;
        public static final int SMS_WINDOW_SECONDS = 3600; // 1 hour
        public static final int UPLOAD_REQUEST_LIMIT = 20;
        public static final int UPLOAD_WINDOW_SECONDS = 3600; // 1 hour
    }

    public static final class Queue {
        public static final String DEFAULT_QUEUE = "default";
        public static final String HIGH_PRIORITY_QUEUE = "high-priority";
        public static final String LOW_PRIORITY_QUEUE = "low-priority";
        public static final String EMAIL_QUEUE = "email";
        public static final String SMS_QUEUE = "sms";
        public static final String NOTIFICATION_QUEUE = "notification";
        public static final String REPORT_QUEUE = "report";
        public static final String CLEANUP_QUEUE = "cleanup";
        public static final int DEFAULT_RETRY_COUNT = 3;
        public static final int MAX_RETRY_COUNT = 5;
        public static final long DEFAULT_DELAY_MS = 1000;
        public static final long MAX_DELAY_MS = 60000; // 1 minute
    }

    public static final class Schedule {
        public static final String CLEANUP_CRON = "0 0 2 * * ?"; // 2:00 AM daily
        public static final String REPORT_CRON = "0 0 6 * * ?"; // 6:00 AM daily
        public static final String BACKUP_CRON = "0 0 3 * * ?"; // 3:00 AM daily
        public static final String SYNC_CRON = "0 */30 * * * ?"; // Every 30 minutes
        public static final String CACHE_REFRESH_CRON = "0 */15 * * * ?"; // Every 15 minutes
        public static final String ANALYTICS_CRON = "0 0 1 * * ?"; // 1:00 AM daily
        public static final String HEALTH_CHECK_CRON = "0 */5 * * * ?"; // Every 5 minutes
    }

    public static final class Feature {
        public static final String USER_REGISTRATION = "user.registration";
        public static final String EMAIL_VERIFICATION = "email.verification";
        public static final String PASSWORD_RESET = "password.reset";
        public static final String MULTI_TENANCY = "multi.tenancy";
        public static final String APP_MARKETPLACE = "app.marketplace";
        public static final String BILLING_SUBSCRIPTION = "billing.subscription";
        public static final String WALLET_PAYMENTS = "wallet.payments";
        public static final String MESSAGE_ROUTING = "message.routing";
        public static final String FACEBOOK_INTEGRATION = "facebook.integration";
        public static final String BOTPRESS_INTEGRATION = "botpress.integration";
        public static final String ODOO_INTEGRATION = "odoo.integration";
        public static final String MINIO_STORAGE = "minio.storage";
        public static final String ANALYTICS_DASHBOARD = "analytics.dashboard";
        public static final String API_ACCESS = "api.access";
        public static final String WEBHOOK_SUPPORT = "webhook.support";
    }

    public static final class Logging {
        public static final String DEFAULT_LOG_LEVEL = "INFO";
        public static final String ROOT_LOGGER = "com.chatbot";
        public static final String SECURITY_LOGGER = "com.chatbot.security";
        public static final String DATABASE_LOGGER = "com.chatbot.database";
        public static final String INTEGRATION_LOGGER = "com.chatbot.integration";
        public static final String PERFORMANCE_LOGGER = "com.chatbot.performance";
        public static final String AUDIT_LOGGER = "com.chatbot.audit";
        public static final String ERROR_LOGGER = "com.chatbot.error";
        public static final String ACCESS_LOGGER = "com.chatbot.access";
        public static final String TRANSACTION_LOGGER = "com.chatbot.transaction";
    }

    public static final class Monitoring {
        public static final String METRICS_PREFIX = "chatbot.";
        public static final String HEALTH_CHECK_PATH = "/health";
        public static final String METRICS_PATH = "/metrics";
        public static final String PROMETHEUS_PATH = "/actuator/prometheus";
        public static final String INFO_PATH = "/actuator/info";
        public static final String ENV_PATH = "/actuator/env";
        public static final String BEANS_PATH = "/actuator/beans";
        public static final String MAPPINGS_PATH = "/actuator/mappings";
        public static final int HEALTH_CHECK_INTERVAL_SECONDS = 30;
        public static final int METRICS_COLLECTION_INTERVAL_SECONDS = 60;
        public static final int ALERT_THRESHOLD_CPU = 80;
        public static final int ALERT_THRESHOLD_MEMORY = 85;
        public static final int ALERT_THRESHOLD_DISK = 90;
    }

    public static final class Integration {
        public static final String FACEBOOK_API_VERSION = "v18.0";
        public static final String FACEBOOK_WEBHOOK_VERIFY_TOKEN = "chatbot-webhook-verify";
        public static final String BOTPRESS_API_VERSION = "v1";
        public static final String ODOO_API_VERSION = "v2";
        public static final int MINIO_CONNECTION_TIMEOUT_SECONDS = 30;
        public static final int FACEBOOK_CONNECTION_TIMEOUT_SECONDS = 30;
        public static final int BOTPRESS_CONNECTION_TIMEOUT_SECONDS = 30;
        public static final int ODOO_CONNECTION_TIMEOUT_SECONDS = 60;
        public static final int INTEGRATION_RETRY_COUNT = 3;
        public static final long INTEGRATION_RETRY_DELAY_MS = 2000;
    }

    public static final class Localization {
        public static final String DEFAULT_LOCALE = "en";
        public static final String DEFAULT_TIMEZONE = "UTC";
        public static final String DEFAULT_CURRENCY = "USD";
        public static final String DEFAULT_COUNTRY = "US";
        public static final String DATE_FORMAT = "yyyy-MM-dd";
        public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        public static final String TIME_FORMAT = "HH:mm:ss";
        public static final String[] SUPPORTED_LOCALES = {"en", "es", "fr", "de", "it", "pt", "zh", "ja", "ko"};
        public static final String[] SUPPORTED_TIMEZONES = {"UTC", "America/New_York", "America/Los_Angeles", "Europe/London", "Europe/Paris", "Asia/Tokyo", "Asia/Shanghai"};
        public static final String[] SUPPORTED_CURRENCIES = {"USD", "EUR", "GBP", "CAD", "AUD", "CHF", "JPY", "CNY", "INR", "SGD"};
    }

    public static final class Validation {
        public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        public static final String PHONE_REGEX = "^[+]?[0-9]{10,15}$";
        public static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{3,20}$";
        public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        public static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";
        public static final String URL_REGEX = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        public static final String IPV4_REGEX = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        public static final String MAC_ADDRESS_REGEX = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
        public static final String COLOR_HEX_REGEX = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
    }

    public static final class Performance {
        public static final int DEFAULT_THREAD_POOL_SIZE = 10;
        public static final int MAX_THREAD_POOL_SIZE = 50;
        public static final int QUEUE_CAPACITY = 100;
        public static final long CONNECTION_TIMEOUT_MS = 30000;
        public static final long READ_TIMEOUT_MS = 60000;
        public static final long WRITE_TIMEOUT_MS = 60000;
        public static final int MAX_CONNECTIONS = 100;
        public static final int MAX_CONNECTIONS_PER_ROUTE = 20;
        public static final long KEEP_ALIVE_DURATION_MS = 30000;
        public static final long IDLE_CONNECTION_TIMEOUT_MS = 60000;
    }

    public static final class Backup {
        public static final String DEFAULT_BACKUP_PATH = "/backups";
        public static final String BACKUP_FORMAT = "yyyy-MM-dd_HH-mm-ss";
        public static final String BACKUP_EXTENSION = ".zip";
        public static final int RETENTION_DAYS = 30;
        public static final int MAX_BACKUP_SIZE_MB = 1024; // 1GB
        public static final boolean ENCRYPT_BACKUP = true;
        public static final String BACKUP_ENCRYPTION_ALGORITHM = "AES";
    }

    public static final class Migration {
        public static final String MIGRATION_PATH = "classpath:db/migration";
        public static final String MIGRATION_TABLE = "flyway_schema_history";
        public static final boolean MIGRATION_ENABLED = true;
        public static final boolean MIGRATION_CLEAN_DISABLED = true;
        public static final boolean MIGRATION_VALIDATE_ON_MIGRATE = true;
        public static final String MIGRATION_BASELINE_VERSION = "1";
        public static final String MIGRATION_BASELINE_DESCRIPTION = "Base migration";
    }
}
