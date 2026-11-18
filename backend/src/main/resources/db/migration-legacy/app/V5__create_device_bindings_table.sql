-- ============================================================
-- V5: Create device_bindings table for License device limit management
-- ============================================================

CREATE TABLE IF NOT EXISTS device_bindings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    device_name VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    activated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_seen_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_device_binding_user ON device_bindings(user_id);
CREATE INDEX IF NOT EXISTS idx_device_binding_device ON device_bindings(device_id);
CREATE INDEX IF NOT EXISTS idx_device_binding_user_device ON device_bindings(user_id, device_id);

COMMENT ON TABLE device_bindings IS 'Stores device binding and active session tracking for license enforcement';
