-- ============================================================
-- V4: Add BaseTenantEntity columns to app tables
-- ============================================================

ALTER TABLE app_registry ADD COLUMN IF NOT EXISTS tenant_key VARCHAR(255);
ALTER TABLE app_registry ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE app_registry ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE app_registry ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE app_subscriptions ADD COLUMN IF NOT EXISTS tenant_key VARCHAR(255);
ALTER TABLE app_subscriptions ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE app_subscriptions ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE app_subscriptions ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE app_guards ADD COLUMN IF NOT EXISTS tenant_key VARCHAR(255);
ALTER TABLE app_guards ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE app_guards ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE app_guards ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
