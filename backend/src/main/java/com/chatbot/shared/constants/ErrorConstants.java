package com.chatbot.shared.constants;

public class ErrorConstants {

    public static final String GENERAL_ERROR_PREFIX = "ERR_";
    public static final String VALIDATION_ERROR_PREFIX = "VAL_";
    public static final String BUSINESS_ERROR_PREFIX = "BIZ_";
    public static final String SYSTEM_ERROR_PREFIX = "SYS_";
    public static final String SECURITY_ERROR_PREFIX = "SEC_";
    public static final String INTEGRATION_ERROR_PREFIX = "INT_";

    public static final class General {
        public static final String INTERNAL_SERVER_ERROR = GENERAL_ERROR_PREFIX + "001";
        public static final String BAD_REQUEST = GENERAL_ERROR_PREFIX + "002";
        public static final String NOT_FOUND = GENERAL_ERROR_PREFIX + "003";
        public static final String METHOD_NOT_ALLOWED = GENERAL_ERROR_PREFIX + "004";
        public static final String UNSUPPORTED_MEDIA_TYPE = GENERAL_ERROR_PREFIX + "005";
        public static final String CONFLICT = GENERAL_ERROR_PREFIX + "006";
        public static final String UNPROCESSABLE_ENTITY = GENERAL_ERROR_PREFIX + "007";
        public static final String TOO_MANY_REQUESTS = GENERAL_ERROR_PREFIX + "008";
        public static final String SERVICE_UNAVAILABLE = GENERAL_ERROR_PREFIX + "009";
        public static final String TIMEOUT = GENERAL_ERROR_PREFIX + "010";
    }

    public static final class Validation {
        public static final String REQUIRED_FIELD = VALIDATION_ERROR_PREFIX + "001";
        public static final String INVALID_EMAIL = VALIDATION_ERROR_PREFIX + "002";
        public static final String INVALID_PHONE = VALIDATION_ERROR_PREFIX + "003";
        public static final String INVALID_PASSWORD = VALIDATION_ERROR_PREFIX + "004";
        public static final String INVALID_USERNAME = VALIDATION_ERROR_PREFIX + "005";
        public static final String INVALID_LENGTH = VALIDATION_ERROR_PREFIX + "006";
        public static final String INVALID_RANGE = VALIDATION_ERROR_PREFIX + "007";
        public static final String INVALID_FORMAT = VALIDATION_ERROR_PREFIX + "008";
        public static final String INVALID_DATE = VALIDATION_ERROR_PREFIX + "009";
        public static final String INVALID_UUID = VALIDATION_ERROR_PREFIX + "010";
        public static final String INVALID_URL = VALIDATION_ERROR_PREFIX + "011";
        public static final String INVALID_JSON = VALIDATION_ERROR_PREFIX + "012";
        public static final String INVALID_FILE_TYPE = VALIDATION_ERROR_PREFIX + "013";
        public static final String INVALID_FILE_SIZE = VALIDATION_ERROR_PREFIX + "014";
        public static final String DUPLICATE_VALUE = VALIDATION_ERROR_PREFIX + "015";
        public static final String INVALID_COUNTRY_CODE = VALIDATION_ERROR_PREFIX + "016";
        public static final String INVALID_CURRENCY_CODE = VALIDATION_ERROR_PREFIX + "017";
        public static final String INVALID_POSTAL_CODE = VALIDATION_ERROR_PREFIX + "018";
    }

    public static final class Business {
        public static final String USER_NOT_FOUND = BUSINESS_ERROR_PREFIX + "001";
        public static final String USER_ALREADY_EXISTS = BUSINESS_ERROR_PREFIX + "002";
        public static final String USER_INACTIVE = BUSINESS_ERROR_PREFIX + "003";
        public static final String USER_SUSPENDED = BUSINESS_ERROR_PREFIX + "004";
        public static final String INVALID_CREDENTIALS = BUSINESS_ERROR_PREFIX + "005";
        public static final String ACCOUNT_LOCKED = BUSINESS_ERROR_PREFIX + "006";
        public static final String EMAIL_NOT_VERIFIED = BUSINESS_ERROR_PREFIX + "007";
        public static final String PASSWORD_EXPIRED = BUSINESS_ERROR_PREFIX + "008";
        public static final String WEAK_PASSWORD = BUSINESS_ERROR_PREFIX + "009";

