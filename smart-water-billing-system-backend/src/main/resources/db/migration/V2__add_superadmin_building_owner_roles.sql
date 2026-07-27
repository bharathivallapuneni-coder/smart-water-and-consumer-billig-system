-- =============================================================================
-- Smart Water Usage and Consumer Billing System
-- Flyway Migration: V2 — Superadmin, Building Owner Roles & Approval Status
-- =============================================================================

-- Add approval_status column to users table if it does not exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS approval_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED';

-- Ensure all existing rows have a valid approval_status
UPDATE users SET approval_status = 'APPROVED' WHERE approval_status IS NULL;

-- Drop any old check constraints on users role (both custom and Hibernate auto-generated names)
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_role;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

-- Add updated check constraint for users role
ALTER TABLE users ADD CONSTRAINT chk_users_role 
    CHECK (role IN ('SUPERADMIN', 'ADMIN', 'BUILDING_OWNER', 'RESIDENT'));

-- Drop any old check constraints on users approval_status
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_approval_status;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_approval_status_check;

-- Add updated check constraint for approval_status
ALTER TABLE users ADD CONSTRAINT chk_users_approval_status
    CHECK (approval_status IN ('PENDING', 'APPROVED', 'REJECTED'));
