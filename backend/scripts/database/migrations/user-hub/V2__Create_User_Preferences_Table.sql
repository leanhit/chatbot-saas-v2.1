-- Create User Preferences Table
CREATE TABLE IF NOT EXISTS user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL,
    email_notifications BOOLEAN DEFAULT TRUE,
    sms_notifications BOOLEAN DEFAULT FALSE,
    push_notifications BOOLEAN DEFAULT TRUE,
    theme VARCHAR(20) DEFAULT 'light' CHECK (theme IN ('light', 'dark', 'auto')),
    currency VARCHAR(10) DEFAULT 'USD',
    privacy_profile_visibility VARCHAR(20) DEFAULT 'PUBLIC' CHECK (privacy_profile_visibility IN ('PUBLIC', 'PRIVATE', 'FRIENDS')),
    privacy_activity_tracking BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_preferences_user_id ON user_preferences(user_id);
CREATE INDEX IF NOT EXISTS idx_preferences_theme ON user_preferences(theme);
CREATE INDEX IF NOT EXISTS idx_preferences_currency ON user_preferences(currency);

-- Create trigger for updated_at
CREATE OR REPLACE FUNCTION update_preferences_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_user_preferences_updated_at 
    BEFORE UPDATE ON user_preferences 
    FOR EACH ROW 
    EXECUTE FUNCTION update_preferences_updated_at_column();
