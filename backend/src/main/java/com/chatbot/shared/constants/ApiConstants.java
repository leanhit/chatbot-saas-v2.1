package com.chatbot.shared.constants;

public class ApiConstants {

    public static final String API_PREFIX = "/api";
    public static final String API_VERSION = "/v1";
    public static final String BASE_PATH = API_PREFIX + API_VERSION;

    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    public static final String TENANT_HEADER = "X-Tenant-ID";
    public static final String USER_HEADER = "X-User-ID";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    public static final String PAGINATION_PAGE = "page";
    public static final String PAGINATION_SIZE = "size";
    public static final String PAGINATION_SORT = "sort";
    public static final String PAGINATION_DIRECTION = "direction";

    public static final String DEFAULT_PAGE = "0";
    public static final String DEFAULT_SIZE = "20";
    public static final String DEFAULT_SORT = "id";
    public static final String DEFAULT_DIRECTION = "asc";

    public static final int MAX_PAGE_SIZE = 100;
    public static final int MIN_PAGE_SIZE = 1;

    public static final String SEARCH_PARAM = "search";
    public static final String FILTER_PARAM = "filter";
    public static final String FIELDS_PARAM = "fields";

    public static final class IdentityHub {
        public static final String PATH = "/identity";
        public static final String LOGIN = PATH + "/login";
        public static final String REGISTER = PATH + "/register";
        public static final String LOGOUT = PATH + "/logout";
        public static final String REFRESH = PATH + "/refresh";
        public static final String PROFILE = PATH + "/profile";
        public static final String CHANGE_PASSWORD = PATH + "/change-password";
        public static final String FORGOT_PASSWORD = PATH + "/forgot-password";
        public static final String RESET_PASSWORD = PATH + "/reset-password";
        public static final String VERIFY_EMAIL = PATH + "/verify-email";
        public static final String HEALTH = PATH + "/health";
    }

    public static final class UserHub {
        public static final String PATH = "/user";
        public static final String USERS = PATH + "/users";
        public static final String USER_ID = USERS + "/{id}";
        public static final String PROFILE = PATH + "/profile";
        public static final String PREFERENCES = PATH + "/preferences";
        public static final String ACTIVITIES = PATH + "/activities";
        public static final String ANALYTICS = PATH + "/analytics";
        public static final String ADDRESSES = PATH + "/addresses";
        public static final String SEARCH = PATH + "/search";
    }

    public static final class TenantHub {
        public static final String PATH = "/tenant";
        public static final String TENANTS = PATH + "/tenants";
        public static final String TENANT_ID = TENANTS + "/{id}";
        public static final String MEMBERS = PATH + "/members";
        public static final String MEMBERSHIP = PATH + "/membership";
        public static final String PROFILE = PATH + "/profile";
        public static final String PROFESSIONAL = PATH + "/professional";
        public static final String SEARCH = PATH + "/search";
        public static final String VALIDATE = PATH + "/validate";
    }

    public static final class AppHub {
        public static final String PATH = "/app";
        public static final String REGISTRY = PATH + "/registry";
        public static final String APPS = REGISTRY + "/apps";
        public static final String APP_ID = APPS + "/{id}";
        public static final String SUBSCRIPTION = PATH + "/subscription";
        public static final String SUBSCRIBE = SUBSCRIPTION + "/subscribe";
        public static final String UNSUBSCRIBE = SUBSCRIPTION + "/unsubscribe";
        public static final String GUARD = PATH + "/guard";
        public static final String VALIDATE = GUARD + "/validate";
        public static final String ENABLE = APP_ID + "/enable";
        public static final String DISABLE = APP_ID + "/disable";
    }

    public static final class BillingHub {
        public static final String PATH = "/billing";
        public static final String ACCOUNT = PATH + "/account";
        public static final String SUBSCRIPTION = PATH + "/subscription";
        public static final String ENTITLEMENT = PATH + "/entitlement";
        public static final String USAGE = PATH + "/usage";
        public static final String INVOICES = PATH + "/invoices";
        public static final String PAYMENTS = PATH + "/payments";
    }

    public static final class WalletHub {
        public static final String PATH = "/wallet";
        public static final String WALLETS = PATH + "/wallets";
        public static final String WALLET_ID = WALLETS + "/{id}";
        public static final String BALANCE = PATH + "/balance";
        public static final String TRANSACTION = PATH + "/transaction";
        public static final String TRANSACTIONS = TRANSACTION + "/transactions";
        public static final String TOPUP = PATH + "/topup";
        public static final String PURCHASE = PATH + "/purchase";
        public static final String LEDGER = PATH + "/ledger";
        public static final String TRANSFER = PATH + "/transfer";
    }

    public static final class ConfigHub {
        public static final String PATH = "/config";
        public static final String RUNTIME = PATH + "/runtime";
        public static final String ENVIRONMENT = PATH + "/environment";
        public static final String CACHE = PATH + "/cache";
        public static final String SETTINGS = PATH + "/settings";
        public static final String FEATURES = PATH + "/features";
    }

