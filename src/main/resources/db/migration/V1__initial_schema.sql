-- ============================================================================
-- Initial Database Schema
-- Version: V1
-- Description: Initial database schema migration containing all tables and data
-- This file merges all migrations (V1-V7) into a single initial schema setup
-- ============================================================================

-- ============================================================================
-- V1: Create User table
-- ============================================================================
-- Migration: Create User table
-- Version: V1
-- Description: Creates the app_user table for storing registered application users

CREATE TABLE IF NOT EXISTS app_user (
    username VARCHAR(255) NOT NULL,
    country VARCHAR(3),
    default_currency VARCHAR(3),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    archived_at TIMESTAMP,
    archived_by VARCHAR(255),
    version BIGINT,
    
    -- Primary key constraint
    CONSTRAINT pk_app_user PRIMARY KEY (username)
);

-- Create index on username for faster lookups
CREATE INDEX IF NOT EXISTS idx_app_user_username ON app_user(username);

-- Create index on country for potential filtering
CREATE INDEX IF NOT EXISTS idx_app_user_country ON app_user(country);

-- Create index on default_currency for potential filtering
CREATE INDEX IF NOT EXISTS idx_app_user_default_currency ON app_user(default_currency);

-- Create index on created_at for potential date-based queries
CREATE INDEX IF NOT EXISTS idx_app_user_created_at ON app_user(created_at);

-- Add comments for documentation
COMMENT ON TABLE app_user IS 'Stores registered application users with their preferences and audit information';
COMMENT ON COLUMN app_user.username IS 'Primary key - username extracted from Keycloak token (preferred_username claim)';
COMMENT ON COLUMN app_user.country IS 'User''s country of residence (ISO 3166-1 alpha-2 or alpha-3 code)';
COMMENT ON COLUMN app_user.default_currency IS 'User''s preferred default currency (ISO 4217 3-letter code)';
COMMENT ON COLUMN app_user.created_at IS 'Timestamp when the user was first created';
COMMENT ON COLUMN app_user.updated_at IS 'Timestamp when the user was last updated';
COMMENT ON COLUMN app_user.created_by IS 'Username of who created the user record';
COMMENT ON COLUMN app_user.updated_by IS 'Username of who last updated the user record';
COMMENT ON COLUMN app_user.archived_at IS 'Timestamp when the user was archived (soft delete)';
COMMENT ON COLUMN app_user.archived_by IS 'Username of who archived the user record';
COMMENT ON COLUMN app_user.version IS 'Hibernate optimistic locking version';

-- ============================================================================
-- V2: Create Wallet table
-- ============================================================================
-- Migration: Create Wallet table
-- Version: V2
-- Description: Creates the t_wallet table for storing user wallets with proper constraints and indexes