        public static final String TENANT_NOT_FOUND = BUSINESS_ERROR_PREFIX + "010";
        public static final String TENANT_ALREADY_EXISTS = BUSINESS_ERROR_PREFIX + "011";
        public static final String TENANT_INACTIVE = BUSINESS_ERROR_PREFIX + "012";
        public static final String TENANT_SUSPENDED = BUSINESS_ERROR_PREFIX + "013";
        public static final String TENANT_LIMIT_EXCEEDED = BUSINESS_ERROR_PREFIX + "014";
        public static final String TENANT_ACCESS_DENIED = BUSINESS_ERROR_PREFIX + "015";

        public static final String APP_NOT_FOUND = BUSINESS_ERROR_PREFIX + "016";
        public static final String APP_ALREADY_EXISTS = BUSINESS_ERROR_PREFIX + "017";
        public static final String APP_INACTIVE = BUSINESS_ERROR_PREFIX + "018";
        public static final String APP_NOT_SUBSCRIBED = BUSINESS_ERROR_PREFIX + "019";
        public static final String APP_SUBSCRIPTION_EXPIRED = BUSINESS_ERROR_PREFIX + "020";
        public static final String APP_ACCESS_DENIED = BUSINESS_ERROR_PREFIX + "021";

        public static final String WALLET_NOT_FOUND = BUSINESS_ERROR_PREFIX + "022";
        public static final String INSUFFICIENT_BALANCE = BUSINESS_ERROR_PREFIX + "023";
        public static final String TRANSACTION_FAILED = BUSINESS_ERROR_PREFIX + "024";
        public static final String TRANSACTION_NOT_FOUND = BUSINESS_ERROR_PREFIX + "025";
        public static final String DUPLICATE_TRANSACTION = BUSINESS_ERROR_PREFIX + "026";
        public static final String INVALID_AMOUNT = BUSINESS_ERROR_PREFIX + "027";

        public static final String BILLING_ACCOUNT_NOT_FOUND = BUSINESS_ERROR_PREFIX + "028";
        public static final String SUBSCRIPTION_NOT_FOUND = BUSINESS_ERROR_PREFIX + "029";
        public static final String SUBSCRIPTION_EXPIRED = BUSINESS_ERROR_PREFIX + "030";
        public static final String ENTITLEMENT_NOT_FOUND = BUSINESS_ERROR_PREFIX + "031";
        public static final String USAGE_LIMIT_EXCEEDED = BUSINESS_ERROR_PREFIX + "032";

        public static final String CONFIG_NOT_FOUND = BUSINESS_ERROR_PREFIX + "033";
        public static final String CONFIG_INVALID = BUSINESS_ERROR_PREFIX + "034";
        public static final String FEATURE_NOT_ENABLED = BUSINESS_ERROR_PREFIX + "035";

        public static final String MESSAGE_NOT_FOUND = BUSINESS_ERROR_PREFIX + "036";
        public static final String CONVERSATION_NOT_FOUND = BUSINESS_ERROR_PREFIX + "037";
        public static final String MESSAGE_SEND_FAILED = BUSINESS_ERROR_PREFIX + "038";
        public static final String ROUTING_FAILED = BUSINESS_ERROR_PREFIX + "039";
    }

