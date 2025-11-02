-- ============================================================================
-- Rename is_active column to enabled in Subscription table
-- Version: V6
-- Description: Renames the is_active column to enabled in t_subscription table
--              and updates all related indexes
-- ============================================================================

-- Rename the column from is_active to enabled
ALTER TABLE t_subscription
RENAME COLUMN is_active TO enabled;

-- Drop old indexes that reference is_active
DROP INDEX IF EXISTS idx_subscription_is_active;
DROP INDEX IF EXISTS idx_subscription_user_active;
DROP INDEX IF EXISTS idx_subscription_active_due_date;

-- Recreate indexes with the new column name
CREATE INDEX IF NOT EXISTS idx_subscription_enabled ON t_subscription(enabled);
CREATE INDEX IF NOT EXISTS idx_subscription_user_enabled ON t_subscription(user_id, enabled);
CREATE INDEX IF NOT EXISTS idx_subscription_enabled_due_date ON t_subscription(enabled, next_due_date);

-- Update column comment
COMMENT ON COLUMN t_subscription.enabled IS 'Whether the subscription is currently enabled (default: TRUE)';

