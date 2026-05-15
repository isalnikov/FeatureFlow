-- V4__add_audit_columns.sql
-- Audit trail columns for key entities

ALTER TABLE products ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE products ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE teams ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE teams ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE features ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE features ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
