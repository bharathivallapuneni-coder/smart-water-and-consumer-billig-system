package com.infosys.smartwater.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents apartment-level bulk water procurement record.
 */
@Entity
@Table(
        name = "bulk_water_purchases",
        indexes = {
                @Index(name = "idx_bulk_water_apartment", columnList = "apartment_id"),
                @Index(name = "idx_bulk_water_cycle", columnList = "billing_cycle_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class BulkWaterPurchase extends BaseEntity {

    @JsonIgnore
    @NotNull(message = "Apartment is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_cycle_id")
    private BillingCycle billingCycle;

    /**
     * Source type e.g. TANKER, MUNICIPAL, OTHER
     */
    @NotBlank(message = "Source type is required")
    @Column(name = "source_type", nullable = false, length = 50)
    private String sourceType;

    @Column(name = "supplier_name", length = 100)
    private String supplierName;

    @NotNull(message = "Purchase date is required")
    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @NotNull(message = "Purchased volume is required")
    @Column(name = "purchased_volume_kl", nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasedVolumeKl;

    @NotNull(message = "Total cost is required")
    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    @NotNull(message = "Unit cost is required")
    @Column(name = "unit_cost_per_kl", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitCostPerKl;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
