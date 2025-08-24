-- Pockito Database Schema - Initial Migration V1
-- Based on Master Context §3.2

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- Create enums
CREATE TYPE wallet_type_enum    AS ENUM ('SAVINGS','BANK_ACCOUNT','CASH','CREDIT_CARD','CUSTOM');
CREATE TYPE txn_type_enum       AS ENUM ('EXPENSE','INCOME','TRANSFER');
CREATE TYPE category_type_enum  AS ENUM ('EXPENSE','INCOME');
CREATE TYPE freq_type_enum      AS ENUM ('WEEKLY','MONTHLY','QUARTERLY','ANNUALLY','CUSTOM');
CREATE TYPE agreement_type_enum AS ENUM ('BORROW','LEND');
CREATE TYPE icon_type_enum      AS ENUM ('EMOJI','URL');
CREATE TYPE payment_status_enum AS ENUM ('PAID','SKIPPED','FAILED');

-- currency
CREATE TABLE currency (
  code CHAR(3) PRIMARY KEY,
  name TEXT NOT NULL,
  symbol TEXT,
  decimals SMALLINT NOT NULL DEFAULT 2,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  archived_at TIMESTAMPTZ,
  archived_by VARCHAR(255),
  version BIGINT DEFAULT 0,
  CONSTRAINT chk_currency_decimals_valid CHECK (decimals >= 0 AND decimals <= 10)
);

-- app_user (Keycloak sub UUID as PK)
CREATE TABLE app_user (
  id VARCHAR(255) PRIMARY KEY,
  email CITEXT UNIQUE NOT NULL,
  display_name TEXT,
  locale VARCHAR(10),
  timezone VARCHAR(64),
  default_currency CHAR(3) REFERENCES currency(code),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  archived_at TIMESTAMPTZ,
  archived_by VARCHAR(255),
  version BIGINT DEFAULT 0,
  CONSTRAINT chk_user_locale_format CHECK (locale IS NULL OR locale ~ '^[a-z]{2}(-[A-Z]{2})?$'),
  CONSTRAINT chk_user_timezone_format CHECK (timezone IS NULL OR timezone ~ '^[A-Za-z_]+/[A-Za-z_]+$')
);

-- exchange_rate (snapshot)
CREATE TABLE exchange_rate (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  base_code CHAR(3) NOT NULL REFERENCES currency(code),
  quote_code CHAR(3) NOT NULL REFERENCES currency(code),
  rate NUMERIC(20,10) NOT NULL,
  effective_at TIMESTAMPTZ NOT NULL,
  source TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  archived_at TIMESTAMPTZ,
  archived_by VARCHAR(255),
  version BIGINT DEFAULT 0,
  CONSTRAINT uq_exchange_rate_base_quote_effective UNIQUE (base_code, quote_code, effective_at),
  CONSTRAINT chk_exchange_rate_positive CHECK (rate > 0),
  CONSTRAINT chk_exchange_rate_different_currencies CHECK (base_code != quote_code)
);
CREATE INDEX ix_exchange_rate_base_quote ON exchange_rate(base_code, quote_code);
CREATE INDEX ix_exchange_rate_effective ON exchange_rate(effective_at DESC);

-- icon_asset
CREATE TABLE icon_asset (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  type icon_type_enum NOT NULL,
  value TEXT NOT NULL,
  label TEXT,
  tags TEXT[],
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  archived_at TIMESTAMPTZ,
  archived_by VARCHAR(255),
  version BIGINT DEFAULT 0,
  CONSTRAINT chk_icon_asset_value_not_empty CHECK (length(trim(value)) > 0)
);
CREATE INDEX ix_icon_asset_type_active ON icon_asset(type, is_active);
CREATE INDEX ix_icon_asset_tags ON icon_asset USING GIN(tags);
CREATE INDEX ix_icon_asset_label ON icon_asset(label) WHERE label IS NOT NULL;

