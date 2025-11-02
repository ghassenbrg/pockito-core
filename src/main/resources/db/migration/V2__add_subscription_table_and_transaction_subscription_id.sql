-- ============================================================================
-- Add Subscription Table and Transaction Subscription ID Column
-- Version: V2
-- Description: Creates the t_subscription table and adds subscription_id column to t_transaction
-- ============================================================================

-- ============================================================================
-- Create Subscription table
-- ============================================================================
-- Migration: Create Subscription table
-- Version: V2
-- Description: Creates the t_subscription table for storing recurring subscription expenses

CREATE TABLE IF NOT EXISTS t_subscription (
    id VARCHAR(30) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(200) NOT NULL,
    icon_url VARCHAR(500),
    frequency VARCHAR(10) NOT NULL,
    interval INTEGER NOT NULL,
    amount DECIMAL(17,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    start_date DATE NOT NULL,
    next_due_date DATE NOT NULL,
    end_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    category_id VARCHAR(30) NOT NULL,
    day_of_month INTEGER,
    day_of_week INTEGER,
    month_of_year INTEGER,
    default_wallet_id VARCHAR(30) NOT NULL,
    note VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    archived_at TIMESTAMP,
    archived_by VARCHAR(255),
    version BIGINT,
    
    -- Primary key constraint
    CONSTRAINT pk_subscription PRIMARY KEY (id),
    
    -- Foreign key constraint to user table
    CONSTRAINT fk_subscription_user FOREIGN KEY (user_id) REFERENCES app_user(username),
    
    -- Foreign key constraint to category
    CONSTRAINT fk_subscription_category FOREIGN KEY (category_id) REFERENCES t_category(id),
    
    -- Foreign key constraint to default wallet
    CONSTRAINT fk_subscription_default_wallet FOREIGN KEY (default_wallet_id) REFERENCES t_wallet(id),
    
    -- Check constraints for data validation
    CONSTRAINT chk_subscription_name_length CHECK (LENGTH(name) >= 1 AND LENGTH(name) <= 200),
    CONSTRAINT chk_subscription_frequency_valid CHECK (frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')),
    CONSTRAINT chk_subscription_interval_positive CHECK (interval >= 1),
    CONSTRAINT chk_subscription_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_subscription_icon_url_length CHECK (icon_url IS NULL OR LENGTH(icon_url) <= 500),
    CONSTRAINT chk_subscription_day_of_month_range CHECK (day_of_month IS NULL OR (day_of_month >= 1 AND day_of_month <= 31)),
    CONSTRAINT chk_subscription_day_of_week_range CHECK (day_of_week IS NULL OR (day_of_week >= 1 AND day_of_week <= 7)),
    CONSTRAINT chk_subscription_month_of_year_range CHECK (month_of_year IS NULL OR (month_of_year >= 1 AND month_of_year <= 12)),
    CONSTRAINT chk_subscription_note_length CHECK (note IS NULL OR LENGTH(note) <= 1000),
    CONSTRAINT chk_subscription_end_date_after_start CHECK (end_date IS NULL OR end_date >= start_date),
    CONSTRAINT chk_subscription_next_due_date_after_start CHECK (next_due_date >= start_date)
);

-- Create indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_subscription_user_id ON t_subscription(user_id);
CREATE INDEX IF NOT EXISTS idx_subscription_category ON t_subscription(category_id);
CREATE INDEX IF NOT EXISTS idx_subscription_default_wallet ON t_subscription(default_wallet_id);
CREATE INDEX IF NOT EXISTS idx_subscription_is_active ON t_subscription(is_active);
CREATE INDEX IF NOT EXISTS idx_subscription_next_due_date ON t_subscription(next_due_date);

-- Create composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_subscription_user_active ON t_subscription(user_id, is_active);
CREATE INDEX IF NOT EXISTS idx_subscription_user_due_date ON t_subscription(user_id, next_due_date);
CREATE INDEX IF NOT EXISTS idx_subscription_active_due_date ON t_subscription(is_active, next_due_date);

-- Add comments for documentation
COMMENT ON TABLE t_subscription IS 'Stores recurring subscription expenses with flexible frequency and scheduling options';
COMMENT ON COLUMN t_subscription.id IS 'Primary key - Pockito ID format: SUB-{10-20 digits}';
COMMENT ON COLUMN t_subscription.user_id IS 'Foreign key reference to the user who owns this subscription';
COMMENT ON COLUMN t_subscription.name IS 'Subscription name (1-200 characters)';
COMMENT ON COLUMN t_subscription.icon_url IS 'URL to the subscription icon/image (optional, max 500 characters)';
COMMENT ON COLUMN t_subscription.frequency IS 'Frequency unit of recurrence - DAILY, WEEKLY, MONTHLY, or YEARLY';
COMMENT ON COLUMN t_subscription.interval IS 'Repeat every interval units of frequency (must be >= 1)';
COMMENT ON COLUMN t_subscription.amount IS 'Subscription amount (precision: 17,2, must be > 0)';
COMMENT ON COLUMN t_subscription.currency IS 'Currency code following ISO 4217 standard (3-letter code)';
COMMENT ON COLUMN t_subscription.start_date IS 'Start date when the subscription began';
COMMENT ON COLUMN t_subscription.next_due_date IS 'Next due date for the subscription charge (calculated based on frequency and interval)';
COMMENT ON COLUMN t_subscription.end_date IS 'End date of the subscription (optional, null means continues indefinitely)';
COMMENT ON COLUMN t_subscription.is_active IS 'Whether the subscription is currently active (default: TRUE)';
COMMENT ON COLUMN t_subscription.category_id IS 'Foreign key reference to expense category using Pockito ID format';
COMMENT ON COLUMN t_subscription.day_of_month IS 'Day of month (1-31) for precise scheduling with MONTHLY frequency (optional)';
COMMENT ON COLUMN t_subscription.day_of_week IS 'Day of week (1-7, 1=Monday, 7=Sunday) for precise scheduling with WEEKLY frequency (optional)';
COMMENT ON COLUMN t_subscription.month_of_year IS 'Month of year (1-12) for precise scheduling with YEARLY frequency (optional)';
COMMENT ON COLUMN t_subscription.default_wallet_id IS 'Foreign key reference to default wallet to charge subscription from';
COMMENT ON COLUMN t_subscription.note IS 'Optional note/description for the subscription (max 1000 characters)';
COMMENT ON COLUMN t_subscription.created_at IS 'Timestamp when the subscription was first created';
COMMENT ON COLUMN t_subscription.updated_at IS 'Timestamp when the subscription was last updated';
COMMENT ON COLUMN t_subscription.created_by IS 'Username of who created the subscription record';
COMMENT ON COLUMN t_subscription.updated_by IS 'Username of who last updated the subscription record';
COMMENT ON COLUMN t_subscription.archived_at IS 'Timestamp when the subscription was archived (soft delete)';
COMMENT ON COLUMN t_subscription.archived_by IS 'Username of who archived the subscription record';
COMMENT ON COLUMN t_subscription.version IS 'Hibernate optimistic locking version';

-- ============================================================================
-- Add subscription_id column to Transaction table
-- ============================================================================
-- Migration: Add subscription_id to Transaction table
-- Version: V2
-- Description: Adds subscription_id foreign key column to t_transaction for subscription payments

-- Add subscription_id column to t_transaction
ALTER TABLE t_transaction 
ADD COLUMN IF NOT EXISTS subscription_id VARCHAR(30);

-- Add foreign key constraint
ALTER TABLE t_transaction
ADD CONSTRAINT fk_transaction_subscription 
FOREIGN KEY (subscription_id) REFERENCES t_subscription(id);

-- Create index for subscription_id
CREATE INDEX IF NOT EXISTS idx_transaction_subscription ON t_transaction(subscription_id);

-- Create composite index for user + subscription queries
CREATE INDEX IF NOT EXISTS idx_transaction_user_subscription ON t_transaction(user_id, subscription_id);

-- Add comment for documentation
COMMENT ON COLUMN t_transaction.subscription_id IS 'Foreign key reference to subscription that generated this transaction (null for non-subscription transactions)';

