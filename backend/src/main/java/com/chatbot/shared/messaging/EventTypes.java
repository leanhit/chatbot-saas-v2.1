package com.chatbot.shared.messaging;

public final class EventTypes {

    // User Events
    public static final String USER_REGISTERED = "user.registered";
    public static final String USER_UPDATED = "user.updated";
    public static final String USER_DELETED = "user.deleted";
    public static final String USER_LOGGED_IN = "user.logged_in";
    public static final String USER_LOGGED_OUT = "user.logged_out";
    public static final String USER_PASSWORD_CHANGED = "user.password_changed";
    public static final String USER_EMAIL_VERIFIED = "user.email_verified";
    public static final String USER_PASSWORD_RESET = "user.password_reset";
    public static final String USER_ACCOUNT_LOCKED = "user.account_locked";
    public static final String USER_ACCOUNT_UNLOCKED = "user.account_unlocked";
    public static final String USER_PROFILE_UPDATED = "user.profile_updated";
    public static final String USER_PREFERENCES_UPDATED = "user.preferences_updated";
    public static final String USER_SESSION_CREATED = "user.session_created";
    public static final String USER_SESSION_EXPIRED = "user.session_expired";
    public static final String USER_ACTIVITY_RECORDED = "user.activity_recorded";

    // Tenant Events
    public static final String TENANT_CREATED = "tenant.created";
    public static final String TENANT_UPDATED = "tenant.updated";
    public static final String TENANT_DELETED = "tenant.deleted";
    public static final String TENANT_ACTIVATED = "tenant.activated";
    public static final String TENANT_DEACTIVATED = "tenant.deactivated";
    public static final String TENANT_SUSPENDED = "tenant.suspended";
    public static final String TENANT_SETTINGS_UPDATED = "tenant.settings_updated";
    public static final String TENANT_MEMBER_ADDED = "tenant.member_added";
    public static final String TENANT_MEMBER_REMOVED = "tenant.member_removed";
    public static final String TENANT_MEMBER_ROLE_UPDATED = "tenant.member_role_updated";
    public static final String TENANT_PROFILE_UPDATED = "tenant.profile_updated";
    public static final String TENANT_LIMIT_UPDATED = "tenant.limit_updated";

    // App Events
    public static final String APP_REGISTERED = "app.registered";
    public static final String APP_UPDATED = "app.updated";
    public static final String APP_DELETED = "app.deleted";
    public static final String APP_ACTIVATED = "app.activated";
    public static final String APP_DEACTIVATED = "app.deactivated";
    public static final String APP_SUBSCRIBED = "app.subscribed";
    public static final String APP_UNSUBSCRIBED = "app.unsubscribed";
    public static final String APP_CONFIGURATION_UPDATED = "app.configuration_updated";
    public static final String APP_FEATURE_ENABLED = "app.feature_enabled";
    public static final String APP_FEATURE_DISABLED = "app.feature_disabled";
    public static final String APP_PERMISSION_GRANTED = "app.permission_granted";
    public static final String APP_PERMISSION_REVOKED = "app.permission_revoked";
    public static final String APP_USAGE_RECORDED = "app.usage_recorded";

    // Billing Events
    public static final String BILLING_ACCOUNT_CREATED = "billing.account_created";
    public static final String BILLING_ACCOUNT_UPDATED = "billing.account_updated";
    public static final String BILLING_ACCOUNT_DELETED = "billing.account_deleted";
    public static final String SUBSCRIPTION_CREATED = "subscription.created";
    public static final String SUBSCRIPTION_UPDATED = "subscription.updated";
    public static final String SUBSCRIPTION_CANCELLED = "subscription.cancelled";
    public static final String SUBSCRIPTION_RENEWED = "subscription.renewed";
    public static final String SUBSCRIPTION_EXPIRED = "subscription.expired";
    public static final String BILLING_CYCLE_COMPLETED = "billing.cycle_completed";
    public static final String INVOICE_GENERATED = "invoice.generated";
    public static final String INVOICE_SENT = "invoice.sent";
    public static final String INVOICE_PAID = "invoice.paid";
    public static final String INVOICE_OVERDUE = "invoice.overdue";
    public static final String PAYMENT_INITIATED = "payment.initiated";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String PAYMENT_REFUNDED = "payment.refunded";
    public static final String ENTITLEMENT_GRANTED = "entitlement.granted";
    public static final String ENTITLEMENT_REVOKED = "entitlement.revoked";
    public static final String USAGE_LIMIT_REACHED = "usage.limit_reached";

