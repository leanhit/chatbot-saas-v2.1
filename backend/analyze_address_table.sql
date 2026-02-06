-- COMPLETE ANALYSIS OF ADDRESS TABLE
-- Check owner_type values and relationships

-- Step 1: Check distinct owner_type values
SELECT 'DISTINCT_OWNER_TYPES' as analysis_type,
       owner_type,
       COUNT(*) as record_count
FROM address
GROUP BY owner_type
ORDER BY owner_type;

-- Step 2: Check address table structure
SELECT 'ADDRESS_TABLE_STRUCTURE' as analysis_type,
       column_name,
       data_type,
       is_nullable,
       character_maximum_length
FROM information_schema.columns 
WHERE table_name = 'address' 
  AND table_schema = current_schema()
ORDER BY ordinal_position;

-- Step 3: Check owner_id values for each owner_type
SELECT 'OWNER_ID_ANALYSIS' as analysis_type,
       owner_type,
       COUNT(DISTINCT owner_id) as unique_owner_ids,
       MIN(owner_id) as min_owner_id,
       MAX(owner_id) as max_owner_id
FROM address
GROUP BY owner_type
ORDER BY owner_type;

-- Step 4: Check which tables the owner_ids reference
-- Check if owner_ids reference auth_users table
SELECT 'OWNER_ID_IN_AUTH_USERS' as analysis_type,
       a.owner_type,
       COUNT(*) as matching_records
FROM address a
JOIN auth_users au ON a.owner_id = au.id
WHERE a.owner_type IN ('USER', 'AUTH_USER', 'LEGACY_USER')
GROUP BY a.owner_type

UNION ALL

-- Check if owner_ids reference users table (Identity Hub)
SELECT 'OWNER_ID_IN_USERS' as analysis_type,
       a.owner_type,
       COUNT(*) as matching_records
FROM address a
JOIN users u ON a.owner_id = u.id
WHERE a.owner_type IN ('USER', 'IDENTITY_USER', 'USER_IDENTITY')
GROUP BY a.owner_type;

-- Step 5: Check for orphaned address records
SELECT 'ORPHANED_ADDRESSES' as analysis_type,
       owner_type,
       COUNT(*) as orphaned_count
FROM address a
LEFT JOIN auth_users au ON a.owner_id = au.id AND a.owner_type IN ('USER', 'AUTH_USER', 'LEGACY_USER')
LEFT JOIN users u ON a.owner_id = u.id AND a.owner_type IN ('USER', 'IDENTITY_USER', 'USER_IDENTITY')
WHERE (au.id IS NULL AND u.id IS NULL)
  AND a.owner_type IN ('USER', 'AUTH_USER', 'LEGACY_USER', 'IDENTITY_USER', 'USER_IDENTITY')
GROUP BY a.owner_type
ORDER BY owner_type;

-- Step 6: Sample records for each owner_type
SELECT 'SAMPLE_RECORDS' as analysis_type,
       owner_type,
       owner_id,
       is_default,
       street,
       ward,
       district,
       city,
       country
FROM (
    SELECT *,
           ROW_NUMBER() OVER (PARTITION BY owner_type ORDER BY id) as rn
    FROM address
) ranked
WHERE rn <= 3  -- Show up to 3 sample records per owner_type
ORDER BY owner_type, rn;