    public static final class MessageHub {
        public static final String PATH = "/message";
        public static final String ROUTER = PATH + "/router";
        public static final String DECISION = PATH + "/decision";
        public static final String PROCESSOR = PATH + "/processor";
        public static final String STORE = PATH + "/store";
        public static final String MESSAGES = STORE + "/messages";
        public static final String CONVERSATIONS = STORE + "/conversations";
        public static final String SEND = PATH + "/send";
        public static final String RECEIVE = PATH + "/receive";
    }

    public static final class FacebookSpoke {
        public static final String PATH = "/facebook";
        public static final String WEBHOOK = PATH + "/webhook";
        public static final String CONNECTION = PATH + "/connection";
        public static final String USER = PATH + "/user";
        public static final String AUTOCONNECT = PATH + "/autoconnect";
        public static final String VERIFY = WEBHOOK + "/verify";
        public static final String EVENTS = WEBHOOK + "/events";
    }

    public static final class BotpressSpoke {
        public static final String PATH = "/botpress";
        public static final String API = PATH + "/api";
        public static final String AUTH = PATH + "/auth";
        public static final String MAPPING = PATH + "/mapping";
        public static final String BOTS = API + "/bots";
        public static final String MESSAGES = API + "/messages";
        public static final String LOGIN = AUTH + "/login";
    }

    public static final class OdooSpoke {
        public static final String PATH = "/odoo";
        public static final String CLIENT = PATH + "/client";
        public static final String CUSTOMER = PATH + "/customer";
        public static final String PHONE = PATH + "/phone";
        public static final String STAGING = PATH + "/staging";
    }

    public static final class MinIOSpoke {
        public static final String PATH = "/minio";
        public static final String STORAGE = PATH + "/storage";
        public static final String FILES = STORAGE + "/files";
        public static final String IMAGES = STORAGE + "/images";
        public static final String METADATA = PATH + "/metadata";
        public static final String CATEGORIES = PATH + "/categories";
        public static final String UPLOAD = FILES + "/upload";
        public static final String DOWNLOAD = FILES + "/download";
    }

    public static final class HttpStatus {
        public static final int OK = 200;
        public static final int CREATED = 201;
        public static final int NO_CONTENT = 204;
        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int CONFLICT = 409;
        public static final int UNPROCESSABLE_ENTITY = 422;
        public static final int TOO_MANY_REQUESTS = 429;
        public static final int INTERNAL_SERVER_ERROR = 500;
        public static final int SERVICE_UNAVAILABLE = 503;
    }

    public static final class Cache {
        public static final String USER_PREFIX = "user:";
        public static final String TENANT_PREFIX = "tenant:";
        public static final String APP_PREFIX = "app:";
        public static final String CONFIG_PREFIX = "config:";
        public static final String SESSION_PREFIX = "session:";
        public static final String TOKEN_PREFIX = "token:";
        public static final String RATE_LIMIT_PREFIX = "rate_limit:";
        public static final String SEARCH_PREFIX = "search:";
        
        public static final long DEFAULT_TTL = 3600; // 1 hour
        public static final long SHORT_TTL = 300; // 5 minutes
        public static final long LONG_TTL = 86400; // 24 hours
        public static final long SESSION_TTL = 1800; // 30 minutes
    }

    public static final class Security {
        public static final String JWT_SECRET = "jwt.secret";
        public static final String JWT_EXPIRATION = "jwt.expiration";
        public static final String JWT_REFRESH_EXPIRATION = "jwt.refresh.expiration";
        public static final String PASSWORD_SALT = "password.salt";
        public static final String ENCRYPTION_KEY = "encryption.key";
        public static final String API_KEY_HEADER = "X-API-Key";
        public static final String CSRF_HEADER = "X-CSRF-Token";
    }

    public static final class Pagination {
        public static final String PAGE_NUMBER = "pageNumber";
        public static final String PAGE_SIZE = "pageSize";
        public static final String TOTAL_ELEMENTS = "totalElements";
        public static final String TOTAL_PAGES = "totalPages";
        public static final String FIRST = "first";
        public static final String LAST = "last";
        public static final String HAS_NEXT = "hasNext";
        public static final String HAS_PREVIOUS = "hasPrevious";
        public static final String SORT = "sort";
    }

    public static final class Response {
        public static final String SUCCESS = "success";
        public static final String ERROR = "error";
        public static final String MESSAGE = "message";
        public static final String DATA = "data";
        public static final String ERRORS = "errors";
        public static final String TIMESTAMP = "timestamp";
        public static final String PATH = "path";
        public static final String STATUS = "status";
        public static final String CODE = "code";
    }

    public static final class Validation {
        public static final String REQUIRED = "required";
        public static final String INVALID_FORMAT = "invalid.format";
        public static final String INVALID_LENGTH = "invalid.length";
        public static final String INVALID_RANGE = "invalid.range";
        public static final String DUPLICATE = "duplicate";
        public static final String NOT_FOUND = "not.found";
        public static final String ACCESS_DENIED = "access.denied";
    }
}
