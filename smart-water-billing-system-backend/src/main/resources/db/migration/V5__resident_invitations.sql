-- =============================================================================
-- Smart Water Usage and Consumer Billing System
-- Flyway Migration: V5 — Resident Invitations & Single-Use Tokens
-- Description : Adds block_number and invitation_status to households, and
--               creates the invitation_tokens table for resident onboarding.
-- Author      : Smart Water Team
-- Created     : 2026-08-15
-- =============================================================================

ALTER TABLE households
    ADD COLUMN IF NOT EXISTS block_number VARCHAR(50),
    ADD COLUMN IF NOT EXISTS invitation_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

CREATE TABLE IF NOT EXISTS invitation_tokens
(
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    household_id UUID         REFERENCES households (id) ON DELETE CASCADE,
    apartment_id UUID         REFERENCES apartments (id) ON DELETE CASCADE,
    token        VARCHAR(255) NOT NULL UNIQUE,
    full_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(100) NOT NULL,
    phone        VARCHAR(20),
    flat_number  VARCHAR(50)  NOT NULL,
    block_number VARCHAR(50),
    expiry_date  TIMESTAMP    NOT NULL,
    is_used      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_invitation_tokens PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_invitation_tokens_token ON invitation_tokens (token);
CREATE INDEX IF NOT EXISTS idx_invitation_tokens_email ON invitation_tokens (email);
