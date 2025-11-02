-- ============================================================================
-- Allow NULL for next_due_date Column
-- Version: V4
-- Description: Modifies the next_due_date column to allow NULL values and
--              updates the CHECK constraint to handle NULL values
-- ============================================================================

-- Drop the existing constraint that requires next_due_date >= start_date
ALTER TABLE t_subscription 
DROP CONSTRAINT IF EXISTS chk_subscription_next_due_date_after_start;

-- Alter the column to allow NULL
ALTER TABLE t_subscription 
ALTER COLUMN next_due_date DROP NOT NULL;

-- Recreate the constraint with logic that allows NULL or requires next_due_date >= start_date
ALTER TABLE t_subscription
ADD CONSTRAINT chk_subscription_next_due_date_after_start CHECK (
    next_due_date IS NULL OR next_due_date >= start_date
);

-- Add comment to document the constraint behavior
COMMENT ON CONSTRAINT chk_subscription_next_due_date_after_start ON t_subscription IS 
    'next_due_date can be NULL (when subscription has ended) or must be >= start_date';