CREATE TABLE IF NOT EXISTS t_wallet (
    id VARCHAR(30) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    initial_balance DECIMAL(17,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    icon_url VARCHAR(500),
    goal_amount DECIMAL(17,2),
    type VARCHAR(20) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    order_position INTEGER NOT NULL,
    description VARCHAR(500),
    color VARCHAR(7),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    archived_at TIMESTAMP,
    archived_by VARCHAR(255),
    version BIGINT,
    
    -- Primary key constraint
    CONSTRAINT pk_wallet PRIMARY KEY (id),
    
    -- Foreign key constraint to user table
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES app_user(username),
    
    -- Unique constraint: no duplicate wallet names per user
    CONSTRAINT uk_wallet_user_name UNIQUE (user_id, name),
    
    -- Check constraints for data validation
    CONSTRAINT chk_wallet_name_length CHECK (LENGTH(name) >= 1 AND LENGTH(name) <= 100),
    CONSTRAINT chk_wallet_goal_amount_positive CHECK (goal_amount IS NULL OR goal_amount >= 0),
    CONSTRAINT chk_wallet_order_position_non_negative CHECK (order_position >= 0),
    CONSTRAINT chk_wallet_color_format CHECK (color IS NULL OR color ~ '^#[0-9A-Fa-f]{6}$'),
    CONSTRAINT chk_wallet_type_valid CHECK (type IN ('BANK_ACCOUNT', 'CASH', 'CREDIT_CARD', 'SAVINGS', 'CUSTOM'))
);

-- Create indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_wallet_user_id ON t_wallet(user_id);
CREATE INDEX IF NOT EXISTS idx_wallet_is_default ON t_wallet(is_default);
CREATE INDEX IF NOT EXISTS idx_wallet_type ON t_wallet(type);
CREATE INDEX IF NOT EXISTS idx_wallet_order_position ON t_wallet(order_position);

-- Create composite index for user_id + order_position for efficient ordering
CREATE INDEX IF NOT EXISTS idx_wallet_user_order ON t_wallet(user_id, order_position);

-- Create index for currency filtering
CREATE INDEX IF NOT EXISTS idx_wallet_currency ON t_wallet(currency);

-- Add comments for documentation
COMMENT ON TABLE t_wallet IS 'Stores user wallets with their financial information and customization options';
COMMENT ON COLUMN t_wallet.id IS 'Primary key - Pockito ID format: WAL-{10-20 digits}';
COMMENT ON COLUMN t_wallet.user_id IS 'Foreign key reference to the user who owns this wallet';
COMMENT ON COLUMN t_wallet.name IS 'Wallet name - must be unique per user (1-100 characters)';
COMMENT ON COLUMN t_wallet.initial_balance IS 'Initial balance of the wallet (precision: 17,2)';
COMMENT ON COLUMN t_wallet.currency IS 'Currency code following ISO 4217 standard (3-letter code)';
COMMENT ON COLUMN t_wallet.icon_url IS 'URL to the wallet icon/image (optional, max 500 characters)';
COMMENT ON COLUMN t_wallet.goal_amount IS 'Goal amount for the wallet, e.g., savings target (optional, precision: 17,2)';
COMMENT ON COLUMN t_wallet.type IS 'Wallet type/category (BANK_ACCOUNT, CASH, CREDIT_CARD, SAVINGS, CUSTOM)';
COMMENT ON COLUMN t_wallet.is_default IS 'Whether this wallet is the user default wallet';
COMMENT ON COLUMN t_wallet.order_position IS 'Display order position for the wallet (non-negative integer)';
COMMENT ON COLUMN t_wallet.description IS 'Description of the wallet (optional, max 500 characters)';
COMMENT ON COLUMN t_wallet.color IS 'Hex color code for the wallet (optional, format: #A1B2C3)';
COMMENT ON COLUMN t_wallet.created_at IS 'Timestamp when the wallet was first created';
COMMENT ON COLUMN t_wallet.updated_at IS 'Timestamp when the wallet was last updated';
COMMENT ON COLUMN t_wallet.created_by IS 'Username of who created the wallet record';
COMMENT ON COLUMN t_wallet.updated_by IS 'Username of who last updated the wallet record';
COMMENT ON COLUMN t_wallet.archived_at IS 'Timestamp when the wallet was archived (soft delete)';
COMMENT ON COLUMN t_wallet.archived_by IS 'Username of who archived the wallet record';
COMMENT ON COLUMN t_wallet.version IS 'Hibernate optimistic locking version';

-- ============================================================================
-- V3: Create Category table
-- ============================================================================
-- Migration: Create Category table
-- Version: V3
-- Description: Creates the t_category table for storing user-defined categories with hierarchical support

CREATE TABLE IF NOT EXISTS t_category (
    id VARCHAR(30) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7) NOT NULL,
    category_type VARCHAR(10) NOT NULL,
    icon_url VARCHAR(500),
    parent_category_id VARCHAR(30),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    archived_at TIMESTAMP,
    archived_by VARCHAR(255),
    version BIGINT,
    
    -- Primary key constraint
    CONSTRAINT pk_category PRIMARY KEY (id),
    
    -- Foreign key constraint to user table
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES app_user(username),
    
    -- Foreign key constraint to parent category (self-reference)
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_category_id) REFERENCES t_category(id),
    
    -- Unique constraint: no duplicate category names per user
    CONSTRAINT uk_category_user_name UNIQUE (user_id, name),
    
    -- Check constraints for data validation
    CONSTRAINT chk_category_name_length CHECK (LENGTH(name) >= 1 AND LENGTH(name) <= 100),
    CONSTRAINT chk_category_color_format CHECK (color ~ '^#[0-9A-Fa-f]{6}$'),
    CONSTRAINT chk_category_type_valid CHECK (category_type IN ('EXPENSE', 'INCOME')),
    CONSTRAINT chk_category_icon_url_length CHECK (icon_url IS NULL OR LENGTH(icon_url) <= 500),
    CONSTRAINT chk_category_no_self_parent CHECK (parent_category_id IS NULL OR parent_category_id != id)
);