    // Wallet Events
    public static final String WALLET_CREATED = "wallet.created";
    public static final String WALLET_UPDATED = "wallet.updated";
    public static final String WALLET_DELETED = "wallet.deleted";
    public static final String WALLET_ACTIVATED = "wallet.activated";
    public static final String WALLET_DEACTIVATED = "wallet.deactivated";
    public static final String BALANCE_UPDATED = "balance.updated";
    public static final String TRANSACTION_INITIATED = "transaction.initiated";
    public static final String TRANSACTION_COMPLETED = "transaction.completed";
    public static final String TRANSACTION_FAILED = "transaction.failed";
    public static final String TRANSACTION_CANCELLED = "transaction.cancelled";
    public static final String TRANSACTION_REFUNDED = "transaction.refunded";
    public static final String TOPUP_INITIATED = "topup.initiated";
    public static final String TOPUP_COMPLETED = "topup.completed";
    public static final String TOPUP_FAILED = "topup.failed";
    public static final String PURCHASE_INITIATED = "purchase.initiated";
    public static final String PURCHASE_COMPLETED = "purchase.completed";
    public static final String PURCHASE_FAILED = "purchase.failed";
    public static final String TRANSFER_INITIATED = "transfer.initiated";
    public static final String TRANSFER_COMPLETED = "transfer.completed";
    public static final String TRANSFER_FAILED = "transfer.failed";
    public static final String LEDGER_ENTRY_CREATED = "ledger.entry_created";

    // Config Events
    public static final String CONFIG_CREATED = "config.created";
    public static final String CONFIG_UPDATED = "config.updated";
    public static final String CONFIG_DELETED = "config.deleted";
    public static final String CONFIG_ACTIVATED = "config.activated";
    public static final String CONFIG_DEACTIVATED = "config.deactivated";
    public static final String RUNTIME_CONFIG_UPDATED = "runtime.config.updated";
    public static final String ENVIRONMENT_CONFIG_UPDATED = "environment.config.updated";
    public static final String FEATURE_FLAG_UPDATED = "feature.flag_updated";
    public static final String SYSTEM_SETTINGS_UPDATED = "system.settings_updated";
    public static final String CACHE_INVALIDATED = "cache.invalidated";
    public static final String CACHE_REFRESHED = "cache.refreshed";

    // Message Events
    public static final String MESSAGE_SENT = "message.sent";
    public static final String MESSAGE_RECEIVED = "message.received";
    public static final String MESSAGE_DELIVERED = "message.delivered";
    public static final String MESSAGE_READ = "message.read";
    public static final String MESSAGE_FAILED = "message.failed";
    public static final String MESSAGE_ROUTED = "message.routed";
    public static final String MESSAGE_PROCESSED = "message.processed";
    public static final String CONVERSATION_STARTED = "conversation.started";
    public static final String CONVERSATION_ENDED = "conversation.ended";
    public static final String CONVERSATION_UPDATED = "conversation.updated";
    public static final String ROUTING_RULE_UPDATED = "routing.rule_updated";
    public static final String PROCESSING_RULE_UPDATED = "processing.rule_updated";

    // Integration Events
    public static final String FACEBOOK_WEBHOOK_RECEIVED = "facebook.webhook.received";
    public static final String FACEBOOK_WEBHOOK_PROCESSED = "facebook.webhook.processed";
    public static final String FACEBOOK_CONNECTION_ESTABLISHED = "facebook.connection.established";
    public static final String FACEBOOK_CONNECTION_LOST = "facebook.connection.lost";
    public static final String FACEBOOK_USER_CONNECTED = "facebook.user.connected";
    public static final String FACEBOOK_USER_DISCONNECTED = "facebook.user.disconnected";
    public static final String BOTPRESS_MESSAGE_SENT = "botpress.message.sent";
    public static final String BOTPRESS_MESSAGE_RECEIVED = "botpress.message.received";
    public static final String BOTPRESS_AUTH_SUCCESSFUL = "botpress.auth.successful";
    public static final String BOTPRESS_AUTH_FAILED = "botpress.auth.failed";
    public static final String BOTPRESS_MAPPING_UPDATED = "botpress.mapping.updated";
    public static final String ODOO_SYNC_STARTED = "odoo.sync.started";
    public static final String ODOO_SYNC_COMPLETED = "odoo.sync.completed";
    public static final String ODOO_SYNC_FAILED = "odoo.sync.failed";
    public static final String ODOO_CUSTOMER_CREATED = "odoo.customer.created";
    public static final String ODOO_CUSTOMER_UPDATED = "odoo.customer.updated";
    public static final String MINIO_FILE_UPLOADED = "minio.file.uploaded";
    public static final String MINIO_FILE_DOWNLOADED = "minio.file.downloaded";
    public static final String MINIO_FILE_DELETED = "minio.file.deleted";
    public static final String MINIO_STORAGE_CONFIG_UPDATED = "minio.storage.config.updated";

