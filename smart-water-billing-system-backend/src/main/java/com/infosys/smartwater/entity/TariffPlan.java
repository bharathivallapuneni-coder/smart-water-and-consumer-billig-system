package com.infosys.smartwater.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a water pricing tariff plan applicable to households during billing.
 *
 * <p>A {@code TariffPlan} encodes the financial parameters used to compute
 * the monthly bill amount in {@link BillingCycle}:
 * <pre>
 *   Bill Amount = max(unitsConsumed, minUnits) × ratePerUnit + fixedCharge
 * </pre>
 *
 * <p><b>Validity:</b> Plans have an {@code effectiveFrom} date and an optional
 * {@code effectiveTo} date. A {@code null} {@code effectiveTo} means the plan
 * is open-ended (currently active). Only ONE plan should have {@code isActive = true}
 * at any point in time; the service layer enforces this invariant.
 *
 * <p><b>Relationship:</b>
 * <pre>
 *   TariffPlan  1 ── * BillingCycle
 * </pre>
 *
 * <p>Deletion of a {@code TariffPlan} is blocked at the DB level
 * ({@code ON DELETE RESTRICT}) as long as billing cycles reference it.
 */
@Entity
@Table(
        name = "tariff_plans",
        indexes = {
                @Index(name = "idx_tariff_plans_active",          columnList = "is_active"),
                @Index(name = "idx_tariff_plans_effective_dates", columnList = "effective_from,effective_to")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "billingCycles")
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class TariffPlan extends BaseEntity {

    // -------------------------------------------------------------------------
    // Core fields
    // -------------------------------------------------------------------------

    /**
     * Human-readable unique name for the plan (e.g., {@code "Standard 2026"}).
     */
    @EqualsAndHashCode.Include
    @NotBlank(message = "Plan name is required")
    @Size(max = 100, message = "Plan name cannot exceed 100 characters")
    @Column(name = "plan_name", nullable = false, unique = true, length = 100)
    private String planName;

    /**
     * Cost per unit (cubic metre / kL) of water consumed.
     * Must be strictly positive. Stored with 4 decimal places for precision.
     */
    @NotNull(message = "Rate per unit is required")
    @DecimalMin(value = "0.0001", inclusive = true, message = "Rate per unit must be greater than 0")
    @Digits(integer = 6, fraction = 4, message = "Rate per unit must have at most 6 integer and 4 decimal digits")
    @Column(name = "rate_per_unit", nullable = false, precision = 10, scale = 4)
    private BigDecimal ratePerUnit;

    /**
     * Fixed monthly service / connection charge applied regardless of consumption.
     * Defaults to {@code 0.00} (no fixed charge).
     */
    @NotNull(message = "Fixed charge is required")
    @DecimalMin(value = "0.00", message = "Fixed charge cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Fixed charge must have at most 8 integer and 2 decimal digits")
    @Column(name = "fixed_charge", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal fixedCharge = BigDecimal.ZERO;

    /**
     * Minimum chargeable units per billing cycle.
     * If actual consumption is below this value, the household is still charged
     * for {@code minUnits} at {@code ratePerUnit}.
     * Defaults to {@code 0.00} (no minimum).
     */
    @NotNull(message = "Minimum units is required")
    @DecimalMin(value = "0.00", message = "Minimum units cannot be negative")
    @Column(name = "min_units", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minUnits = BigDecimal.ZERO;

    /**
     * Date from which this tariff plan is effective (inclusive).
     */
    @NotNull(message = "Effective from date is required")
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    /**
     * Date after which this tariff plan expires (exclusive).
     * {@code null} indicates the plan is open-ended.
     */
    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    /**
     * Whether this plan is currently active and eligible for new billing cycles.
     * The service layer ensures at most one plan is active at a time.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // -------------------------------------------------------------------------
    // Relationships
    // -------------------------------------------------------------------------

    /**
     * All billing cycles that were generated using this tariff plan.
     * Read-only from the TariffPlan side; use BillingCycleService to create cycles.
     */
    @OneToMany(mappedBy = "tariffPlan", fetch = FetchType.LAZY)
    @Builder.Default
    private List<BillingCycle> billingCycles = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Business logic helpers
    // -------------------------------------------------------------------------

    /**
     * Computes the bill amount for the given consumption.
     *
     * @param unitsConsumed actual units consumed in the billing period
     * @return total bill amount = max(unitsConsumed, minUnits) × ratePerUnit + fixedCharge
     */
    public BigDecimal computeBillAmount(BigDecimal unitsConsumed) {
        BigDecimal chargeableUnits = unitsConsumed.compareTo(minUnits) >= 0
                ? unitsConsumed
                : minUnits;
        return chargeableUnits.multiply(ratePerUnit).add(fixedCharge);
    }

    /**
     * Returns {@code true} if this plan is valid on the given date.
     *
     * @param date the date to check validity for
     * @return {@code true} if {@code date} is within [effectiveFrom, effectiveTo)
     */
    public boolean isValidOn(LocalDate date) {
        if (date.isBefore(effectiveFrom)) {
            return false;
        }
        return effectiveTo == null || !date.isAfter(effectiveTo);
    }
}
