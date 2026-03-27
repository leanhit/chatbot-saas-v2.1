-- Create updated_at function if not exists
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create App Guards Table
CREATE TABLE IF NOT EXISTS app_guards (
    id BIGSERIAL PRIMARY KEY,
    app_id BIGINT NOT NULL REFERENCES app_registry(id) ON DELETE CASCADE,
    tenant_id BIGINT NOT NULL,
    guard_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    rules JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_guards_app_id ON app_guards(app_id);
CREATE INDEX IF NOT EXISTS idx_guards_tenant_id ON app_guards(tenant_id);
CREATE INDEX IF NOT EXISTS idx_guards_type ON app_guards(guard_type);

-- Create trigger for updated_at
CREATE TRIGGER update_app_guards_updated_at 
    BEFORE UPDATE ON app_guards 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
