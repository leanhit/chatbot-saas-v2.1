package com.chatbot.shared.constants;

public class CacheConstants {

    public static final String CACHE_PREFIX = "chatbot:";
    public static final String SEPARATOR = ":";

    public static final class User {
        public static final String PREFIX = CACHE_PREFIX + "user" + SEPARATOR;
        public static final String USER_BY_ID = PREFIX + "id" + SEPARATOR;
        public static final String USER_BY_EMAIL = PREFIX + "email" + SEPARATOR;
        public static final String USER_BY_USERNAME = PREFIX + "username" + SEPARATOR;
        public static final String USER_PROFILE = PREFIX + "profile" + SEPARATOR;
        public static final String USER_PREFERENCES = PREFIX + "preferences" + SEPARATOR;
        public static final String USER_PERMISSIONS = PREFIX + "permissions" + SEPARATOR;
        public static final String USER_ROLES = PREFIX + "roles" + SEPARATOR;
        public static final String USER_SESSION = PREFIX + "session" + SEPARATOR;
        public static final String USER_ACTIVITIES = PREFIX + "activities" + SEPARATOR;
        public static final String USER_ANALYTICS = PREFIX + "analytics" + SEPARATOR;
    }

    public static final class Tenant {
        public static final String PREFIX = CACHE_PREFIX + "tenant" + SEPARATOR;
        public static final String TENANT_BY_ID = PREFIX + "id" + SEPARATOR;
        public static final String TENANT_BY_DOMAIN = PREFIX + "domain" + SEPARATOR;
        public static final String TENANT_BY_NAME = PREFIX + "name" + SEPARATOR;
        public static final String TENANT_PROFILE = PREFIX + "profile" + SEPARATOR;
        public static final String TENANT_MEMBERS = PREFIX + "members" + SEPARATOR;
        public static final String TENANT_ROLES = PREFIX + "roles" + SEPARATOR;
        public static final String TENANT_SETTINGS = PREFIX + "settings" + SEPARATOR;
        public static final String TENANT_CONFIG = PREFIX + "config" + SEPARATOR;
        public static final String TENANT_SUBSCRIPTIONS = PREFIX + "subscriptions" + SEPARATOR;
    }

    public static final class App {
        public static final String PREFIX = CACHE_PREFIX + "app" + SEPARATOR;
        public static final String APP_BY_ID = PREFIX + "id" + SEPARATOR;
        public static final String APP_BY_NAME = PREFIX + "name" + SEPARATOR;
        public static final String APP_REGISTRY = PREFIX + "registry" + SEPARATOR;
        public static final String APP_CONFIG = PREFIX + "config" + SEPARATOR;
        public static final String APP_SUBSCRIPTIONS = PREFIX + "subscriptions" + SEPARATOR;
        public static final String APP_GUARDS = PREFIX + "guards" + SEPARATOR;
        public static final String APP_FEATURES = PREFIX + "features" + SEPARATOR;
        public static final String APP_PERMISSIONS = PREFIX + "permissions" + SEPARATOR;
    }

    public static final class Billing {
        public static final String PREFIX = CACHE_PREFIX + "billing" + SEPARATOR;
        public static final String BILLING_ACCOUNT = PREFIX + "account" + SEPARATOR;
        public static final String SUBSCRIPTION = PREFIX + "subscription" + SEPARATOR;
        public static final String ENTITLEMENT = PREFIX + "entitlement" + SEPARATOR;
        public static final String USAGE = PREFIX + "usage" + SEPARATOR;
        public static final String INVOICE = PREFIX + "invoice" + SEPARATOR;
        public static final String PAYMENT = PREFIX + "payment" + SEPARATOR;
        public static final String PLAN = PREFIX + "plan" + SEPARATOR;
        public static final String PRICING = PREFIX + "pricing" + SEPARATOR;
    }

    public static final class Wallet {
        public static final String PREFIX = CACHE_PREFIX + "wallet" + SEPARATOR;
        public static final String WALLET_BY_ID = PREFIX + "id" + SEPARATOR;
        public static final String WALLET_BY_USER = PREFIX + "user" + SEPARATOR;
        public static final String WALLET_BALANCE = PREFIX + "balance" + SEPARATOR;
        public static final String TRANSACTION = PREFIX + "transaction" + SEPARATOR;
        public static final String TRANSACTIONS = PREFIX + "transactions" + SEPARATOR;
        public static final String LEDGER = PREFIX + "ledger" + SEPARATOR;
        public static final String EXCHANGE_RATES = PREFIX + "exchange" + SEPARATOR;
        public static final String CURRENCY = PREFIX + "currency" + SEPARATOR;
    }