-- Create indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_category_user_id ON t_category(user_id);
CREATE INDEX IF NOT EXISTS idx_category_parent_id ON t_category(parent_category_id);
CREATE INDEX IF NOT EXISTS idx_category_type ON t_category(category_type);

-- Create composite index for user_id + name for efficient unique constraint checking
CREATE INDEX IF NOT EXISTS idx_category_user_name ON t_category(user_id, name);

-- Create composite index for user_id + category_type for efficient filtering
CREATE INDEX IF NOT EXISTS idx_category_user_type ON t_category(user_id, category_type);

-- Create index for color filtering
CREATE INDEX IF NOT EXISTS idx_category_color ON t_category(color);

-- Create index for hierarchical queries (parent-child relationships)
CREATE INDEX IF NOT EXISTS idx_category_hierarchy ON t_category(parent_category_id, user_id);

-- Add comments for documentation
COMMENT ON TABLE t_category IS 'Stores user-defined categories for organizing transactions or other entities with hierarchical support';
COMMENT ON COLUMN t_category.id IS 'Primary key - Pockito ID format: CAT-{10-20 digits}';
COMMENT ON COLUMN t_category.user_id IS 'Foreign key reference to the user who owns this category';
COMMENT ON COLUMN t_category.name IS 'Category name - must be unique per user (1-100 characters)';
COMMENT ON COLUMN t_category.color IS 'Hex color code for the category (required, format: #A1B2C3)';
COMMENT ON COLUMN t_category.category_type IS 'Type of category - either EXPENSE or INCOME (required)';
COMMENT ON COLUMN t_category.icon_url IS 'URL to the category icon/image (optional, max 500 characters)';
COMMENT ON COLUMN t_category.parent_category_id IS 'Foreign key reference to parent category using Pockito ID format';
COMMENT ON COLUMN t_category.created_at IS 'Timestamp when the category was first created';
COMMENT ON COLUMN t_category.updated_at IS 'Timestamp when the category was last updated';
COMMENT ON COLUMN t_category.created_by IS 'Username of who created the category record';
COMMENT ON COLUMN t_category.updated_by IS 'Username of who last updated the category record';
COMMENT ON COLUMN t_category.archived_at IS 'Timestamp when the category was archived (soft delete)';
COMMENT ON COLUMN t_category.archived_by IS 'Username of who archived the category record';
COMMENT ON COLUMN t_category.version IS 'Hibernate optimistic locking version';

-- ============================================================================
-- V4: Create system user data
-- ============================================================================
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

-- ============================================================================
-- V5: Insert default categories data
-- ============================================================================
-- Migration: Insert default "system" categories
-- Version: V5
-- Description: Seeds hierarchical default categories owned by user_id = 'system'
--              Using Icons8 Color PNG icons (96px).
-- Attribution: Icons by Icons8 (https://icons8.com)

-- Ensure pgcrypto for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

BEGIN;

-----------------------------------------------------------------------
-- 0) Precondition: ensure the "system" user exists (FK to app_user)
-----------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app_user WHERE username = 'system') THEN
        RAISE EXCEPTION 'Precondition failed: app_user(username=system) not found. Create it before running V5.';
    END IF;
END$$;