    public static final class Security {
        public static final String UNAUTHORIZED = SECURITY_ERROR_PREFIX + "001";
        public static final String FORBIDDEN = SECURITY_ERROR_PREFIX + "002";
        public static final String TOKEN_EXPIRED = SECURITY_ERROR_PREFIX + "003";
        public static final String TOKEN_INVALID = SECURITY_ERROR_PREFIX + "004";
        public static final String TOKEN_MISSING = SECURITY_ERROR_PREFIX + "005";
        public static final String API_KEY_INVALID = SECURITY_ERROR_PREFIX + "006";
        public static final String CSRF_TOKEN_INVALID = SECURITY_ERROR_PREFIX + "007";
        public static final String RATE_LIMIT_EXCEEDED = SECURITY_ERROR_PREFIX + "008";
        public static final String IP_BLOCKED = SECURITY_ERROR_PREFIX + "009";
        public static final String SESSION_EXPIRED = SECURITY_ERROR_PREFIX + "010";
        public static final String PERMISSION_DENIED = SECURITY_ERROR_PREFIX + "011";
        public static final String ROLE_INSUFFICIENT = SECURITY_ERROR_PREFIX + "012";
    }

    public static final class System {
        public static final String DATABASE_ERROR = SYSTEM_ERROR_PREFIX + "001";
        public static final String CONNECTION_ERROR = SYSTEM_ERROR_PREFIX + "002";
        public static final String TIMEOUT_ERROR = SYSTEM_ERROR_PREFIX + "003";
        public static final String MEMORY_ERROR = SYSTEM_ERROR_PREFIX + "004";
        public static final String DISK_SPACE_ERROR = SYSTEM_ERROR_PREFIX + "005";
        public static final String NETWORK_ERROR = SYSTEM_ERROR_PREFIX + "006";
        public static final String CACHE_ERROR = SYSTEM_ERROR_PREFIX + "007";
        public static final String QUEUE_ERROR = SYSTEM_ERROR_PREFIX + "008";
        public static final String EMAIL_SERVICE_ERROR = SYSTEM_ERROR_PREFIX + "009";
        public static final String SMS_SERVICE_ERROR = SYSTEM_ERROR_PREFIX + "010";
        public static final String FILE_SYSTEM_ERROR = SYSTEM_ERROR_PREFIX + "011";
        public static final String CONFIGURATION_ERROR = SYSTEM_ERROR_PREFIX + "012";
    }

    public static final class Integration {
        public static final String FACEBOOK_API_ERROR = INTEGRATION_ERROR_PREFIX + "001";
        public static final String FACEBOOK_WEBHOOK_FAILED = INTEGRATION_ERROR_PREFIX + "002";
        public static final String FACEBOOK_CONNECTION_FAILED = INTEGRATION_ERROR_PREFIX + "003";

        public static final String BOTPRESS_API_ERROR = INTEGRATION_ERROR_PREFIX + "004";
        public static final String BOTPRESS_AUTH_FAILED = INTEGRATION_ERROR_PREFIX + "005";
        public static final String BOTPRESS_MAPPING_FAILED = INTEGRATION_ERROR_PREFIX + "006";

        public static final String ODOO_API_ERROR = INTEGRATION_ERROR_PREFIX + "007";
        public static final String ODOO_CONNECTION_FAILED = INTEGRATION_ERROR_PREFIX + "008";
        public static final String ODOO_DATA_SYNC_FAILED = INTEGRATION_ERROR_PREFIX + "009";

        public static final String MINIO_STORAGE_ERROR = INTEGRATION_ERROR_PREFIX + "010";
        public static final String MINIO_CONNECTION_FAILED = INTEGRATION_ERROR_PREFIX + "011";
        public static final String MINIO_UPLOAD_FAILED = INTEGRATION_ERROR_PREFIX + "012";

        public static final String PAYMENT_GATEWAY_ERROR = INTEGRATION_ERROR_PREFIX + "013";
        public static final String PAYMENT_FAILED = INTEGRATION_ERROR_PREFIX + "014";
        public static final String REFUND_FAILED = INTEGRATION_ERROR_PREFIX + "015";
    }

