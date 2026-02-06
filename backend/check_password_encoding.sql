-- CHECK PASSWORD ENCODING CONSISTENCY
-- This script checks if passwords in auth_users and user_credentials use same encoding

-- Step 1: Check password format in auth_users (legacy)
SELECT 'AUTH_USERS_PASSWORD_SAMPLE' as audit_type,
       id,
       email,
       LEFT(password, 20) as password_prefix,
       LENGTH(password) as password_length
FROM auth_users
LIMIT 3;

-- Step 2: Check password format in user_credentials (identity)
SELECT 'USER_CREDENTIALS_PASSWORD_SAMPLE' as audit_type,
       user_id,
       LEFT(password, 20) as password_prefix,
       LENGTH(password) as password_length,
       source,
       provider
FROM user_credentials
LIMIT 3;

-- Step 3: Check for same email in both tables
SELECT 'DUPLICATE_EMAIL_CHECK' as audit_type,
       a.email as auth_email,
       a.id as auth_id,
       u.id as identity_id,
       u.email as identity_email,
       LEFT(a.password, 20) as auth_password_prefix,
       LEFT(c.password, 20) as credential_password_prefix,
       c.source as credential_source
FROM auth_users a
LEFT JOIN identity_users u ON a.email = u.email
LEFT JOIN user_credentials c ON u.id = c.user_id
WHERE a.email = u.email
LIMIT 5;

-- Step 4: Count records by source
SELECT 'CREDENTIALS_BY_SOURCE' as audit_type,
       source,
       COUNT(*) as count
FROM user_credentials
GROUP BY source;