    public static final class Config {
        public static final String PREFIX = CACHE_PREFIX + "config" + SEPARATOR;
        public static final String RUNTIME_CONFIG = PREFIX + "runtime" + SEPARATOR;
        public static final String ENVIRONMENT_CONFIG = PREFIX + "environment" + SEPARATOR;
        public static final String FEATURE_FLAGS = PREFIX + "features" + SEPARATOR;
        public static final String SYSTEM_SETTINGS = PREFIX + "system" + SEPARATOR;
        public static final String APP_SETTINGS = PREFIX + "app" + SEPARATOR;
        public static final String USER_SETTINGS = PREFIX + "user" + SEPARATOR;
        public static final String TENANT_SETTINGS = PREFIX + "tenant" + SEPARATOR;
    }

    public static final class Message {
        public static final String PREFIX = CACHE_PREFIX + "message" + SEPARATOR;
        public static final String MESSAGE_BY_ID = PREFIX + "id" + SEPARATOR;
        public static final String CONVERSATION = PREFIX + "conversation" + SEPARATOR;
        public static final String ROUTING_RULES = PREFIX + "routing" + SEPARATOR;
        public static final String PROCESSING_RULES = PREFIX + "processing" + SEPARATOR;
        public static final String TEMPLATES = PREFIX + "templates" + SEPARATOR;
        public static final String QUEUED_MESSAGES = PREFIX + "queued" + SEPARATOR;
        public static final String FAILED_MESSAGES = PREFIX + "failed" + SEPARATOR;
    }

    public static final class Security {
        public static final String PREFIX = CACHE_PREFIX + "security" + SEPARATOR;
        public static final String JWT_TOKEN = PREFIX + "jwt" + SEPARATOR;
        public static final String REFRESH_TOKEN = PREFIX + "refresh" + SEPARATOR;
        public static final String API_KEY = PREFIX + "apikey" + SEPARATOR;
        public static final String SESSION = PREFIX + "session" + SEPARATOR;
        public static final String CSRF_TOKEN = PREFIX + "csrf" + SEPARATOR;
        public static final String PASSWORD_RESET = PREFIX + "reset" + SEPARATOR;
        public static final String EMAIL_VERIFICATION = PREFIX + "verify" + SEPARATOR;
        public static final String LOGIN_ATTEMPTS = PREFIX + "attempts" + SEPARATOR;
        public static final String BLOCKED_IPS = PREFIX + "blocked" + SEPARATOR;
    }

    public static final class Integration {
        public static final String PREFIX = CACHE_PREFIX + "integration" + SEPARATOR;
        public static final String FACEBOOK_TOKEN = PREFIX + "facebook" + SEPARATOR + "token" + SEPARATOR;
        public static final String FACEBOOK_WEBHOOK = PREFIX + "facebook" + SEPARATOR + "webhook" + SEPARATOR;
        public static final String BOTPRESS_TOKEN = PREFIX + "botpress" + SEPARATOR + "token" + SEPARATOR;
        public static final String BOTPRESS_MAPPING = PREFIX + "botpress" + SEPARATOR + "mapping" + SEPARATOR;
        public static final String ODOO_SESSION = PREFIX + "odoo" + SEPARATOR + "session" + SEPARATOR;
        public static final String ODOO_DATA = PREFIX + "odoo" + SEPARATOR + "data" + SEPARATOR;
        public static final String MINIO_CONFIG = PREFIX + "minio" + SEPARATOR + "config" + SEPARATOR;
        public static final String MINIO_TOKENS = PREFIX + "minio" + SEPARATOR + "tokens" + SEPARATOR;
    }

    public static final class RateLimit {
        public static final String PREFIX = CACHE_PREFIX + "ratelimit" + SEPARATOR;
        public static final String API_REQUESTS = PREFIX + "api" + SEPARATOR;
        public static final String LOGIN_REQUESTS = PREFIX + "login" + SEPARATOR;
        public static final String PASSWORD_RESET_REQUESTS = PREFIX + "reset" + SEPARATOR;
        public static final String EMAIL_REQUESTS = PREFIX + "email" + SEPARATOR;
        public static final String SMS_REQUESTS = PREFIX + "sms" + SEPARATOR;
        public static final String UPLOAD_REQUESTS = PREFIX + "upload" + SEPARATOR;
        public static final String WEBHOOK_REQUESTS = PREFIX + "webhook" + SEPARATOR;
    }

