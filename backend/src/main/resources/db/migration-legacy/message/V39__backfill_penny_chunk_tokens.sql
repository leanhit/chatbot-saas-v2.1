-- ============================================================
-- V39: Backfill chunk_tokens for existing rows where the value is 0 or NULL.
--
-- Background: chunk_tokens was added in V16 but older rows were saved before
-- the TextChunkerService populated it, leaving them at the default 0.
-- formatContextForPrompt() falls back to a 3.5 chars/token estimate for such
-- rows, which is inaccurate for Vietnamese text.
--
-- This migration applies a language-aware backfill:
--   - If > 40% of characters are non-ASCII (Vietnamese): ~2.5 chars/token
--   - Otherwise (English / mixed):                       ~3.5 chars/token
--
-- The resulting values are intentionally slightly conservative (round up) so
-- the RAG context window stays within maxContextTokens rather than exceeding it.
-- ============================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_name = 'penny_knowledge_chunks'
    ) THEN

        -- Vietnamese-heavy chunks  (>40% non-ASCII bytes → ~2.5 chars/token)
        UPDATE penny_knowledge_chunks
        SET chunk_tokens = CEIL(CHAR_LENGTH(chunk_text) / 2.5)
        WHERE (chunk_tokens IS NULL OR chunk_tokens = 0)
          AND is_deleted = FALSE
          AND (
              -- Heuristic: octet_length >> char_length means many multi-byte chars
              OCTET_LENGTH(chunk_text)::float / NULLIF(CHAR_LENGTH(chunk_text), 0) > 1.8
          );

        -- English / ASCII-dominant chunks  (~3.5 chars/token)
        UPDATE penny_knowledge_chunks
        SET chunk_tokens = CEIL(CHAR_LENGTH(chunk_text) / 3.5)
        WHERE (chunk_tokens IS NULL OR chunk_tokens = 0)
          AND is_deleted = FALSE;

        RAISE NOTICE 'Backfilled chunk_tokens for all zero/null rows in penny_knowledge_chunks';
    ELSE
        RAISE NOTICE 'penny_knowledge_chunks table not found, skipping backfill';
    END IF;
END $$;
