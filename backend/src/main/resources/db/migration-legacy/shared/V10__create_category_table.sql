-- ============================================================
-- V10: Create category table for file categorization
-- ============================================================

CREATE TABLE IF NOT EXISTS category (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_key VARCHAR(255),
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_category_tenant ON category(tenant_id);
CREATE INDEX IF NOT EXISTS idx_category_name ON category(name);

-- Add comments
COMMENT ON TABLE category IS 'File category for organizing uploaded files';
COMMENT ON COLUMN category.name IS 'Category name (unique)';
