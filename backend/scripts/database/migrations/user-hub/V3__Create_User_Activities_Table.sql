-- Create User Activities Table
CREATE TABLE IF NOT EXISTS user_activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    description TEXT,
    ip_address INET,
    user_agent TEXT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_activities_user_id ON user_activities(user_id);
CREATE INDEX IF NOT EXISTS idx_activities_type ON user_activities(activity_type);
CREATE INDEX IF NOT EXISTS idx_activities_created_at ON user_activities(created_at);
CREATE INDEX IF NOT EXISTS idx_activities_ip_address ON user_activities(ip_address);

-- Create composite index for recent activities
CREATE INDEX IF NOT EXISTS idx_activities_recent ON user_activities(user_id, created_at DESC);

-- Create GIN index for metadata JSONB
CREATE INDEX IF NOT EXISTS idx_activities_metadata ON user_activities USING GIN(metadata);

-- Partition table by month for better performance (optional for large datasets)
-- This would be implemented if the table grows very large
-- CREATE TABLE user_activities_y2024m01 PARTITION OF user_activities
-- FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');
