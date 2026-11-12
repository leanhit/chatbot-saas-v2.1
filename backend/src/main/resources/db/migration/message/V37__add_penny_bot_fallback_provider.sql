-- Add per-bot fallback LLM provider configuration
ALTER TABLE penny_bots
    ADD COLUMN IF NOT EXISTS fallback_provider_type VARCHAR(20);

COMMENT ON COLUMN penny_bots.fallback_provider_type IS
    'Optional fallback LLM provider (OPENAI, CLAUDE, GEMINI, OLLAMA). NULL = use default fallback order.';