-----------------------------------------------------------------------
-- 1) Staging: declare the default categories with Icons8 PNG icons
--    Format: https://img.icons8.com/color/96/<icon-name>.png
-----------------------------------------------------------------------
CREATE TEMP TABLE _default_categories (
    id                text        NOT NULL,
    name              text        NOT NULL,
    category_type     text        NOT NULL,
    color             text        NOT NULL,
    icon_url          text        NOT NULL,
    parent_name       text        NULL
) ON COMMIT DROP;

INSERT INTO _default_categories (id, name, category_type, color, icon_url, parent_name)
VALUES
    -- EXPENSE: Housing
    ('CAT-0000000001', 'Housing',       'EXPENSE', '#5965F2', 'https://img.icons8.com/color/96/home.png', NULL),
    ('CAT-0000000002', 'Rent',          'EXPENSE', '#7B88FF', 'https://img.icons8.com/color/96/key.png', 'Housing'),
    ('CAT-0000000003', 'Mortgage',      'EXPENSE', '#7B88FF', 'https://img.icons8.com/color/96/bank.png', 'Housing'),
    ('CAT-0000000004', 'Utilities',     'EXPENSE', '#7B88FF', 'https://img.icons8.com/color/96/electricity.png', 'Housing'),
    ('CAT-0000000005', 'Maintenance',   'EXPENSE', '#7B88FF', 'https://img.icons8.com/color/96/tools.png', 'Housing'),

    -- EXPENSE: Transportation
    ('CAT-0000000006', 'Transportation','EXPENSE', '#2CB1BC', 'https://img.icons8.com/color/96/car.png', NULL),
    ('CAT-0000000007', 'Public Transit','EXPENSE', '#55C2CB', 'https://img.icons8.com/color/96/bus.png', 'Transportation'),
    ('CAT-0000000008', 'Fuel',          'EXPENSE', '#55C2CB', 'https://img.icons8.com/color/96/gas-station.png', 'Transportation'),
    ('CAT-0000000009', 'Taxi & Ridehailing','EXPENSE','#55C2CB','https://img.icons8.com/color/96/taxi.png','Transportation'),
    ('CAT-0000000010', 'Parking',       'EXPENSE', '#55C2CB', 'https://img.icons8.com/color/96/parking.png', 'Transportation'),

    -- EXPENSE: Food & Dining
    ('CAT-0000000011', 'Food & Dining', 'EXPENSE', '#F26457', 'https://img.icons8.com/color/96/restaurant.png', NULL),
    ('CAT-0000000012', 'Groceries',     'EXPENSE', '#F58A80', 'https://img.icons8.com/color/96/shopping-cart.png', 'Food & Dining'),
    ('CAT-0000000013', 'Restaurants',   'EXPENSE', '#F58A80', 'https://img.icons8.com/color/96/cutlery.png', 'Food & Dining'),
    ('CAT-0000000014', 'Coffee & Snacks','EXPENSE','#F58A80', 'https://img.icons8.com/color/96/coffee.png', 'Food & Dining'),
    ('CAT-0000000015', 'Delivery',      'EXPENSE', '#F58A80', 'https://img.icons8.com/color/96/delivery.png', 'Food & Dining'),

    -- EXPENSE: Entertainment
    ('CAT-0000000016', 'Entertainment', 'EXPENSE', '#A46BF5', 'https://img.icons8.com/?size=100&id=Kh9KJFNHDnTA&format=png&color=000000', NULL),
    ('CAT-0000000017', 'Streaming',     'EXPENSE', '#BE90FF', 'https://img.icons8.com/color/96/tv.png', 'Entertainment'),
    ('CAT-0000000018', 'Movies & Events','EXPENSE','#BE90FF', 'https://img.icons8.com/color/96/ticket.png', 'Entertainment'),
    ('CAT-0000000019', 'Games',         'EXPENSE', '#BE90FF', 'https://img.icons8.com/color/96/controller.png', 'Entertainment'),

    -- EXPENSE: Health
    ('CAT-0000000020', 'Health',        'EXPENSE', '#2ECC71', 'https://img.icons8.com/?size=100&id=AjSTXy58YYuf&format=png&color=000000', NULL),
    ('CAT-0000000021', 'Pharmacy',      'EXPENSE', '#71D79A', 'https://img.icons8.com/color/96/pill.png', 'Health'),
    ('CAT-0000000022', 'Doctor',        'EXPENSE', '#71D79A', 'https://img.icons8.com/color/96/doctor-male.png', 'Health'),
    ('CAT-0000000023', 'Insurance',     'EXPENSE', '#71D79A', 'https://img.icons8.com/color/96/insurance-agent.png', 'Health'),

    -- EXPENSE: Shopping
    ('CAT-0000000024', 'Shopping',      'EXPENSE', '#F2B84B', 'https://img.icons8.com/color/96/shopping-bag.png', NULL),
    ('CAT-0000000025', 'Clothing',      'EXPENSE', '#F7CD7F', 'https://img.icons8.com/color/96/t-shirt.png', 'Shopping'),
    ('CAT-0000000026', 'Electronics',   'EXPENSE', '#F7CD7F', 'https://img.icons8.com/color/96/laptop.png', 'Shopping'),
    ('CAT-0000000027', 'Home Goods',    'EXPENSE', '#F7CD7F', 'https://img.icons8.com/color/96/sofa.png', 'Shopping'),

    -- EXPENSE: Travel
    ('CAT-0000000028', 'Travel',        'EXPENSE', '#FF8C42', 'https://img.icons8.com/color/96/airplane-take-off.png', NULL),
    ('CAT-0000000029', 'Flights',       'EXPENSE', '#FFB27E', 'https://img.icons8.com/color/96/airport.png', 'Travel'),
    ('CAT-0000000030', 'Hotels',        'EXPENSE', '#FFB27E', 'https://img.icons8.com/color/96/5-star-hotel.png', 'Travel'),
    ('CAT-0000000031', 'Activities',    'EXPENSE', '#FFB27E', 'https://img.icons8.com/color/96/beach.png', 'Travel'),

    -- EXPENSE: Personal Care
    ('CAT-0000000032', 'Personal Care', 'EXPENSE', '#E57AB3', 'https://img.icons8.com/color/96/spa.png', NULL),
    ('CAT-0000000033', 'Salon',         'EXPENSE', '#F0A2CC', 'https://img.icons8.com/color/96/barber-pole.png', 'Personal Care'),
    ('CAT-0000000034', 'Gym & Fitness', 'EXPENSE', '#F0A2CC', 'https://img.icons8.com/color/96/dumbbell.png', 'Personal Care'),

    -- EXPENSE: Misc
    ('CAT-0000000035', 'Gifts & Donations','EXPENSE','#C97D63','https://img.icons8.com/color/96/gift.png', NULL),
    ('CAT-0000000036', 'Taxes',         'EXPENSE', '#8E8E93', 'https://img.icons8.com/color/96/tax.png', NULL),
    ('CAT-0000000037', 'Fees & Charges','EXPENSE', '#8E8E93', 'https://img.icons8.com/color/96/receipt.png', NULL),
    ('CAT-0000000038', 'Pets',          'EXPENSE', '#6FC2B0', 'https://img.icons8.com/color/96/dog.png', NULL),

    -- INCOME
    ('CAT-0000000039', 'Income',        'INCOME',  '#27AE60', 'https://img.icons8.com/color/96/money-bag.png', NULL),
    ('CAT-0000000040', 'Salary',        'INCOME',  '#56D27E', 'https://img.icons8.com/color/96/money.png', 'Income'),
    ('CAT-0000000041', 'Bonus',         'INCOME',  '#56D27E', 'https://img.icons8.com/color/96/money-transfer.png', 'Income'),
    ('CAT-0000000042', 'Dividends',     'INCOME',  '#56D27E', 'https://img.icons8.com/color/96/stocks.png', 'Income'),
    ('CAT-0000000043', 'Refunds & Reimbursements','INCOME','#56D27E','https://img.icons8.com/color/96/refund-2.png','Income'),
    ('CAT-0000000044', 'Other Income',  'INCOME',  '#56D27E', 'https://img.icons8.com/color/96/money-bag-euro.png', 'Income');