    public static final class Search {
        public static final String PREFIX = CACHE_PREFIX + "search" + SEPARATOR;
        public static final String USER_SEARCH = PREFIX + "user" + SEPARATOR;
        public static final String TENANT_SEARCH = PREFIX + "tenant" + SEPARATOR;
        public static final String APP_SEARCH = PREFIX + "app" + SEPARATOR;
        public static final String MESSAGE_SEARCH = PREFIX + "message" + SEPARATOR;
        public static final String TRANSACTION_SEARCH = PREFIX + "transaction" + SEPARATOR;
        public static final String SUGGESTIONS = PREFIX + "suggestions" + SEPARATOR;
        public static final String RECENT_SEARCHES = PREFIX + "recent" + SEPARATOR;
    }

    public static final class Analytics {
        public static final String PREFIX = CACHE_PREFIX + "analytics" + SEPARATOR;
        public static final String USER_STATS = PREFIX + "user" + SEPARATOR;
        public static final String TENANT_STATS = PREFIX + "tenant" + SEPARATOR;
        public static final String APP_STATS = PREFIX + "app" + SEPARATOR;
        public static final String MESSAGE_STATS = PREFIX + "message" + SEPARATOR;
        public static final String TRANSACTION_STATS = PREFIX + "transaction" + SEPARATOR;
        public static final String SYSTEM_STATS = PREFIX + "system" + SEPARATOR;
        public static final String DASHBOARD_DATA = PREFIX + "dashboard" + SEPARATOR;
        public static final String REPORTS = PREFIX + "reports" + SEPARATOR;
    }

    public static final class External {
        public static final String PREFIX = CACHE_PREFIX + "external" + SEPARATOR;
        public static final String EXCHANGE_RATES = PREFIX + "exchange" + SEPARATOR;
        public static final String COUNTRY_CODES = PREFIX + "countries" + SEPARATOR;
        public static final String TIME_ZONES = PREFIX + "timezones" + SEPARATOR;
        public static final String LANGUAGES = PREFIX + "languages" + SEPARATOR;
        public static final String CURRENCIES = PREFIX + "currencies" + SEPARATOR;
        public static final String PHONE_CODES = PREFIX + "phonecodes" + SEPARATOR;
        public static final String POSTAL_CODES = PREFIX + "postal" + SEPARATOR;
    }

    public static final class TTL {
        public static final long SHORT = 300; // 5 minutes
        public static final long MEDIUM = 1800; // 30 minutes
        public static final long LONG = 3600; // 1 hour
        public static final long VERY_LONG = 86400; // 24 hours
        public static final long SESSION = 1800; // 30 minutes
        public static final long TOKEN = 3600; // 1 hour
        public static final long REFRESH_TOKEN = 604800; // 7 days
        public static final long PASSWORD_RESET = 900; // 15 minutes
        public static final long EMAIL_VERIFICATION = 86400; // 24 hours
        public static final long RATE_LIMIT = 60; // 1 minute
        public static final long ANALYTICS = 300; // 5 minutes
        public static final long EXTERNAL_DATA = 86400; // 24 hours
    }

    public static final class Keys {
        public static final String WILDCARD = "*";
        public static final String ALL_KEYS = CACHE_PREFIX + "*";
        
        public static String buildKey(String... parts) {
            return String.join(SEPARATOR, parts);
        }
        
        public static String buildPattern(String prefix) {
            return prefix + SEPARATOR + WILDCARD;
        }
        
        public static String buildUserKey(Long userId, String type) {
            return buildKey(User.PREFIX, type, userId.toString());
        }
        
        public static String buildTenantKey(Long tenantId, String type) {
            return buildKey(Tenant.PREFIX, type, tenantId.toString());
        }
        
        public static String buildAppKey(Long appId, String type) {
            return buildKey(App.PREFIX, type, appId.toString());
        }
        
        public static String buildSecurityKey(String identifier, String type) {
            return buildKey(Security.PREFIX, type, identifier);
        }
        
        public static String buildRateLimitKey(String identifier, String type) {
            return buildKey(RateLimit.PREFIX, type, identifier);
        }
    }

    public static final class Operations {
        public static final String CLEAR_ALL = "CLEAR_ALL";
        public static final String CLEAR_BY_PATTERN = "CLEAR_BY_PATTERN";
        public static final String CLEAR_BY_PREFIX = "CLEAR_BY_PREFIX";
        public static final String EVICT_EXPIRED = "EVICT_EXPIRED";
        public static final String WARM_UP = "WARM_UP";
        public static final String REFRESH = "REFRESH";
        public static final String INVALIDATE = "INVALIDATE";
        public static final String SYNC = "SYNC";
    }
}
