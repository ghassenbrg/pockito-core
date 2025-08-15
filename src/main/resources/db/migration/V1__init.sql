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
  version BIGINT DEFAULT 0
);

-- app_user (Keycloak sub UUID as PK)
CREATE TABLE app_user (
  id UUID PRIMARY KEY,
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
  version BIGINT DEFAULT 0
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
  UNIQUE (base_code, quote_code, effective_at)
);

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
  version BIGINT DEFAULT 0
);

-- wallet
CREATE TABLE wallet (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES app_user(id),
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
  CONSTRAINT savings_goal_check CHECK ((type <> 'SAVINGS') OR (goal_amount IS NULL OR goal_amount >= 0))
);
CREATE UNIQUE INDEX uq_wallet_user_name_active
  ON wallet (user_id, lower(name))
  WHERE archived_at IS NULL;
CREATE INDEX ix_wallet_user ON wallet(user_id);

-- category
CREATE TABLE category (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES app_user(id),
  type category_type_enum NOT NULL,
  name TEXT NOT NULL,
  color VARCHAR(7),
  icon_type icon_type_enum,
  icon_value TEXT,
  parent_id UUID REFERENCES category(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  archived_at TIMESTAMPTZ,
  archived_by VARCHAR(255),
  version BIGINT DEFAULT 0
);
CREATE UNIQUE INDEX uq_category_user_type_name_active
  ON category (user_id, type, lower(name))
  WHERE archived_at IS NULL;
CREATE INDEX ix_category_user_type ON category(user_id, type);

-- txn
CREATE TABLE txn (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES app_user(id),
  type txn_type_enum NOT NULL,
  occurred_at DATE NOT NULL,
  note TEXT,
  wallet_id UUID REFERENCES wallet(id),
  category_id UUID REFERENCES category(id),
  amount NUMERIC(18,2),
  currency_code CHAR(3) REFERENCES currency(code),
  from_wallet_id UUID REFERENCES wallet(id),
  to_wallet_id UUID REFERENCES wallet(id),
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
  CONSTRAINT txn_exp_inc_check CHECK (
    (type IN ('EXPENSE','INCOME') AND wallet_id IS NOT NULL AND amount IS NOT NULL AND currency_code IS NOT NULL AND from_wallet_id IS NULL AND to_wallet_id IS NULL)
    OR
    (type = 'TRANSFER' AND (from_wallet_id IS NOT NULL OR to_wallet_id IS NOT NULL) AND (from_amount IS NOT NULL OR to_amount IS NOT NULL))
  )
);
CREATE INDEX ix_txn_user_date ON txn(user_id, occurred_at DESC);
CREATE INDEX ix_txn_user_type ON txn(user_id, type);

-- subscription
CREATE TABLE subscription (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES app_user(id),
  name TEXT NOT NULL,
  icon_type icon_type_enum,
  icon_value TEXT,
  amount NUMERIC(18,2) NOT NULL,
  currency_code CHAR(3) NOT NULL REFERENCES currency(code),
  wallet_id UUID REFERENCES wallet(id),
  category_id UUID REFERENCES category(id),
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
  version BIGINT DEFAULT 0
);
CREATE INDEX ix_subscription_user_next ON subscription(user_id, next_due_date);

-- subscription_payment
CREATE TABLE subscription_payment (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  subscription_id UUID NOT NULL REFERENCES subscription(id),
  txn_id UUID REFERENCES txn(id),
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
  version BIGINT DEFAULT 0
);
CREATE INDEX ix_subscription_payment_sub ON subscription_payment(subscription_id, paid_at DESC);

-- budget
CREATE TABLE budget (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES app_user(id),
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
  version BIGINT DEFAULT 0
);
CREATE UNIQUE INDEX uq_budget_user_name_active
  ON budget(user_id, lower(name))
  WHERE archived_at IS NULL;

CREATE TABLE budget_category (
  budget_id UUID NOT NULL REFERENCES budget(id) ON DELETE CASCADE,
  category_id UUID NOT NULL REFERENCES category(id),
  PRIMARY KEY (budget_id, category_id)
);

-- agreement
CREATE TABLE agreement (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES app_user(id),
  person_name TEXT NOT NULL,
  type agreement_type_enum NOT NULL,
  principal_amount NUMERIC(18,2) NOT NULL,
  currency_code CHAR(3) NOT NULL REFERENCES currency(code),
  wallet_id UUID REFERENCES wallet(id),
  start_date DATE NOT NULL DEFAULT CURRENT_DATE,
  note TEXT,
  status TEXT NOT NULL DEFAULT 'OPEN',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  archived_at TIMESTAMPTZ,
  archived_by VARCHAR(255),
  version BIGINT DEFAULT 0
);
CREATE INDEX ix_agreement_user_status ON agreement(user_id, status);

-- agreement_payment
CREATE TABLE agreement_payment (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  agreement_id UUID NOT NULL REFERENCES agreement(id) ON DELETE CASCADE,
  txn_id UUID REFERENCES txn(id),
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
  version BIGINT DEFAULT 0
);
CREATE INDEX ix_agreement_payment_agreement ON agreement_payment(agreement_id, paid_at DESC);

-- activity_log
CREATE TABLE activity_log (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES app_user(id),
  entity_type TEXT NOT NULL,
  entity_id UUID NOT NULL,
  action TEXT NOT NULL,
  payload JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_activity_user_entity ON activity_log(user_id, entity_type, entity_id, created_at DESC);
