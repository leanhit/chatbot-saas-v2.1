-- ============================================================
-- V9: Create merchant payment gateway tables
-- ============================================================

-- Create merchant_api_keys table
CREATE TABLE IF NOT EXISTS merchant_api_keys (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    api_key_hash VARCHAR(255) UNIQUE NOT NULL,
    api_secret_hash VARCHAR(255) NOT NULL,
    webhook_secret VARCHAR(255) NOT NULL,
    webhook_url VARCHAR(500),
    rate_limit_per_minute INTEGER NOT NULL DEFAULT 100,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    usage_count BIGINT NOT NULL DEFAULT 0,
    last_used_at TIMESTAMP,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for merchant_api_keys
CREATE INDEX IF NOT EXISTS idx_merchant_api_keys_merchant_id ON merchant_api_keys(merchant_id);
CREATE INDEX IF NOT EXISTS idx_merchant_api_keys_tenant_id ON merchant_api_keys(tenant_id);
CREATE INDEX IF NOT EXISTS idx_merchant_api_keys_api_key_hash ON merchant_api_keys(api_key_hash);
CREATE INDEX IF NOT EXISTS idx_merchant_api_keys_is_active ON merchant_api_keys(is_active);

-- Create merchant_payment_sessions table
CREATE TABLE IF NOT EXISTS merchant_payment_sessions (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(100) UNIQUE NOT NULL,
    merchant_id BIGINT NOT NULL,
    merchant_order_id VARCHAR(100) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    description TEXT,
    return_url VARCHAR(500),
    cancel_url VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_code VARCHAR(100),
    bank_transaction_id VARCHAR(100),
    completed_at TIMESTAMP,
    expired_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for merchant_payment_sessions
CREATE INDEX IF NOT EXISTS idx_merchant_payment_sessions_session_id ON merchant_payment_sessions(session_id);
CREATE INDEX IF NOT EXISTS idx_merchant_payment_sessions_merchant_id ON merchant_payment_sessions(merchant_id);
CREATE INDEX IF NOT EXISTS idx_merchant_payment_sessions_merchant_order_id ON merchant_payment_sessions(merchant_order_id);
CREATE INDEX IF NOT EXISTS idx_merchant_payment_sessions_status ON merchant_payment_sessions(status);
CREATE INDEX IF NOT EXISTS idx_merchant_payment_sessions_payment_code ON merchant_payment_sessions(payment_code);

-- Create webhook_dispatch_logs table
CREATE TABLE IF NOT EXISTS webhook_dispatch_logs (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    session_id VARCHAR(100) NOT NULL,
    webhook_url VARCHAR(500) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 5,
    next_retry_at TIMESTAMP,
    error_message TEXT,
    request_payload TEXT,
    response_payload TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for webhook_dispatch_logs
CREATE INDEX IF NOT EXISTS idx_webhook_dispatch_logs_merchant_id ON webhook_dispatch_logs(merchant_id);
CREATE INDEX IF NOT EXISTS idx_webhook_dispatch_logs_session_id ON webhook_dispatch_logs(session_id);
CREATE INDEX IF NOT EXISTS idx_webhook_dispatch_logs_status ON webhook_dispatch_logs(status);
CREATE INDEX IF NOT EXISTS idx_webhook_dispatch_logs_next_retry_at ON webhook_dispatch_logs(next_retry_at);

-- Add comments
COMMENT ON TABLE merchant_api_keys IS 'Stores API keys for merchant payment gateway';
COMMENT ON TABLE merchant_payment_sessions IS 'Stores payment sessions for merchant transactions';
COMMENT ON TABLE webhook_dispatch_logs IS 'Stores webhook dispatch logs with retry logic';
