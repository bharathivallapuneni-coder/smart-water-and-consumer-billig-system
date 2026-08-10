-- =============================================================================
-- Smart Water Usage and Consumer Billing System
-- Flyway Migration: V3 — Milestone 2 Schema Additions
-- Description : Tiered Tariffs, Bulk Water Purchases, Household Invoices,
--               Notifications, Password Reset Tokens, and Household attributes.
-- Author      : Smart Water Team
-- Created     : 2026-08-06
-- =============================================================================

-- 1. Add flat_area, is_metered, and alert_threshold_kl to households
ALTER TABLE households 
    ADD COLUMN IF NOT EXISTS flat_area NUMERIC(10, 2) NOT NULL DEFAULT 1000.00,
    ADD COLUMN IF NOT EXISTS is_metered BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS alert_threshold_kl NUMERIC(10, 2) NOT NULL DEFAULT 20.00;

-- 2. Add building_owner_id to apartments for tenancy scoping
ALTER TABLE apartments 
    ADD COLUMN IF NOT EXISTS building_owner_id UUID REFERENCES users (id) ON DELETE SET NULL;

-- 3. Table: tariff_tiers (Configurable Tiered Tariffs per Apartment)
CREATE TABLE IF NOT EXISTS tariff_tiers (
    id             UUID           NOT NULL DEFAULT gen_random_uuid(),
    apartment_id   UUID           NOT NULL,
    tier_name      VARCHAR(50)    NOT NULL,
    min_kl         NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    max_kl         NUMERIC(10, 2), -- NULL means infinity / above min_kl
    rate_per_kl    NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    fixed_charge   NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_tariff_tiers PRIMARY KEY (id),
    CONSTRAINT fk_tariff_tiers_apartment FOREIGN KEY (apartment_id) REFERENCES apartments (id) ON DELETE CASCADE,
    CONSTRAINT chk_tariff_tiers_rate CHECK (rate_per_kl >= 0),
    CONSTRAINT chk_tariff_tiers_min CHECK (min_kl >= 0)
);

CREATE INDEX IF NOT EXISTS idx_tariff_tiers_apartment ON tariff_tiers (apartment_id);

-- 4. Table: bulk_water_purchases
CREATE TABLE IF NOT EXISTS bulk_water_purchases (
    id                  UUID           NOT NULL DEFAULT gen_random_uuid(),
    apartment_id        UUID           NOT NULL,
    billing_cycle_id    UUID,
    source_type         VARCHAR(50)    NOT NULL,
    supplier_name       VARCHAR(100),
    purchase_date       DATE           NOT NULL,
    purchased_volume_kl NUMERIC(10, 2) NOT NULL,
    total_cost          NUMERIC(10, 2) NOT NULL,
    unit_cost_per_kl    NUMERIC(10, 2) NOT NULL,
    notes               TEXT,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_bulk_water_purchases PRIMARY KEY (id),
    CONSTRAINT fk_bulk_water_apartment FOREIGN KEY (apartment_id) REFERENCES apartments (id) ON DELETE CASCADE,
    CONSTRAINT chk_bulk_volume_pos CHECK (purchased_volume_kl > 0),
    CONSTRAINT chk_bulk_cost_nonneg CHECK (total_cost >= 0)
);

CREATE INDEX IF NOT EXISTS idx_bulk_water_apartment ON bulk_water_purchases (apartment_id);
CREATE INDEX IF NOT EXISTS idx_bulk_water_cycle ON bulk_water_purchases (billing_cycle_id);

-- 5. Modify billing_cycles for cycle lifecycle & building scoping
ALTER TABLE billing_cycles 
    ADD COLUMN IF NOT EXISTS apartment_id UUID REFERENCES apartments (id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS cycle_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS opened_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS finalized_at TIMESTAMP;

-- Drop old billing_cycles status constraint if present and update to allow OPEN, FINALIZED, ARCHIVED, PENDING, PAID, OVERDUE
ALTER TABLE billing_cycles DROP CONSTRAINT IF EXISTS chk_billing_status;
ALTER TABLE billing_cycles DROP CONSTRAINT IF EXISTS billing_cycles_status_check;
ALTER TABLE billing_cycles ADD CONSTRAINT chk_billing_status 
    CHECK (status IN ('OPEN', 'FINALIZED', 'ARCHIVED', 'PENDING', 'PAID', 'OVERDUE'));

-- 6. Table: household_invoices (Itemized Household Invoices)
CREATE TABLE IF NOT EXISTS household_invoices (
    id                                  UUID           NOT NULL DEFAULT gen_random_uuid(),
    invoice_number                      VARCHAR(50)    NOT NULL,
    billing_cycle_id                    UUID           NOT NULL,
    household_id                        UUID           NOT NULL,
    resident_id                         UUID,
    apartment_id                        UUID           NOT NULL,
    billing_period                      VARCHAR(20)    NOT NULL,
    metered_consumption_kl              NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    flat_area_sqft                      NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    is_metered                          BOOLEAN        NOT NULL DEFAULT TRUE,
    base_tiered_charge                  NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    allocated_water_procurement_charge  NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    shared_area_charge                  NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    adjustments                         NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    total_amount                        NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    status                              VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    generated_at                        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date                            DATE           NOT NULL,
    paid_at                             TIMESTAMP,
    payment_id                          VARCHAR(100),
    breakdown_json                      TEXT,
    created_at                          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_household_invoices PRIMARY KEY (id),
    CONSTRAINT uq_invoice_number UNIQUE (invoice_number),
    CONSTRAINT uq_invoice_household_cycle UNIQUE (household_id, billing_cycle_id),
    CONSTRAINT fk_invoice_billing_cycle FOREIGN KEY (billing_cycle_id) REFERENCES billing_cycles (id) ON DELETE RESTRICT,
    CONSTRAINT fk_invoice_household FOREIGN KEY (household_id) REFERENCES households (id) ON DELETE RESTRICT,
    CONSTRAINT fk_invoice_apartment FOREIGN KEY (apartment_id) REFERENCES apartments (id) ON DELETE RESTRICT,
    CONSTRAINT chk_invoice_status CHECK (status IN ('PENDING', 'PAID', 'OVERDUE'))
);

CREATE INDEX IF NOT EXISTS idx_invoices_cycle ON household_invoices (billing_cycle_id);
CREATE INDEX IF NOT EXISTS idx_invoices_household ON household_invoices (household_id);
CREATE INDEX IF NOT EXISTS idx_invoices_resident ON household_invoices (resident_id);
CREATE INDEX IF NOT EXISTS idx_invoices_apartment ON household_invoices (apartment_id);

-- 7. Table: notifications
CREATE TABLE IF NOT EXISTS notifications (
    id                UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id           UUID,
    building_id       UUID,
    title             VARCHAR(150) NOT NULL,
    message           TEXT         NOT NULL,
    notification_type VARCHAR(50)  NOT NULL DEFAULT 'INFO',
    is_read           BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_building FOREIGN KEY (building_id) REFERENCES apartments (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications (user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_building ON notifications (building_id);
CREATE INDEX IF NOT EXISTS idx_notifications_read ON notifications (is_read);

-- 8. Table: password_reset_tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL,
    token       VARCHAR(100) NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,
    is_used     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id),
    CONSTRAINT uq_reset_token UNIQUE (token),
    CONSTRAINT fk_reset_token_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_reset_token_token ON password_reset_tokens (token);
CREATE INDEX IF NOT EXISTS idx_reset_token_user ON password_reset_tokens (user_id);
