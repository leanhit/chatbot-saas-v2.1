-- Migration Script: Create UserProfile for existing users
-- This script creates UserProfile records for users who don't have one yet

INSERT INTO user_profiles (id, created_at, updated_at)
SELECT u.id, NOW(), NOW()
FROM users u
LEFT JOIN user_profiles up ON u.id = up.id
WHERE up.id IS NULL;

-- Log the migration
SELECT COUNT(*) as migrated_users
FROM users u
LEFT JOIN user_profiles up ON u.id = up.id
WHERE up.id IS NULL;
