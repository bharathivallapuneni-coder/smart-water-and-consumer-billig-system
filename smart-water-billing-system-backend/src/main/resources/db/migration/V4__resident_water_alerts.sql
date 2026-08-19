-- =============================================================================
-- Smart Water Usage and Consumer Billing System
-- Flyway Migration: V4 — Resident Water Usage Alerts & Leak Detection
-- Description : Adds household, billing cycle, alert type, severity, tariff tier,
--               consumption metrics, and resolution tracking to notifications table.
-- Author      : Smart Water Team
-- Created     : 2026-08-11
-- =============================================================================

ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS household_id UUID REFERENCES households (id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS billing_cycle_id UUID REFERENCES billing_cycles (id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS alert_type VARCHAR(50) NOT NULL DEFAULT 'HIGH_CONSUMPTION',
    ADD COLUMN IF NOT EXISTS severity VARCHAR(20) NOT NULL DEFAULT 'WARNING',
    ADD COLUMN IF NOT EXISTS tariff_tier VARCHAR(100),
    ADD COLUMN IF NOT EXISTS current_consumption NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS average_consumption NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS standard_deviation NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS is_resolved BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS resolved_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_notifications_household ON notifications (household_id);
CREATE INDEX IF NOT EXISTS idx_notifications_alert_type ON notifications (alert_type);
CREATE INDEX IF NOT EXISTS idx_notifications_severity ON notifications (severity);
CREATE INDEX IF NOT EXISTS idx_notifications_resolved ON notifications (is_resolved);
