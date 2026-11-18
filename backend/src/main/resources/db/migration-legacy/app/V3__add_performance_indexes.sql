DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = current_schema() AND table_name = 'webhooks') THEN
        CREATE INDEX IF NOT EXISTS idx_webhooks_url ON webhooks(url);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = current_schema() AND table_name = 'webhook_events') THEN
        CREATE INDEX IF NOT EXISTS idx_webhook_events_webhook_id ON webhook_events(webhook_id);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = current_schema() AND table_name = 'webhook_dead_letter') THEN
        CREATE INDEX IF NOT EXISTS idx_webhook_dead_letter_status ON webhook_dead_letter(status);
        CREATE INDEX IF NOT EXISTS idx_webhook_dead_letter_next_retry ON webhook_dead_letter(next_retry_at);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = current_schema() AND table_name = 'users') THEN
        CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = current_schema() AND table_name = 'conversations') THEN
        CREATE INDEX IF NOT EXISTS idx_conversations_user_id ON conversations(user_id);
    END IF;
END $$;