    public static final class Messages {
        public static final String INTERNAL_SERVER_ERROR_MSG = "An unexpected error occurred. Please try again later.";
        public static final String BAD_REQUEST_MSG = "Invalid request. Please check your input.";
        public static final String NOT_FOUND_MSG = "Resource not found.";
        public static final String UNAUTHORIZED_MSG = "Authentication required.";
        public static final String FORBIDDEN_MSG = "Access denied.";
        public static final String VALIDATION_FAILED_MSG = "Validation failed. Please check your input.";
        public static final String RATE_LIMIT_EXCEEDED_MSG = "Too many requests. Please try again later.";
        public static final String SERVICE_UNAVAILABLE_MSG = "Service is temporarily unavailable. Please try again later.";

        public static final String REQUIRED_FIELD_MSG = "Field is required.";
        public static final String INVALID_EMAIL_MSG = "Invalid email format.";
        public static final String INVALID_PHONE_MSG = "Invalid phone number format.";
        public static final String INVALID_PASSWORD_MSG = "Password does not meet requirements.";
        public static final String INVALID_USERNAME_MSG = "Username is invalid.";
        public static final String INVALID_LENGTH_MSG = "Invalid length.";
        public static final String INVALID_RANGE_MSG = "Value is out of valid range.";
        public static final String INVALID_FORMAT_MSG = "Invalid format.";
        public static final String INVALID_DATE_MSG = "Invalid date format.";
        public static final String INVALID_UUID_MSG = "Invalid UUID format.";
        public static final String INVALID_URL_MSG = "Invalid URL format.";
        public static final String INVALID_JSON_MSG = "Invalid JSON format.";
        public static final String DUPLICATE_VALUE_MSG = "Value already exists.";

        public static final String USER_NOT_FOUND_MSG = "User not found.";
        public static final String USER_ALREADY_EXISTS_MSG = "User already exists.";
        public static final String INVALID_CREDENTIALS_MSG = "Invalid username or password.";
        public static final String ACCOUNT_LOCKED_MSG = "Account is locked. Please contact support.";
        public static final String EMAIL_NOT_VERIFIED_MSG = "Email address not verified.";
        public static final String PASSWORD_EXPIRED_MSG = "Password has expired. Please reset it.";

        public static final String TENANT_NOT_FOUND_MSG = "Tenant not found.";
        public static final String TENANT_ALREADY_EXISTS_MSG = "Tenant already exists.";
        public static final String TENANT_ACCESS_DENIED_MSG = "Access denied to tenant.";

        public static final String APP_NOT_FOUND_MSG = "Application not found.";
        public static final String APP_NOT_SUBSCRIBED_MSG = "Not subscribed to this application.";
        public static final String APP_ACCESS_DENIED_MSG = "Access denied to application.";

        public static final String WALLET_NOT_FOUND_MSG = "Wallet not found.";
        public static final String INSUFFICIENT_BALANCE_MSG = "Insufficient balance.";
        public static final String TRANSACTION_FAILED_MSG = "Transaction failed.";
        public static final String INVALID_AMOUNT_MSG = "Invalid amount.";

        public static final String TOKEN_EXPIRED_MSG = "Authentication token has expired.";
        public static final String TOKEN_INVALID_MSG = "Invalid authentication token.";
        public static final String PERMISSION_DENIED_MSG = "Permission denied.";
    }

    public static final class Logging {
        public static final String ERROR_LOG_FORMAT = "Error Code: {}, Message: {}, Path: {}, Timestamp: {}";
        public static final String VALIDATION_ERROR_LOG_FORMAT = "Validation Error: Field: {}, Message: {}, Value: {}";
        public static final String BUSINESS_ERROR_LOG_FORMAT = "Business Error: Code: {}, Message: {}, Context: {}";
        public static final String SECURITY_ERROR_LOG_FORMAT = "Security Error: Code: {}, IP: {}, User: {}, Action: {}";
        public static final String INTEGRATION_ERROR_LOG_FORMAT = "Integration Error: Service: {}, Error: {}, Request: {}";
    }
}
