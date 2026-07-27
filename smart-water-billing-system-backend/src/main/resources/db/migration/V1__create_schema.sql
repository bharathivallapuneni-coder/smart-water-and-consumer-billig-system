-- =============================================================================
-- Smart Water Usage and Consumer Billing System
-- Flyway Migration: V1 — Initial Schema
-- Description : Creates all 6 core tables with constraints, indexes,
--               and foreign keys.
-- Author      : Smart Water Team
-- Created     : 2026-01-01
-- =============================================================================

-- =============================================================================
-- TABLE: apartments
-- Description: Physical apartment buildings managed by the system.
-- =============================================================================
CREATE TABLE IF NOT EXISTS apartments
(
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    apartment_number VARCHAR(50)  NOT NULL,
    building_name    VARCHAR(100) NOT NULL,
    address          TEXT         NOT NULL,
    total_floors     INTEGER      NOT NULL DEFAULT 1,
    total_households INTEGER      NOT NULL DEFAULT 0,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT pk_apartments
        PRIMARY KEY (id),
    CONSTRAINT uq_apartments_number
        UNIQUE (apartment_number),
    CONSTRAINT chk_apartments_total_floors
        CHECK (total_floors >= 1),
    CONSTRAINT chk_apartments_total_households
        CHECK (total_households >= 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_apartments_number
    ON apartments (apartment_number);
CREATE INDEX IF NOT EXISTS idx_apartments_building
    ON apartments (building_name);

COMMENT ON TABLE apartments IS 'Physical apartment buildings registered in the billing system';
COMMENT ON COLUMN apartments.apartment_number IS 'Unique code identifying the apartment block (e.g. APT-001)';
COMMENT ON COLUMN apartments.total_households IS 'Denormalised count — updated when households are created/deleted';

-- =============================================================================
-- TABLE: users
-- Description: System users — both ADMIN staff and RESIDENT account holders.
--              Created before households to avoid circular FK dependency.
-- =============================================================================
CREATE TABLE IF NOT EXISTS users
(
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    username   VARCHAR(50)  NOT NULL,
    email      VARCHAR(100) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    phone      VARCHAR(20),
    role       VARCHAR(20)  NOT NULL DEFAULT 'RESIDENT',
    is_enabled BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT pk_users
        PRIMARY KEY (id),
    CONSTRAINT uq_users_username
        UNIQUE (username),
    CONSTRAINT uq_users_email
        UNIQUE (email),
    CONSTRAINT chk_users_role
        CHECK (role IN ('ADMIN', 'RESIDENT'))
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_users_email
    ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_username
    ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_role
    ON users (role);

COMMENT ON TABLE users IS 'System users: ADMIN staff and RESIDENT account holders';
COMMENT ON COLUMN users.password IS 'BCrypt-hashed password — never store plaintext';
COMMENT ON COLUMN users.role IS 'ADMIN: full system access | RESIDENT: own household data only';

-- =============================================================================
-- TABLE: households
-- Description: Individual dwelling units within an apartment.
--              Owns the FK to both apartments and users.
-- =============================================================================
CREATE TABLE IF NOT EXISTS households
(
    id               UUID        NOT NULL DEFAULT gen_random_uuid(),
    household_number VARCHAR(50) NOT NULL,
    owner_name       VARCHAR(100) NOT NULL,
    contact_phone    VARCHAR(20),
    apartment_id     UUID        NOT NULL,
    user_id          UUID,
    is_active        BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT pk_households
        PRIMARY KEY (id),
    CONSTRAINT uq_households_number
        UNIQUE (household_number),
    CONSTRAINT uq_households_user
        UNIQUE (user_id),
    CONSTRAINT fk_households_apartment
        FOREIGN KEY (apartment_id) REFERENCES apartments (id) ON DELETE RESTRICT,
    CONSTRAINT fk_households_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_households_apartment
    ON households (apartment_id);
CREATE INDEX IF NOT EXISTS idx_households_number
    ON households (household_number);
CREATE INDEX IF NOT EXISTS idx_households_active
    ON households (is_active);
CREATE INDEX IF NOT EXISTS idx_households_user
    ON households (user_id);

COMMENT ON TABLE households IS 'Individual dwelling units within an apartment building';
COMMENT ON COLUMN households.user_id IS 'Linked RESIDENT user account — nullable; ADMIN users have no household';
COMMENT ON COLUMN households.is_active IS 'Soft-delete flag; inactive households are excluded from billing';

-- =============================================================================
-- TABLE: tariff_plans
-- Description: Water pricing plans. A plan defines rate-per-unit, fixed
--              charges, and the period during which it is applicable.
-- =============================================================================
CREATE TABLE IF NOT EXISTS tariff_plans
(
    id             UUID           NOT NULL DEFAULT gen_random_uuid(),
    plan_name      VARCHAR(100)   NOT NULL,
    rate_per_unit  NUMERIC(10, 4) NOT NULL,
    fixed_charge   NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    min_units      NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    effective_from DATE           NOT NULL,
    effective_to   DATE,
    is_active      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT pk_tariff_plans
        PRIMARY KEY (id),
    CONSTRAINT uq_tariff_plans_name
        UNIQUE (plan_name),
    CONSTRAINT chk_tariff_rate_positive
        CHECK (rate_per_unit > 0),
    CONSTRAINT chk_tariff_fixed_charge_non_negative
        CHECK (fixed_charge >= 0),
    CONSTRAINT chk_tariff_min_units_non_negative
        CHECK (min_units >= 0),
    CONSTRAINT chk_tariff_date_range
        CHECK (effective_to IS NULL OR effective_to > effective_from)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_tariff_plans_active
    ON tariff_plans (is_active);
CREATE INDEX IF NOT EXISTS idx_tariff_plans_effective_dates
    ON tariff_plans (effective_from, effective_to);

COMMENT ON TABLE tariff_plans IS 'Water pricing plans with rate-per-unit, fixed charges, and validity periods';
COMMENT ON COLUMN tariff_plans.rate_per_unit IS 'Cost per cubic metre / kL of water consumed';
COMMENT ON COLUMN tariff_plans.fixed_charge IS 'Fixed monthly service/connection charge regardless of consumption';
COMMENT ON COLUMN tariff_plans.min_units IS 'Minimum chargeable units even if actual consumption is lower';

-- =============================================================================
-- TABLE: billing_cycles
-- Description: Monthly billing record per household.
--              Unique per (household, month, year).
-- =============================================================================
CREATE TABLE IF NOT EXISTS billing_cycles
(
    id                   UUID           NOT NULL DEFAULT gen_random_uuid(),
    household_id         UUID           NOT NULL,
    tariff_plan_id       UUID           NOT NULL,
    billing_month        INTEGER        NOT NULL,
    billing_year         INTEGER        NOT NULL,
    total_units_consumed NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    total_amount         NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    status               VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    due_date             DATE           NOT NULL,
    paid_date            DATE,
    created_at           TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT pk_billing_cycles
        PRIMARY KEY (id),
    CONSTRAINT uq_billing_household_month_year
        UNIQUE (household_id, billing_month, billing_year),
    CONSTRAINT fk_billing_household
        FOREIGN KEY (household_id) REFERENCES households (id) ON DELETE RESTRICT,
    CONSTRAINT fk_billing_tariff_plan
        FOREIGN KEY (tariff_plan_id) REFERENCES tariff_plans (id) ON DELETE RESTRICT,
    CONSTRAINT chk_billing_month
        CHECK (billing_month BETWEEN 1 AND 12),
    CONSTRAINT chk_billing_year
        CHECK (billing_year >= 2020),
    CONSTRAINT chk_billing_status
        CHECK (status IN ('PENDING', 'PAID', 'OVERDUE')),
    CONSTRAINT chk_billing_amount_non_negative
        CHECK (total_amount >= 0),
    CONSTRAINT chk_billing_units_non_negative
        CHECK (total_units_consumed >= 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_billing_household
    ON billing_cycles (household_id);
CREATE INDEX IF NOT EXISTS idx_billing_year_month
    ON billing_cycles (billing_year, billing_month);
CREATE INDEX IF NOT EXISTS idx_billing_status
    ON billing_cycles (status);
CREATE INDEX IF NOT EXISTS idx_billing_due_date
    ON billing_cycles (due_date);

COMMENT ON TABLE billing_cycles IS 'Monthly billing records per household; one row per household per calendar month';
COMMENT ON COLUMN billing_cycles.status IS 'PENDING: awaiting payment | PAID: settled | OVERDUE: past due date';
COMMENT ON COLUMN billing_cycles.total_units_consumed IS 'Aggregated water units from water_usages for the billing period';
COMMENT ON COLUMN billing_cycles.total_amount IS 'Computed: (units_consumed * rate_per_unit) + fixed_charge';

-- =============================================================================
-- TABLE: water_usages
-- Description: Daily meter readings per household.
--              Unique per (household, date) — one reading per day.
-- =============================================================================
CREATE TABLE IF NOT EXISTS water_usages
(
    id               UUID           NOT NULL DEFAULT gen_random_uuid(),
    household_id     UUID           NOT NULL,
    reading_date     DATE           NOT NULL,
    meter_reading    NUMERIC(12, 2) NOT NULL,
    previous_reading NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    units_consumed   NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    reading_type     VARCHAR(20)    NOT NULL DEFAULT 'MANUAL',
    notes            TEXT,
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT pk_water_usages
        PRIMARY KEY (id),
    CONSTRAINT uq_water_household_date
        UNIQUE (household_id, reading_date),
    CONSTRAINT fk_water_household
        FOREIGN KEY (household_id) REFERENCES households (id) ON DELETE RESTRICT,
    CONSTRAINT chk_water_reading_type
        CHECK (reading_type IN ('MANUAL', 'CSV_IMPORT')),
    CONSTRAINT chk_water_meter_reading_non_negative
        CHECK (meter_reading >= 0),
    CONSTRAINT chk_water_previous_reading_non_negative
        CHECK (previous_reading >= 0),
    CONSTRAINT chk_water_units_consumed_non_negative
        CHECK (units_consumed >= 0),
    CONSTRAINT chk_water_reading_progression
        CHECK (meter_reading >= previous_reading)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_water_household
    ON water_usages (household_id);
CREATE INDEX IF NOT EXISTS idx_water_date
    ON water_usages (reading_date);
CREATE INDEX IF NOT EXISTS idx_water_household_date
    ON water_usages (household_id, reading_date);
CREATE INDEX IF NOT EXISTS idx_water_reading_type
    ON water_usages (reading_type);

COMMENT ON TABLE water_usages IS 'Daily water meter readings per household; supports both manual entry and CSV import';
COMMENT ON COLUMN water_usages.meter_reading IS 'Cumulative meter reading at reading_date';
COMMENT ON COLUMN water_usages.previous_reading IS 'Meter reading from the immediately preceding entry';
COMMENT ON COLUMN water_usages.units_consumed IS 'Computed: meter_reading - previous_reading; stored for query performance';
COMMENT ON COLUMN water_usages.reading_type IS 'MANUAL: entered by admin | CSV_IMPORT: bulk uploaded via CSV file';
