package com.infosys.smartwater.entity;

import com.infosys.smartwater.entity.enums.BillingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a monthly billing record for a single {@link Household}.
 *
 * <p>A {@code BillingCycle} is generated once per calendar month per household
 * (enforced by the {@code uq_billing_household_month_year} unique constraint).
 * It aggregates all {@link WaterUsage} readings for the billing period and
 * applies the applicable {@link TariffPlan} to compute the total amount due.
 *
 * <p><b>Relationships:</b>
 * <pre>
 *   Household  1 ── * BillingCycle * ── 1  TariffPlan
 * </pre>
 *
 * <p><b>Status lifecycle:</b>
 * <pre>
 *   PENDING  ──► PAID   (payment recorded)
 *   PENDING  ──► OVERDUE (due date passed, scheduled job)
 *   OVERDUE  ──► PAID   (late payment recorded)
 * </pre>
 *
 * <p><b>Deletion policy:</b> Blocked at the DB level. A billing record must be
 * archived rather than hard-deleted to preserve financial history.
 */
@Entity
@Table(
        name = "billing_cycles",
        indexes = {
                @Index(name = "idx_billing_household",  columnList = "household_id"),
                @Index(name = "idx_billing_year_month", columnList = "billing_year,billing_month"),
                @Index(name = "idx_billing_status",     columnList = "status"),
                @Index(name = "idx_billing_due_date",   columnList = "due_date")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name        = "uq_billing_household_month_year",
                        columnNames = {"household_id", "billing_month", "billing_year"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"household", "tariffPlan"})
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class BillingCycle extends BaseEntity {

    // -------------------------------------------------------------------------
    // Relationships
    // -------------------------------------------------------------------------

    /**
     * The household this billing cycle belongs to.
     * LAZY-loaded; NOT NULL — every cycle must be tied to a household.
     */
    @NotNull(message = "Household is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name       = "household_id",
            nullable   = false,
            foreignKey = @ForeignKey(name = "fk_billing_household")
    )
    private Household household;

    /**
     * The tariff plan applied to compute this billing cycle's amount.
     * Recorded at cycle-generation time to preserve historical pricing.
     */
    @NotNull(message = "Tariff plan is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name       = "tariff_plan_id",
            nullable   = false,
            foreignKey = @ForeignKey(name = "fk_billing_tariff_plan")
    )
    private TariffPlan tariffPlan;

    // -------------------------------------------------------------------------
    // Billing period
    // -------------------------------------------------------------------------

    /**
     * Calendar month of this billing cycle (1 = January … 12 = December).
     */
    @EqualsAndHashCode.Include
    @NotNull(message = "Billing month is required")
    @Min(value = 1,  message = "Billing month must be between 1 and 12")
    @Max(value = 12, message = "Billing month must be between 1 and 12")
    @Column(name = "billing_month", nullable = false)
    private Integer billingMonth;

    /**
     * Calendar year of this billing cycle (e.g., 2026).
     */
    @EqualsAndHashCode.Include
    @NotNull(message = "Billing year is required")
    @Min(value = 2020, message = "Billing year must be 2020 or later")
    @Column(name = "billing_year", nullable = false)
    private Integer billingYear;

    // -------------------------------------------------------------------------
    // Computed billing amounts
    // -------------------------------------------------------------------------

    /**
     * Total water units consumed by the household during this billing period.
     * Aggregated from {@link WaterUsage} records by the billing service.
     */
    @NotNull(message = "Total units consumed is required")
    @DecimalMin(value = "0.00", message = "Total units consumed cannot be negative")
    @Column(name = "total_units_consumed", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalUnitsConsumed = BigDecimal.ZERO;

    /**
     * Total amount due for this billing cycle.
     * Computed as: max(totalUnitsConsumed, minUnits) × ratePerUnit + fixedCharge.
     */
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.00", message = "Total amount cannot be negative")
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // -------------------------------------------------------------------------
    // Status and dates
    // -------------------------------------------------------------------------

    /**
     * Payment status of this billing cycle.
     * Defaults to {@link BillingStatus#PENDING} on creation.
     */
    @NotNull(message = "Billing status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BillingStatus status = BillingStatus.PENDING;

    /**
     * Date by which payment must be received to avoid OVERDUE status.
     * Typically set to the last day of the following month.
     */
    @NotNull(message = "Due date is required")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * Date on which payment was received and confirmed.
     * {@code null} until the billing cycle is marked as {@link BillingStatus#PAID}.
     */
    @Column(name = "paid_date")
    private LocalDate paidDate;

    // -------------------------------------------------------------------------
    // Business logic helpers
    // -------------------------------------------------------------------------

    /**
     * Marks this billing cycle as paid on the given payment date.
     *
     * @param paymentDate the date the payment was received
     */
    public void markAsPaid(LocalDate paymentDate) {
        this.status   = BillingStatus.PAID;
        this.paidDate = paymentDate;
    }

    /**
     * Marks this billing cycle as overdue.
     * Called by a scheduled job that evaluates pending cycles past their due date.
     */
    public void markAsOverdue() {
        if (BillingStatus.PENDING.equals(this.status)) {
            this.status = BillingStatus.OVERDUE;
        }
    }

    /**
     * Returns {@code true} if this billing cycle has been paid.
     */
    public boolean isPaid() {
        return BillingStatus.PAID.equals(this.status);
    }

    /**
     * Returns {@code true} if this billing cycle is overdue.
     */
    public boolean isOverdue() {
        return BillingStatus.OVERDUE.equals(this.status);
    }
}
