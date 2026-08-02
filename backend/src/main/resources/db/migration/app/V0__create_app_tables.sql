-- ============================================================
-- V0: Create base tables for app hub
-- ============================================================

-- Create app_registry table
CREATE TABLE IF NOT EXISTS app_registry (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    app_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    version VARCHAR(50),
    api_endpoint VARCHAR(500),
    webhook_url VARCHAR(500),
    config_schema TEXT,
    default_config TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_app_registry_tenant ON app_registry(tenant_id);
CREATE INDEX IF NOT EXISTS idx_app_registry_type ON app_registry(app_type);
CREATE INDEX IF NOT EXISTS idx_app_registry_status ON app_registry(status);
CREATE INDEX IF NOT EXISTS idx_app_registry_active ON app_registry(is_active);

-- Create app_configurations table
CREATE TABLE IF NOT EXISTS app_configurations (
    id BIGSERIAL PRIMARY KEY,
    app_id BIGINT NOT NULL,
    config_key VARCHAR(255) NOT NULL,
    config_value TEXT,
    config_type VARCHAR(50) NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_configurations_app FOREIGN KEY (app_id) REFERENCES app_registry(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_app_configurations_app ON app_configurations(app_id);
CREATE INDEX IF NOT EXISTS idx_app_configurations_key ON app_configurations(config_key);

-- Create app_subscriptions table
CREATE TABLE IF NOT EXISTS app_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    app_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    subscription_plan VARCHAR(50) NOT NULL,
    subscription_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    subscription_start TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subscription_end TIMESTAMP,
    auto_renew BOOLEAN NOT NULL DEFAULT FALSE,
    trial_end TIMESTAMP,
    config_data TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_app_subscriptions_tenant ON app_subscriptions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_app_subscriptions_app ON app_subscriptions(app_id);
CREATE INDEX IF NOT EXISTS idx_app_subscriptions_user ON app_subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_app_subscriptions_status ON app_subscriptions(subscription_status);

-- Create app_guards table
CREATE TABLE IF NOT EXISTS app_guards (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    app_id BIGINT NOT NULL,
    guard_name VARCHAR(255) NOT NULL,
    guard_type VARCHAR(50) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    priority INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_app_guards_tenant ON app_guards(tenant_id);
CREATE INDEX IF NOT EXISTS idx_app_guards_app ON app_guards(app_id);
CREATE INDEX IF NOT EXISTS idx_app_guards_type ON app_guards(guard_type);
CREATE INDEX IF NOT EXISTS idx_app_guards_active ON app_guards(is_active);

-- Create guard_rules table
CREATE TABLE IF NOT EXISTS guard_rules (
    id BIGSERIAL PRIMARY KEY,
    app_guard_id BIGINT NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    rule_condition TEXT NOT NULL,
    rule_action VARCHAR(255) NOT NULL,
    rule_parameters TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    priority INTEGER NOT NULL DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_guard_rules_app_guard FOREIGN KEY (app_guard_id) REFERENCES app_guards(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_guard_rules_app_guard ON guard_rules(app_guard_id);
CREATE INDEX IF NOT EXISTS idx_guard_rules_active ON guard_rules(is_active);

-- Create licenses table
CREATE TABLE IF NOT EXISTS licenses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_licenses_user ON licenses(user_id);
CREATE INDEX IF NOT EXISTS idx_licenses_active ON licenses(is_active);
CREATE INDEX IF NOT EXISTS idx_licenses_expires ON licenses(expires_at);

-- Create license_features collection table
CREATE TABLE IF NOT EXISTS license_features (
    license_id BIGINT NOT NULL,
    feature VARCHAR(255) NOT NULL,
    CONSTRAINT fk_license_features_license FOREIGN KEY (license_id) REFERENCES licenses(id) ON DELETE CASCADE,
    CONSTRAINT uk_license_features UNIQUE (license_id, feature)
);

CREATE INDEX IF NOT EXISTS idx_license_features_license ON license_features(license_id);

-- Create license_modules collection table
CREATE TABLE IF NOT EXISTS license_modules (
    license_id BIGINT NOT NULL,
    module VARCHAR(255) NOT NULL,
    CONSTRAINT fk_license_modules_license FOREIGN KEY (license_id) REFERENCES licenses(id) ON DELETE CASCADE,
    CONSTRAINT uk_license_modules UNIQUE (license_id, module)
);

CREATE INDEX IF NOT EXISTS idx_license_modules_license ON license_modules(license_id);

-- Create license_limits collection table
CREATE TABLE IF NOT EXISTS license_limits (
    license_id BIGINT NOT NULL,
    limit_key VARCHAR(255) NOT NULL,
    limit_value INTEGER NOT NULL,
    CONSTRAINT fk_license_limits_license FOREIGN KEY (license_id) REFERENCES licenses(id) ON DELETE CASCADE,
    CONSTRAINT uk_license_limits UNIQUE (license_id, limit_key)
);

CREATE INDEX IF NOT EXISTS idx_license_limits_license ON license_limits(license_id);
