-- Create proper Identity Hub schema
-- This creates clean, isolated tables for Identity Hub

-- Drop existing tables if they exist (for clean recreation)
DROP TABLE IF EXISTS user_credentials CASCADE;
DROP TABLE IF EXISTS identity_users CASCADE;

-- Create identity_users table (clean, no legacy fields)
CREATE TABLE identity_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create user_credentials table (password and auth provider data)
CREATE TABLE user_credentials (
    user_id BIGINT PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    provider VARCHAR(50) NOT NULL DEFAULT 'LOCAL',
    source VARCHAR(50) NOT NULL DEFAULT 'IDENTITY',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    failed_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    password_changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key to identity_users
    CONSTRAINT fk_user_credentials_user 
        FOREIGN KEY (user_id) REFERENCES identity_users(id) 
        ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_identity_users_email ON identity_users(email);
CREATE INDEX idx_identity_users_status ON identity_users(status);
CREATE INDEX idx_user_credentials_provider ON user_credentials(provider);
CREATE INDEX idx_user_credentials_source ON user_credentials(source);
CREATE INDEX idx_user_credentials_is_active ON user_credentials(is_active);

-- Create trigger for updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_identity_users_updated_at 
    BEFORE UPDATE ON identity_users 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_credentials_updated_at 
    BEFORE UPDATE ON user_credentials 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data for testing
INSERT INTO identity_users (email, status) VALUES 
('test@identityhub.com', 'ACTIVE'),
('admin@identityhub.com', 'ACTIVE');

INSERT INTO user_credentials (user_id, password, provider, source) VALUES 
(1, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'LOCAL', 'IDENTITY'),
(2, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'LOCAL', 'IDENTITY');

COMMIT;