-- wallet
CREATE TABLE wallet (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id VARCHAR(255) NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  icon_type icon_type_enum NOT NULL,
  icon_value TEXT NOT NULL,
  currency_code CHAR(3) NOT NULL REFERENCES currency(code),
  color VARCHAR(7),
  type wallet_type_enum NOT NULL,
  initial_balance NUMERIC(18,2) NOT NULL DEFAULT 0,
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
  goal_amount NUMERIC(18,2),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  archived_at TIMESTAMPTZ,
  archived_by VARCHAR(255),
  version BIGINT DEFAULT 0,
  CONSTRAINT chk_wallet_savings_goal CHECK ((type <> 'SAVINGS') OR (goal_amount IS NULL OR goal_amount >= 0)),
  CONSTRAINT chk_wallet_color_format CHECK (color IS NULL OR color ~ '^#[0-9A-Fa-f]{6}$'),
  CONSTRAINT chk_wallet_icon_value_not_empty CHECK (length(trim(icon_value)) > 0),
  CONSTRAINT chk_wallet_name_not_empty CHECK (length(trim(name)) > 0)
);
CREATE UNIQUE INDEX uq_wallet_user_name_active
  ON wallet (user_id, lower(name))
  WHERE archived_at IS NULL;
CREATE UNIQUE INDEX uq_wallet_user_default_active
  ON wallet (user_id, is_default)
  WHERE archived_at IS NULL AND is_default = TRUE;
CREATE INDEX ix_wallet_user ON wallet(user_id);
CREATE INDEX ix_wallet_user_type ON wallet(user_id, type);
CREATE INDEX ix_wallet_currency ON wallet(currency_code);
CREATE INDEX ix_wallet_archived ON wallet(archived_at) WHERE archived_at IS NOT NULL;

