-- ============================================================
-- V16: Create penny_knowledge_chunks table for storing document chunks with embeddings
-- ============================================================

DO $$
BEGIN
    -- Only create table if penny_knowledge_documents exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'penny_knowledge_documents') THEN
        CREATE TABLE IF NOT EXISTS penny_knowledge_chunks (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            document_id UUID NOT NULL,
            bot_id UUID NOT NULL,
            tenant_id BIGINT NOT NULL,
            chunk_index INTEGER NOT NULL,
            chunk_text TEXT NOT NULL,
            chunk_tokens INTEGER DEFAULT 0,
            embedding VECTOR(1536),
            embedding_model VARCHAR(100),
            page_number INTEGER, -- For PDF
            sheet_name VARCHAR(255), -- For XLSX
            row_index INTEGER, -- For XLSX
            metadata JSONB, -- Additional metadata
            created_at TIMESTAMP DEFAULT NOW(),

            CONSTRAINT fk_chunk_document FOREIGN KEY (document_id) REFERENCES penny_knowledge_documents(id) ON DELETE CASCADE,
            CONSTRAINT fk_chunk_bot FOREIGN KEY (bot_id) REFERENCES penny_bots(id) ON DELETE CASCADE
        );

        CREATE INDEX IF NOT EXISTS idx_chunk_document
            ON penny_knowledge_chunks(document_id);
        CREATE INDEX IF NOT EXISTS idx_chunk_bot_tenant
            ON penny_knowledge_chunks(bot_id, tenant_id);
        CREATE INDEX IF NOT EXISTS idx_chunk_index
            ON penny_knowledge_chunks(document_id, chunk_index);

        -- Vector similarity index (HNSW for approximate nearest neighbor search)
        -- Only create if pgvector extension is available
        IF EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'vector') THEN
            CREATE INDEX IF NOT EXISTS idx_chunk_embedding_hnsw
                ON penny_knowledge_chunks
                USING hnsw (embedding vector_cosine_ops) WITH (m = 16, ef_construction = 64);
        ELSE
            RAISE NOTICE 'pgvector extension not found, skipping HNSW vector index creation';
        END IF;

        RAISE NOTICE 'Successfully created penny_knowledge_chunks table with HNSW index';
    ELSE
        RAISE NOTICE 'penny_knowledge_documents table does not exist, skipping penny_knowledge_chunks creation';
    END IF;
END $$;