-----------------------------------------------------------------------
-- 2) Upsert top-level categories
-----------------------------------------------------------------------
INSERT INTO t_category (
    id, user_id, name, color, category_type, icon_url,
    parent_category_id, created_at, updated_at, created_by, updated_by, version
)
SELECT
    d.id,
    'system',
    d.name,
    d.color,
    d.category_type,
    d.icon_url,
    NULL,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    'system', 'system',
    0
FROM _default_categories d
WHERE d.parent_name IS NULL
ON CONFLICT (user_id, name) DO NOTHING;

-----------------------------------------------------------------------
-- 3) Upsert child categories
-----------------------------------------------------------------------
INSERT INTO t_category (
    id, user_id, name, color, category_type, icon_url,
    parent_category_id, created_at, updated_at, created_by, updated_by, version
)
SELECT
    d.id,
    'system',
    d.name,
    d.color,
    d.category_type,
    d.icon_url,
    p.id AS parent_category_id,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    'system', 'system',
    0
FROM _default_categories d
JOIN t_category p
  ON p.user_id = 'system'
 AND p.name    = d.parent_name
WHERE d.parent_name IS NOT NULL
ON CONFLICT (user_id, name) DO NOTHING;

COMMIT;

-- ============================================================================
-- V6: Create Transaction table
-- ============================================================================
-- Migration: Create Transaction table
-- Version: V6
-- Description: Creates the t_transaction table for storing financial transactions between wallets

