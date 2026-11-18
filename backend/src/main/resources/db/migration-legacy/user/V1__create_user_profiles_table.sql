-- ============================================================
-- V1: Create user_profiles table for user profile information
-- ============================================================

CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    fullname VARCHAR(100),
    phone_number VARCHAR(50),
    avatar VARCHAR(500),
    gender VARCHAR(10),
    bio VARCHAR(500),
    email VARCHAR(255),
    job_title VARCHAR(100),
    department VARCHAR(100),
    company VARCHAR(100),
    linkedin_url VARCHAR(500),
    website VARCHAR(500),
    location VARCHAR(200),
    skills VARCHAR(1000),
    experience VARCHAR(1000),
    education VARCHAR(500),
    certifications VARCHAR(500),
    languages VARCHAR(255),
    availability VARCHAR(100),
    hourly_rate VARCHAR(50),
    portfolio_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_user_profile_user ON user_profiles(user_id);

-- Add comments
COMMENT ON TABLE user_profiles IS 'User profile information';
COMMENT ON COLUMN user_profiles.user_id IS 'Reference to users table';
COMMENT ON COLUMN user_profiles.fullname IS 'User full name';
COMMENT ON COLUMN user_profiles.phone_number IS 'User phone number';
COMMENT ON COLUMN user_profiles.avatar IS 'User avatar URL';