    // System Events
    public static final String SYSTEM_STARTED = "system.started";
    public static final String SYSTEM_STOPPED = "system.stopped";
    public static final String SYSTEM_RESTARTED = "system.restarted";
    public static final String HEALTH_CHECK_PASSED = "health.check.passed";
    public static final String HEALTH_CHECK_FAILED = "health.check.failed";
    public static final String METRICS_COLLECTED = "metrics.collected";
    public static final String ALERT_TRIGGERED = "alert.triggered";
    public static final String ALERT_RESOLVED = "alert.resolved";
    public static final String BACKUP_STARTED = "backup.started";
    public static final String BACKUP_COMPLETED = "backup.completed";
    public static final String BACKUP_FAILED = "backup.failed";
    public static final String CLEANUP_STARTED = "cleanup.started";
    public static final String CLEANUP_COMPLETED = "cleanup.completed";
    public static final String CLEANUP_FAILED = "cleanup.failed";
    public static final String MAINTENANCE_STARTED = "maintenance.started";
    public static final String MAINTENANCE_COMPLETED = "maintenance.completed";

    // Security Events
    public static final String SECURITY_INCIDENT = "security.incident";
    public static final String SECURITY_BREACH = "security.breach";
    public static final String RATE_LIMIT_EXCEEDED = "rate.limit.exceeded";
    public static final String UNAUTHORIZED_ACCESS = "unauthorized.access";
    public static final String SUSPICIOUS_ACTIVITY = "suspicious.activity";
    public static final String LOGIN_ATTEMPT_FAILED = "login.attempt.failed";
    public static final String LOGIN_ATTEMPT_BLOCKED = "login.attempt.blocked";
    public static final String PASSWORD_CHANGE_REQUESTED = "password.change.requested";
    public static final String EMAIL_VERIFICATION_REQUESTED = "email.verification.requested";

    // Performance Events
    public static final String PERFORMANCE_ISSUE = "performance.issue";
    public static final String SLOW_QUERY_DETECTED = "slow.query.detected";
    public static final String MEMORY_USAGE_HIGH = "memory.usage.high";
    public static final String CPU_USAGE_HIGH = "cpu.usage.high";
    public static final String DISK_USAGE_HIGH = "disk.usage.high";
    public static final String DATABASE_CONNECTION_POOL_EXHAUSTED = "database.connection.pool.exhausted";
    public static final String THREAD_POOL_EXHAUSTED = "thread.pool.exhausted";

    // Business Events
    public static final String AUDIT_LOG_RECORDED = "audit.log.recorded";
    public static final String COMPLIANCE_CHECK_PASSED = "compliance.check.passed";
    public static final String COMPLIANCE_CHECK_FAILED = "compliance.check.failed";
    public static final String DATA_RETENTION_EXECUTED = "data.retention.executed";
    public static final String GDPR_REQUEST_RECEIVED = "gdpr.request.received";
    public static final String GDPR_REQUEST_PROCESSED = "gdpr.request.processed";

    // Scheduled Task Events
    public static final String SCHEDULED_TASK_STARTED = "scheduled.task.started";
    public static final String SCHEDULED_TASK_COMPLETED = "scheduled.task.completed";
    public static final String SCHEDULED_TASK_FAILED = "scheduled.task.failed";
    public static final String SCHEDULED_TASK_CANCELLED = "scheduled.task.cancelled";

    // API Events
    public static final String API_REQUEST_RECEIVED = "api.request.received";
    public static final String API_REQUEST_COMPLETED = "api.request.completed";
    public static final String API_REQUEST_FAILED = "api.request.failed";
    public static final String API_RATE_LIMIT_HIT = "api.rate.limit.hit";
    public static final String API_ERROR_OCCURRED = "api.error.occurred";

    // Notification Events
    public static final String NOTIFICATION_SENT = "notification.sent";
    public static final String NOTIFICATION_DELIVERED = "notification.delivered";
    public static final String NOTIFICATION_FAILED = "notification.failed";
    public static final String EMAIL_SENT = "email.sent";
    public static final String EMAIL_DELIVERED = "email.delivered";
    public static final String EMAIL_FAILED = "email.failed";
    public static final String SMS_SENT = "sms.sent";
    public static final String SMS_DELIVERED = "sms.delivered";
    public static final String SMS_FAILED = "sms.failed";
    public static final String PUSH_NOTIFICATION_SENT = "push.notification.sent";
    public static final String PUSH_NOTIFICATION_DELIVERED = "push.notification.delivered";
    public static final String PUSH_NOTIFICATION_FAILED = "push.notification.failed";

