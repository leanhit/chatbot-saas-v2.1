-- ============================================================
-- V15: Create penny_knowledge_documents table for managing uploaded files
-- ============================================================

DO $$
BEGIN
    -- Only create table if penny_bots exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'penny_bots') THEN
        CREATE TABLE IF NOT EXISTS penny_knowledge_documents (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            bot_id UUID NOT NULL,
            tenant_id BIGINT NOT NULL,
            document_name VARCHAR(500) NOT NULL,
            file_name VARCHAR(500) NOT NULL,
            file_type VARCHAR(50) NOT NULL, -- PDF, DOCX, XLSX
            file_size BIGINT NOT NULL,
            file_path VARCHAR(1000), -- Path to stored file (MinIO/S3)
            file_url VARCHAR(1000), -- Public URL for file access
            status VARCHAR(50) DEFAULT 'PROCESSING', -- PROCESSING, COMPLETED, FAILED
            total_pages INTEGER DEFAULT 0,
            total_chunks INTEGER DEFAULT 0,
            metadata JSONB, -- Additional metadata (page_count, sheet_names, etc.)
            uploaded_by VARCHAR(255),
            created_at TIMESTAMP DEFAULT NOW(),
            updated_at TIMESTAMP DEFAULT NOW(),
            processed_at TIMESTAMP,

            CONSTRAINT fk_document_bot FOREIGN KEY (bot_id) REFERENCES penny_bots(id) ON DELETE CASCADE
        );

        CREATE INDEX IF NOT EXISTS idx_document_bot_tenant
            ON penny_knowledge_documents(bot_id, tenant_id);
        CREATE INDEX IF NOT EXISTS idx_document_status
            ON penny_knowledge_documents(status);
        CREATE INDEX IF NOT EXISTS idx_document_file_type
            ON penny_knowledge_documents(file_type);
        CREATE INDEX IF NOT EXISTS idx_document_tenant
            ON penny_knowledge_documents(tenant_id);

        RAISE NOTICE 'Successfully created penny_knowledge_documents table';
    ELSE
        RAISE NOTICE 'penny_bots table does not exist, skipping penny_knowledge_documents creation';
    END IF;
END $$;
