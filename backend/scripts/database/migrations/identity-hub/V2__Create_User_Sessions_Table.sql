-- Create User Sessions Table
CREATE TABLE IF NOT EXISTS user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_token_hash ON user_sessions(token_hash);
CREATE INDEX IF NOT EXISTS idx_sessions_expires_at ON user_sessions(expires_at);
CREATE INDEX IF NOT EXISTS idx_sessions_created_at ON user_sessions(created_at);

-- Create composite index for active sessions
CREATE INDEX IF NOT EXISTS idx_sessions_active ON user_sessions(user_id, expires_at) 
WHERE expires_at > CURRENT_TIMESTAMP;

-- Function to clean up expired sessions
CREATE OR REPLACE FUNCTION cleanup_expired_sessions()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM user_sessions 
    WHERE expires_at < CURRENT_TIMESTAMP;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Create a trigger to automatically clean up expired sessions (optional)
-- This would be called by a scheduled job instead