    // Analytics Events
    public static final String ANALYTICS_DATA_COLLECTED = "analytics.data.collected";
    public static final String ANALYTICS_REPORT_GENERATED = "analytics.report.generated";
    public static final String USER_BEHAVIOR_TRACKED = "user.behavior.tracked";
    public static final String USAGE_STATISTICS_UPDATED = "usage.statistics.updated";
    public static final String DASHBOARD_DATA_REFRESHED = "dashboard.data.refreshed";

    // Integration Events
    public static final String INTEGRATION_CONNECTED = "integration.connected";
    public static final String INTEGRATION_DISCONNECTED = "integration.disconnected";
    public static final String INTEGRATION_SYNC_STARTED = "integration.sync.started";
    public static final String INTEGRATION_SYNC_COMPLETED = "integration.sync.completed";
    public static final String INTEGRATION_SYNC_FAILED = "integration.sync.failed";
    public static final String INTEGRATION_ERROR_OCCURRED = "integration.error.occurred";

    // Custom Events (for extensibility)
    public static final String CUSTOM_EVENT_PREFIX = "custom.";
    public static final String BUSINESS_EVENT_PREFIX = "business.";
    public static final String WORKFLOW_EVENT_PREFIX = "workflow.";
    public static final String EXTERNAL_EVENT_PREFIX = "external.";

    private EventTypes() {
        // Utility class - prevent instantiation
    }

    public static boolean isUserEvent(String eventType) {
        return eventType != null && eventType.startsWith("user.");
    }

    public static boolean isTenantEvent(String eventType) {
        return eventType != null && eventType.startsWith("tenant.");
    }

    public static boolean isAppEvent(String eventType) {
        return eventType != null && eventType.startsWith("app.");
    }

    public static boolean isBillingEvent(String eventType) {
        return eventType != null && eventType.startsWith("billing.") || 
               eventType.startsWith("subscription.") || 
               eventType.startsWith("payment.") || 
               eventType.startsWith("invoice.") || 
               eventType.startsWith("entitlement.");
    }

    public static boolean isWalletEvent(String eventType) {
        return eventType != null && eventType.startsWith("wallet.") || 
               eventType.startsWith("balance.") || 
               eventType.startsWith("transaction.") || 
               eventType.startsWith("topup.") || 
               eventType.startsWith("purchase.") || 
               eventType.startsWith("transfer.") || 
               eventType.startsWith("ledger.");
    }

    public static boolean isConfigEvent(String eventType) {
        return eventType != null && eventType.startsWith("config.") || 
               eventType.startsWith("runtime.") || 
               eventType.startsWith("environment.") || 
               eventType.startsWith("feature.") || 
               eventType.startsWith("cache.");
    }

    public static boolean isMessageEvent(String eventType) {
        return eventType != null && eventType.startsWith("message.") || 
               eventType.startsWith("conversation.") || 
               eventType.startsWith("routing.") || 
               eventType.startsWith("processing.");
    }

    public static boolean isIntegrationEvent(String eventType) {
        return eventType != null && 
               (eventType.startsWith("facebook.") || 
                eventType.startsWith("botpress.") || 
                eventType.startsWith("odoo.") || 
                eventType.startsWith("minio."));
    }

    public static boolean isSystemEvent(String eventType) {
        return eventType != null && eventType.startsWith("system.") || 
               eventType.startsWith("health.") || 
               eventType.startsWith("metrics.") || 
               eventType.startsWith("alert.") || 
               eventType.startsWith("backup.") || 
               eventType.startsWith("cleanup.") || 
               eventType.startsWith("maintenance.");
    }

    public static boolean isSecurityEvent(String eventType) {
        return eventType != null && eventType.startsWith("security.") || 
               eventType.startsWith("rate.") || 
               eventType.startsWith("unauthorized.") || 
               eventType.startsWith("suspicious.") || 
               eventType.startsWith("login.") || 
               eventType.startsWith("password.") || 
               eventType.startsWith("email.");
    }

    public static boolean isPerformanceEvent(String eventType) {
        return eventType != null && eventType.startsWith("performance.") || 
               eventType.startsWith("slow.") || 
               eventType.startsWith("memory.") || 
               eventType.startsWith("cpu.") || 
               eventType.startsWith("disk.") || 
               eventType.startsWith("database.") || 
               eventType.startsWith("thread.");
    }

    public static boolean isNotificationEvent(String eventType) {
        return eventType != null && eventType.startsWith("notification.") || 
               eventType.startsWith("email.") || 
               eventType.startsWith("sms.") || 
               eventType.startsWith("push.");
    }

    public static boolean isCriticalEvent(String eventType) {
        return isSecurityEvent(eventType) || 
               isSystemEvent(eventType) || 
               isPerformanceEvent(eventType) ||
               eventType.equals(ALERT_TRIGGERED) ||
               eventType.equals(SECURITY_BREACH) ||
               eventType.equals(SYSTEM_STOPPED);
    }
}