CREATE TABLE IF NOT EXISTS t_transaction (
    id VARCHAR(30) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    transaction_type VARCHAR(10) NOT NULL,
    wallet_from_id VARCHAR(30),
    wallet_to_id VARCHAR(30),
    amount DECIMAL(17,2) NOT NULL,
    exchange_rate DECIMAL(17,6) NOT NULL DEFAULT 1.000000,
    note VARCHAR(1000),
    effective_date DATE NOT NULL,
    category_id VARCHAR(30),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    archived_at TIMESTAMP,
    archived_by VARCHAR(255),
    version BIGINT,
    
    -- Primary key constraint
    CONSTRAINT pk_transaction PRIMARY KEY (id),
    
    -- Foreign key constraint to user table
    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES app_user(username),
    
    -- Foreign key constraint to source wallet
    CONSTRAINT fk_transaction_wallet_from FOREIGN KEY (wallet_from_id) REFERENCES t_wallet(id),
    
    -- Foreign key constraint to destination wallet
    CONSTRAINT fk_transaction_wallet_to FOREIGN KEY (wallet_to_id) REFERENCES t_wallet(id),
    
    -- Foreign key constraint to category
    CONSTRAINT fk_transaction_category FOREIGN KEY (category_id) REFERENCES t_category(id),
    
    -- Check constraints for data validation
    CONSTRAINT chk_transaction_type_valid CHECK (transaction_type IN ('TRANSFER', 'EXPENSE', 'INCOME')),
    CONSTRAINT chk_transaction_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_transaction_exchange_rate_positive CHECK (exchange_rate > 0),
    CONSTRAINT chk_transaction_note_length CHECK (note IS NULL OR LENGTH(note) <= 1000),
    CONSTRAINT chk_transaction_wallet_from_to_different CHECK (wallet_from_id IS NULL OR wallet_to_id IS NULL OR wallet_from_id != wallet_to_id),
    
    -- Business logic constraints
    -- TRANSFER: requires both wallets (one can be NULL for external transfers)
    -- EXPENSE: requires wallet_from, category recommended
    -- INCOME: requires wallet_to, category recommended
    CONSTRAINT chk_transaction_transfer_wallets CHECK (
        transaction_type != 'TRANSFER' OR 
        (wallet_from_id IS NOT NULL OR wallet_to_id IS NOT NULL)
    ),
    CONSTRAINT chk_transaction_expense_wallet_from CHECK (
        transaction_type != 'EXPENSE' OR wallet_from_id IS NOT NULL
    ),
    CONSTRAINT chk_transaction_income_wallet_to CHECK (
        transaction_type != 'INCOME' OR wallet_to_id IS NOT NULL
    )
);

