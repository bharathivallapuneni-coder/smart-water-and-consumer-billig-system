package com.infosys.smartwater.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents an itemized bill invoice generated for a household upon cycle finalization.
 */
@Entity
@Table(
        name = "household_invoices",
        indexes = {
                @Index(name = "idx_invoices_cycle", columnList = "billing_cycle_id"),
                @Index(name = "idx_invoices_household", columnList = "household_id"),
                @Index(name = "idx_invoices_resident", columnList = "resident_id"),
                @Index(name = "idx_invoices_apartment", columnList = "apartment_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class HouseholdInvoice extends BaseEntity {

    @NotBlank(message = "Invoice number is required")
    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @JsonIgnore
    @NotNull(message = "Billing cycle is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "billing_cycle_id", nullable = false)
    private BillingCycle billingCycle;

    @JsonIgnore
    @NotNull(message = "Household is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id")
    private User resident;

    @JsonIgnore
    @NotNull(message = "Apartment is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @NotBlank(message = "Billing period is required")
    @Column(name = "billing_period", nullable = false, length = 20)
    private String billingPeriod; // e.g. "August 2026"

    @Column(name = "metered_consumption_kl", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal meteredConsumptionKl = BigDecimal.ZERO;

    @Column(name = "flat_area_sqft", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal flatAreaSqft = BigDecimal.ZERO;

    @Column(name = "is_metered", nullable = false)
    @Builder.Default
    private Boolean isMetered = true;

    @Column(name = "base_tiered_charge", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal baseTieredCharge = BigDecimal.ZERO;

    @Column(name = "allocated_water_procurement_charge", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal allocatedWaterProcurementCharge = BigDecimal.ZERO;

    @Column(name = "shared_area_charge", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal sharedAreaCharge = BigDecimal.ZERO;

    @Column(name = "adjustments", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal adjustments = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @NotBlank(message = "Status is required")
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, PAID, OVERDUE

    @Column(name = "generated_at", nullable = false)
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();

    @NotNull(message = "Due date is required")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payment_id", length = 100)
    private String paymentId;

    @Column(name = "breakdown_json", columnDefinition = "TEXT")
    private String breakdownJson;
}
