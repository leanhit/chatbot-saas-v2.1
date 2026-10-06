-- ============================================================
-- V1: Create category, file_metadata and file_tags tables for MinIO image/file storage
-- ============================================================

CREATE TABLE IF NOT EXISTS category (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    tenant_key VARCHAR(255),
    tenant_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_category_tenant ON category(tenant_id);
CREATE INDEX IF NOT EXISTS idx_category_tenant_key ON category(tenant_key);
CREATE INDEX IF NOT EXISTS idx_category_name ON category(name);

CREATE TABLE IF NOT EXISTS file_metadata (
    id UUID PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(255),
    title VARCHAR(255),
    description TEXT,
    category_id UUID NOT NULL REFERENCES category(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    code VARCHAR(100),
    tenant_key VARCHAR(255),
    tenant_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_file_metadata_tenant ON file_metadata(tenant_id);
CREATE INDEX IF NOT EXISTS idx_file_metadata_category ON file_metadata(category_id);
CREATE INDEX IF NOT EXISTS idx_file_metadata_user ON file_metadata(user_id);

CREATE TABLE IF NOT EXISTS file_tags (
    file_id UUID NOT NULL REFERENCES file_metadata(id) ON DELETE CASCADE,
    tag VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_file_tags_file ON file_tags(file_id);