-- Create indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_transaction_user_id ON t_transaction(user_id);
CREATE INDEX IF NOT EXISTS idx_transaction_type ON t_transaction(transaction_type);
CREATE INDEX IF NOT EXISTS idx_transaction_effective_date ON t_transaction(effective_date);
CREATE INDEX IF NOT EXISTS idx_transaction_wallet_from ON t_transaction(wallet_from_id);
CREATE INDEX IF NOT EXISTS idx_transaction_wallet_to ON t_transaction(wallet_to_id);
CREATE INDEX IF NOT EXISTS idx_transaction_category ON t_transaction(category_id);

-- Create composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_transaction_user_type ON t_transaction(user_id, transaction_type);
CREATE INDEX IF NOT EXISTS idx_transaction_user_date ON t_transaction(user_id, effective_date);
CREATE INDEX IF NOT EXISTS idx_transaction_user_wallet_from ON t_transaction(user_id, wallet_from_id);
CREATE INDEX IF NOT EXISTS idx_transaction_user_wallet_to ON t_transaction(user_id, wallet_to_id);
CREATE INDEX IF NOT EXISTS idx_transaction_user_category ON t_transaction(user_id, category_id);

-- Create index for date range queries
CREATE INDEX IF NOT EXISTS idx_transaction_effective_date_desc ON t_transaction(effective_date DESC);

-- Create index for wallet transaction history
CREATE INDEX IF NOT EXISTS idx_transaction_wallet_from_date ON t_transaction(wallet_from_id, effective_date);
CREATE INDEX IF NOT EXISTS idx_transaction_wallet_to_date ON t_transaction(wallet_to_id, effective_date);

-- Add comments for documentation
COMMENT ON TABLE t_transaction IS 'Stores financial transactions between wallets with support for transfers, expenses, and income';
COMMENT ON COLUMN t_transaction.id IS 'Primary key - Pockito ID format: TXN-{10-20 digits}';
COMMENT ON COLUMN t_transaction.user_id IS 'Foreign key reference to the user who owns this transaction';
COMMENT ON COLUMN t_transaction.transaction_type IS 'Type of transaction - TRANSFER, EXPENSE, or INCOME';
COMMENT ON COLUMN t_transaction.wallet_from_id IS 'Foreign key reference to source wallet using Pockito ID format';
COMMENT ON COLUMN t_transaction.wallet_to_id IS 'Foreign key reference to destination wallet using Pockito ID format';
COMMENT ON COLUMN t_transaction.amount IS 'Base transaction amount in source wallet currency (precision: 17,2)';
COMMENT ON COLUMN t_transaction.exchange_rate IS 'Exchange rate for currency conversion (precision: 17,6, default: 1.000000)';
COMMENT ON COLUMN t_transaction.note IS 'Optional note providing additional transaction details (max 1000 characters)';
COMMENT ON COLUMN t_transaction.effective_date IS 'Date when the transaction takes effect';
COMMENT ON COLUMN t_transaction.category_id IS 'Foreign key reference to category using Pockito ID format';
COMMENT ON COLUMN t_transaction.created_at IS 'Timestamp when the transaction was first created';
COMMENT ON COLUMN t_transaction.updated_at IS 'Timestamp when the transaction was last updated';
COMMENT ON COLUMN t_transaction.created_by IS 'Username of who created the transaction record';
COMMENT ON COLUMN t_transaction.updated_by IS 'Username of who last updated the transaction record';
COMMENT ON COLUMN t_transaction.archived_at IS 'Timestamp when the transaction was archived (soft delete)';
COMMENT ON COLUMN t_transaction.archived_by IS 'Username of who archived the transaction record';
COMMENT ON COLUMN t_transaction.version IS 'Hibernate optimistic locking version';
