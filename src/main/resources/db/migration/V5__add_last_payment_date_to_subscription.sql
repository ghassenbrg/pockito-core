-- ============================================================================
-- Add last_payment_date Column to Subscription Table
-- Version: V5
-- Description: Adds last_payment_date column to track when the last payment
--              was successfully processed for a subscription. This field is
--              only set when the pay service API is successful and a transaction
--              is created.
-- ============================================================================

-- Add the last_payment_date column to t_subscription table
ALTER TABLE t_subscription 
ADD COLUMN last_payment_date DATE NULL;

-- Add comment to document the column purpose
COMMENT ON COLUMN t_subscription.last_payment_date IS 
    'Date when the last payment was successfully processed. Only updated when pay service API is successful and transaction is created.';

