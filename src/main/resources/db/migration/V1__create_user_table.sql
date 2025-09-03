-- Migration: Create User table
-- Version: V4
-- Description: Creates the app_user table for storing registered application users

CREATE TABLE app_user (
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
CREATE INDEX idx_app_user_username ON app_user(username);

-- Create index on country for potential filtering
CREATE INDEX idx_app_user_country ON app_user(country);

-- Create index on default_currency for potential filtering
CREATE INDEX idx_app_user_default_currency ON app_user(default_currency);

-- Create index on created_at for potential date-based queries
CREATE INDEX idx_app_user_created_at ON app_user(created_at);

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
