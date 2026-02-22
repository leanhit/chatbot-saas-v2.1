-- Create User Profiles Table
CREATE TABLE IF NOT EXISTS user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    avatar_url TEXT,
    timezone VARCHAR(50) DEFAULT 'UTC',
    language VARCHAR(10) DEFAULT 'en',
    bio TEXT,
    date_of_birth DATE,
    gender VARCHAR(20) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_TO_SAY')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_profiles_user_id ON user_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_profiles_timezone ON user_profiles(timezone);
CREATE INDEX IF NOT EXISTS idx_profiles_language ON user_profiles(language);
CREATE INDEX IF NOT EXISTS idx_profiles_created_at ON user_profiles(created_at);

-- Create trigger for updated_at
CREATE OR REPLACE FUNCTION update_profiles_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_user_profiles_updated_at 
    BEFORE UPDATE ON user_profiles 
    FOR EACH ROW 
    EXECUTE FUNCTION update_profiles_updated_at_column();
