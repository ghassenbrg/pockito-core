-- Add missing auditable columns to activity_log table
-- These columns are expected by the AuditableEntity hierarchy

ALTER TABLE activity_log 
ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
ADD COLUMN created_by VARCHAR(255),
ADD COLUMN updated_by VARCHAR(255),
ADD COLUMN archived_at TIMESTAMPTZ,
ADD COLUMN archived_by VARCHAR(255),
ADD COLUMN version BIGINT DEFAULT 0;

-- Add trigger to automatically update updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_activity_log_updated_at 
    BEFORE UPDATE ON activity_log 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
