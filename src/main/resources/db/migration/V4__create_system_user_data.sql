-- Migration: Create system user data
-- Version: V4
-- Description: Creates the system user required for system operations and default categories

-- Ensure pgcrypto for gen_random_uuid() if not already available
CREATE EXTENSION IF NOT EXISTS pgcrypto;

BEGIN;

-----------------------------------------------------------------------
-- Create system user if it doesn't exist
-----------------------------------------------------------------------
INSERT INTO app_user (
    username,
    created_at,
    updated_at,
    created_by,
    updated_by,
    version
)
SELECT 
    'system',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system',
    0
WHERE NOT EXISTS (
    SELECT 1 FROM app_user WHERE username = 'system'
);

-- Add comment for documentation
COMMENT ON TABLE app_user IS 'Stores application users including the system user for system operations';

COMMIT;
