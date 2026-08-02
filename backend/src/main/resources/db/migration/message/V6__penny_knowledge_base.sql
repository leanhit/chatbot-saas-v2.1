-- ============================================================
-- V11: Create Knowledge Base tables for Penny RAG
-- ============================================================
-- Requires: CREATE EXTENSION IF NOT EXISTS vector;
-- (Run manually on PostgreSQL if pgvector is not yet enabled)

DO $$
BEGIN
    -- Only create table if penny_bots exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'penny_bots') THEN
        CREATE TABLE IF NOT EXISTS penny_knowledge_articles (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            bot_id UUID NOT NULL,
            tenant_id BIGINT NOT NULL,
            title VARCHAR(500) NOT NULL,
            content TEXT NOT NULL,
            category VARCHAR(100),
            tags VARCHAR(500),
            embedding VECTOR(1536),
            embedding_model VARCHAR(100),
            source_url VARCHAR(1000),
            is_active BOOLEAN DEFAULT true,
            priority INTEGER DEFAULT 0,
            created_at TIMESTAMP DEFAULT NOW(),
            updated_at TIMESTAMP DEFAULT NOW(),
            created_by VARCHAR(255),

            CONSTRAINT fk_knowledge_bot FOREIGN KEY (bot_id) REFERENCES penny_bots(id) ON DELETE CASCADE
        );

        CREATE INDEX IF NOT EXISTS idx_knowledge_bot_tenant
            ON penny_knowledge_articles(bot_id, tenant_id);
        CREATE INDEX IF NOT EXISTS idx_knowledge_category
            ON penny_knowledge_articles(bot_id, category);
        CREATE INDEX IF NOT EXISTS idx_knowledge_active
            ON penny_knowledge_articles(bot_id, tenant_id, is_active);

        -- Vector similarity index (IVFFlat for approximate nearest neighbor search)
        -- Only create if pgvector extension is available
        IF EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'vector') THEN
            CREATE INDEX IF NOT EXISTS idx_knowledge_embedding
                ON penny_knowledge_articles
                USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
        ELSE
            RAISE NOTICE 'pgvector extension not found, skipping vector index creation';
        END IF;
    ELSE
        RAISE NOTICE 'penny_bots table does not exist, skipping penny_knowledge_articles creation';
    END IF;
END $$;
