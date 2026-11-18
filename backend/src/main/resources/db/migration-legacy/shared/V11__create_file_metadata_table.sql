-- ============================================================
-- V11: Create file_metadata table for file management
-- ============================================================

CREATE TABLE IF NOT EXISTS file_metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_key VARCHAR(255),
    tenant_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(1000) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    title VARCHAR(255),
    description TEXT,
    category_id UUID NOT NULL,
    user_id BIGINT NOT NULL,
    code VARCHAR(100),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_file_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_file_metadata_tenant ON file_metadata(tenant_id);
CREATE INDEX IF NOT EXISTS idx_file_metadata_category ON file_metadata(category_id);
CREATE INDEX IF NOT EXISTS idx_file_metadata_user ON file_metadata(user_id);
CREATE INDEX IF NOT EXISTS idx_file_metadata_code ON file_metadata(code);

-- Create file_tags collection table for @ElementCollection
CREATE TABLE IF NOT EXISTS file_tags (
    file_id UUID NOT NULL,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY (file_id, tag),
    CONSTRAINT fk_file_tags_file FOREIGN KEY (file_id) REFERENCES file_metadata(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_file_tags_tag ON file_tags(tag);

-- Add comments
COMMENT ON TABLE file_metadata IS 'File metadata for uploaded files';
COMMENT ON COLUMN file_metadata.file_name IS 'Original file name';
COMMENT ON COLUMN file_metadata.file_url IS 'URL to access the file';
COMMENT ON COLUMN file_metadata.category_id IS 'Category reference';
COMMENT ON COLUMN file_metadata.user_id IS 'User who uploaded the file';
COMMENT ON COLUMN file_metadata.code IS 'File code for filtering';
COMMENT ON TABLE file_tags IS 'File tags collection';