-- category
CREATE TABLE category (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id VARCHAR(255) NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  type category_type_enum NOT NULL,
  name TEXT NOT NULL,
  color VARCHAR(7),
  icon_type icon_type_enum,
  icon_value TEXT,
  parent_id UUID REFERENCES category(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  archived_at TIMESTAMPTZ,
  archived_by VARCHAR(255),
  version BIGINT DEFAULT 0,
  CONSTRAINT chk_category_color_format CHECK (color IS NULL OR color ~ '^#[0-9A-Fa-f]{6}$'),
  CONSTRAINT chk_category_parent_hierarchy CHECK (parent_id IS NULL OR parent_id != id),
  CONSTRAINT chk_category_name_not_empty CHECK (length(trim(name)) > 0),
  CONSTRAINT chk_category_icon_value_not_empty CHECK (icon_value IS NULL OR length(trim(icon_value)) > 0)
);
CREATE UNIQUE INDEX uq_category_user_type_name_active
  ON category (user_id, type, lower(name))
  WHERE archived_at IS NULL;
CREATE INDEX ix_category_user_type ON category(user_id, type);
CREATE INDEX ix_category_parent ON category(parent_id);
CREATE INDEX ix_category_hierarchy ON category(user_id, parent_id);
CREATE INDEX ix_category_archived ON category(archived_at) WHERE archived_at IS NOT NULL;

-- txn
CREATE TABLE txn (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id VARCHAR(255) NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  type txn_type_enum NOT NULL,
  occurred_at DATE NOT NULL,
  note TEXT,
  wallet_id UUID REFERENCES wallet(id) ON DELETE SET NULL,
  category_id UUID REFERENCES category(id) ON DELETE SET NULL,
  amount NUMERIC(18,2),
  currency_code CHAR(3) REFERENCES currency(code),
  from_wallet_id UUID REFERENCES wallet(id) ON DELETE SET NULL,
  to_wallet_id UUID REFERENCES wallet(id) ON DELETE SET NULL,
  from_amount NUMERIC(18,2),
  to_amount NUMERIC(18,2),
  from_currency_code CHAR(3) REFERENCES currency(code),
  to_currency_code CHAR(3) REFERENCES currency(code),
  exchange_rate NUMERIC(20,10),
  external_wallet_name TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  archived_at TIMESTAMPTZ,
  archived_by VARCHAR(255),
  version BIGINT DEFAULT 0,
  CONSTRAINT chk_txn_exp_inc_valid CHECK (
    (type IN ('EXPENSE','INCOME') AND wallet_id IS NOT NULL AND amount IS NOT NULL AND currency_code IS NOT NULL AND from_wallet_id IS NULL AND to_wallet_id IS NULL)
    OR
    (type = 'TRANSFER' AND (from_wallet_id IS NOT NULL OR to_wallet_id IS NOT NULL) AND (from_amount IS NOT NULL OR to_amount IS NOT NULL))
  ),
  CONSTRAINT chk_txn_amount_positive CHECK (amount IS NULL OR amount > 0),
  CONSTRAINT chk_txn_from_amount_positive CHECK (from_amount IS NULL OR from_amount > 0),
  CONSTRAINT chk_txn_to_amount_positive CHECK (to_amount IS NULL OR to_amount > 0),
  CONSTRAINT chk_txn_exchange_rate_positive CHECK (exchange_rate IS NULL OR exchange_rate > 0),
  CONSTRAINT chk_txn_occurred_at_not_future CHECK (occurred_at <= CURRENT_DATE),
  CONSTRAINT chk_txn_note_length CHECK (note IS NULL OR length(note) <= 1000)
);
CREATE INDEX ix_txn_user_date ON txn(user_id, occurred_at DESC);
CREATE INDEX ix_txn_user_type ON txn(user_id, type);
CREATE INDEX ix_txn_wallet ON txn(wallet_id);
CREATE INDEX ix_txn_category ON txn(category_id);
CREATE INDEX ix_txn_from_wallet ON txn(from_wallet_id);
CREATE INDEX ix_txn_to_wallet ON txn(to_wallet_id);
CREATE INDEX ix_txn_currency ON txn(currency_code);
CREATE INDEX ix_txn_archived ON txn(archived_at) WHERE archived_at IS NOT NULL;
CREATE INDEX ix_txn_occurred_at ON txn(occurred_at);

-- subscription
CREATE TABLE subscription (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id VARCHAR(255) NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  icon_type icon_type_enum,
  icon_value TEXT,
  amount NUMERIC(18,2) NOT NULL,
  currency_code CHAR(3) NOT NULL REFERENCES currency(code),
  wallet_id UUID REFERENCES wallet(id) ON DELETE SET NULL,
  category_id UUID REFERENCES category(id) ON DELETE SET NULL,
  frequency freq_type_enum NOT NULL,
  interval INT NOT NULL DEFAULT 1,
  day_of_month SMALLINT,
  day_of_week SMALLINT,
  month_of_year SMALLINT,
  start_date DATE NOT NULL,
  next_due_date DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  archived_at TIMESTAMPTZ,
  archived_by VARCHAR(255),
  version BIGINT DEFAULT 0,
  CONSTRAINT chk_subscription_amount_positive CHECK (amount > 0),
  CONSTRAINT chk_subscription_interval_positive CHECK (interval > 0),
  CONSTRAINT chk_subscription_day_of_month CHECK (day_of_month IS NULL OR (day_of_month >= 1 AND day_of_month <= 31)),
  CONSTRAINT chk_subscription_day_of_week CHECK (day_of_week IS NULL OR (day_of_week >= 0 AND day_of_week <= 6)),
  CONSTRAINT chk_subscription_month_of_year CHECK (month_of_year IS NULL OR (month_of_year >= 1 AND month_of_year <= 12)),
  CONSTRAINT chk_subscription_start_date_not_future CHECK (start_date <= CURRENT_DATE),
  CONSTRAINT chk_subscription_next_due_not_before_start CHECK (next_due_date IS NULL OR next_due_date >= start_date),
  CONSTRAINT chk_subscription_name_not_empty CHECK (length(trim(name)) > 0),
  CONSTRAINT chk_subscription_icon_value_not_empty CHECK (icon_value IS NULL OR length(trim(icon_value)) > 0)
);
CREATE UNIQUE INDEX uq_subscription_user_name_active
  ON subscription(user_id, lower(name))
  WHERE archived_at IS NULL;
CREATE INDEX ix_subscription_user_next ON subscription(user_id, next_due_date);
CREATE INDEX ix_subscription_wallet ON subscription(wallet_id);
CREATE INDEX ix_subscription_category ON subscription(category_id);
CREATE INDEX ix_subscription_frequency ON subscription(frequency);
CREATE INDEX ix_subscription_archived ON subscription(archived_at) WHERE archived_at IS NOT NULL;

-- subscription_payment
CREATE TABLE subscription_payment (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  subscription_id UUID NOT NULL REFERENCES subscription(id) ON DELETE CASCADE,
  txn_id UUID REFERENCES txn(id) ON DELETE SET NULL,
  amount NUMERIC(18,2) NOT NULL,
  currency_code CHAR(3) NOT NULL REFERENCES currency(code),
  paid_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  auto BOOLEAN NOT NULL DEFAULT TRUE,
  status payment_status_enum NOT NULL DEFAULT 'PAID',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  archived_at TIMESTAMPTZ,
  archived_by VARCHAR(255),
  version BIGINT DEFAULT 0,
  CONSTRAINT chk_subscription_payment_amount_positive CHECK (amount > 0),
  CONSTRAINT chk_subscription_payment_paid_at_not_future CHECK (paid_at <= CURRENT_TIMESTAMP)
);
CREATE INDEX ix_subscription_payment_sub ON subscription_payment(subscription_id, paid_at DESC);
CREATE INDEX ix_subscription_payment_txn ON subscription_payment(txn_id);
CREATE INDEX ix_subscription_payment_status ON subscription_payment(status);
CREATE INDEX ix_subscription_payment_archived ON subscription_payment(archived_at) WHERE archived_at IS NOT NULL;

-- budget
CREATE TABLE budget (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id VARCHAR(255) NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  period freq_type_enum NOT NULL DEFAULT 'MONTHLY',
  start_date DATE NOT NULL,
  end_date DATE,
  limit_amount NUMERIC(18,2) NOT NULL,
  currency_code CHAR(3) NOT NULL REFERENCES currency(code),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  archived_at TIMESTAMPTZ,
  archived_by VARCHAR(255),
  version BIGINT DEFAULT 0,
  CONSTRAINT chk_budget_limit_positive CHECK (limit_amount > 0),
  CONSTRAINT chk_budget_date_range CHECK (end_date IS NULL OR end_date > start_date),
  CONSTRAINT chk_budget_start_date_not_future CHECK (start_date <= CURRENT_DATE),
  CONSTRAINT chk_budget_name_not_empty CHECK (length(trim(name)) > 0)
);
CREATE UNIQUE INDEX uq_budget_user_name_active
  ON budget(user_id, lower(name))
  WHERE archived_at IS NULL;
CREATE INDEX ix_budget_user_period ON budget(user_id, period);
CREATE INDEX ix_budget_currency ON budget(currency_code);
CREATE INDEX ix_budget_archived ON budget(archived_at) WHERE archived_at IS NOT NULL;

CREATE TABLE budget_category (
  budget_id UUID NOT NULL REFERENCES budget(id) ON DELETE CASCADE,
  category_id UUID NOT NULL REFERENCES category(id) ON DELETE CASCADE,
  PRIMARY KEY (budget_id, category_id)
);
CREATE INDEX ix_budget_category_category ON budget_category(category_id);

-- agreement
CREATE TABLE agreement (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id VARCHAR(255) NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  person_name TEXT NOT NULL,
  type agreement_type_enum NOT NULL,
  principal_amount NUMERIC(18,2) NOT NULL,
  currency_code CHAR(3) NOT NULL REFERENCES currency(code),
  wallet_id UUID REFERENCES wallet(id) ON DELETE SET NULL,
  start_date DATE NOT NULL DEFAULT CURRENT_DATE,
  note TEXT,
  status TEXT NOT NULL DEFAULT 'OPEN',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  archived_at TIMESTAMPTZ,
  archived_by VARCHAR(255),
  version BIGINT DEFAULT 0,
  CONSTRAINT chk_agreement_principal_positive CHECK (principal_amount > 0),
  CONSTRAINT chk_agreement_status_valid CHECK (status IN ('OPEN', 'CLOSED', 'SETTLED', 'DEFAULTED')),
  CONSTRAINT chk_agreement_start_date_not_future CHECK (start_date <= CURRENT_DATE),
  CONSTRAINT chk_agreement_person_name_not_empty CHECK (length(trim(person_name)) > 0),
  CONSTRAINT chk_agreement_note_length CHECK (note IS NULL OR length(note) <= 1000)
);
CREATE INDEX ix_agreement_user_status ON agreement(user_id, status);
CREATE INDEX ix_agreement_wallet ON agreement(wallet_id);
CREATE INDEX ix_agreement_currency ON agreement(currency_code);
CREATE INDEX ix_agreement_archived ON agreement(archived_at) WHERE archived_at IS NOT NULL;

-- agreement_payment
CREATE TABLE agreement_payment (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  agreement_id UUID NOT NULL REFERENCES agreement(id) ON DELETE CASCADE,
  txn_id UUID REFERENCES txn(id) ON DELETE SET NULL,
  amount NUMERIC(18,2) NOT NULL,
  currency_code CHAR(3) NOT NULL REFERENCES currency(code),
  paid_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  note TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  archived_at TIMESTAMPTZ,
  archived_by VARCHAR(255),
  version BIGINT DEFAULT 0,
  CONSTRAINT chk_agreement_payment_amount_positive CHECK (amount > 0),
  CONSTRAINT chk_agreement_payment_paid_at_not_future CHECK (paid_at <= CURRENT_TIMESTAMP),
  CONSTRAINT chk_agreement_payment_note_length CHECK (note IS NULL OR length(note) <= 1000)
);
CREATE INDEX ix_agreement_payment_agreement ON agreement_payment(agreement_id, paid_at DESC);
CREATE INDEX ix_agreement_payment_txn ON agreement_payment(txn_id);
CREATE INDEX ix_agreement_payment_archived ON agreement_payment(archived_at) WHERE archived_at IS NOT NULL;

-- activity_log
CREATE TABLE activity_log (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id VARCHAR(255) NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  entity_type TEXT NOT NULL,
  entity_id UUID NOT NULL,
  action TEXT NOT NULL,
  payload JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT chk_activity_log_entity_type_not_empty CHECK (length(trim(entity_type)) > 0),
  CONSTRAINT chk_activity_log_action_not_empty CHECK (length(trim(action)) > 0),
  CONSTRAINT chk_activity_log_created_at_not_future CHECK (created_at <= CURRENT_TIMESTAMP)
);
CREATE INDEX ix_activity_user_entity ON activity_log(user_id, entity_type, entity_id, created_at DESC);
CREATE INDEX ix_activity_entity ON activity_log(entity_type, entity_id);
CREATE INDEX ix_activity_created ON activity_log(created_at DESC);
CREATE INDEX ix_activity_action ON activity_log(action);

-- Insert default currencies
INSERT INTO currency (code, name, symbol, decimals, is_active) VALUES
  ('USD', 'US Dollar', '$', 2, true),
  ('EUR', 'Euro', '€', 2, true),
  ('GBP', 'British Pound', '£', 2, true),
  ('JPY', 'Japanese Yen', '¥', 0, true),
  ('CAD', 'Canadian Dollar', 'C$', 2, true),
  ('AUD', 'Australian Dollar', 'A$', 2, true),
  ('CHF', 'Swiss Franc', 'CHF', 2, true),
  ('CNY', 'Chinese Yuan', '¥', 2, true),
  ('INR', 'Indian Rupee', '₹', 2, true),
  ('BRL', 'Brazilian Real', 'R$', 2, true);

-- Insert default icon assets
INSERT INTO icon_asset (type, value, label, tags, is_active) VALUES
  ('EMOJI', '💰', 'Money Bag', ARRAY['money', 'cash', 'finance'], true),
  ('EMOJI', '🏦', 'Bank', ARRAY['bank', 'account', 'finance'], true),
  ('EMOJI', '💳', 'Credit Card', ARRAY['card', 'credit', 'payment'], true),
  ('EMOJI', '📊', 'Chart', ARRAY['chart', 'analytics', 'data'], true),
  ('EMOJI', '🎯', 'Target', ARRAY['target', 'goal', 'objective'], true),
  ('EMOJI', '📱', 'Mobile', ARRAY['mobile', 'phone', 'app'], true),
  ('EMOJI', '💻', 'Computer', ARRAY['computer', 'laptop', 'tech'], true),
  ('EMOJI', '🚗', 'Car', ARRAY['car', 'vehicle', 'transport'], true),
  ('EMOJI', '🏠', 'House', ARRAY['house', 'home', 'property'], true),
  ('EMOJI', '🍔', 'Food', ARRAY['food', 'meal', 'restaurant'], true),
  ('EMOJI', '👕', 'Clothing', ARRAY['clothing', 'fashion', 'apparel'], true),
  ('EMOJI', '🎬', 'Entertainment', ARRAY['entertainment', 'movie', 'fun'], true),
  ('EMOJI', '✈️', 'Travel', ARRAY['travel', 'flight', 'vacation'], true),
  ('EMOJI', '💊', 'Health', ARRAY['health', 'medical', 'pharmacy'], true),
  ('EMOJI', '🎓', 'Education', ARRAY['education', 'school', 'learning'], true);
