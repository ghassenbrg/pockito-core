-- ============================================================================
-- Allow EXPENSE Transactions Without Wallet When Subscription Is Set
-- Version: V3
-- Description: Modifies the check constraint to allow wallet_from_id to be NULL 
--              for EXPENSE transactions when subscription_id is set
-- ============================================================================

-- Drop the existing constraint
ALTER TABLE t_transaction 
DROP CONSTRAINT IF EXISTS chk_transaction_expense_wallet_from;

-- Recreate the constraint with new logic:
-- For EXPENSE transactions: wallet_from_id must be NOT NULL OR subscription_id must be NOT NULL
-- This allows subscription-based expense transactions to have null wallet_from_id
ALTER TABLE t_transaction
ADD CONSTRAINT chk_transaction_expense_wallet_from CHECK (
    transaction_type != 'EXPENSE' OR wallet_from_id IS NOT NULL OR subscription_id IS NOT NULL
);

-- Add comment to document the constraint behavior
COMMENT ON CONSTRAINT chk_transaction_expense_wallet_from ON t_transaction IS 
    'EXPENSE transactions require either wallet_from_id or subscription_id to be set';

