-- Add display_order column to wallet table
ALTER TABLE wallet ADD COLUMN display_order INTEGER;

-- Update existing wallets to have order based on creation date (newest first)
WITH wallet_ordering AS (
  SELECT 
    id,
    user_id,
    ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY created_at DESC) as rn
  FROM wallet 
  WHERE archived_at IS NULL
)
UPDATE wallet 
SET display_order = wo.rn
FROM wallet_ordering wo
WHERE wallet.id = wo.id;

-- Make the column NOT NULL after populating data
ALTER TABLE wallet ALTER COLUMN display_order SET NOT NULL;

-- Add index for efficient ordering
CREATE INDEX ix_wallet_user_order ON wallet(user_id, display_order) WHERE archived_at IS NULL;

-- Add constraint to ensure order is positive
ALTER TABLE wallet ADD CONSTRAINT chk_wallet_display_order_positive CHECK (display_order > 0);
