package com.infosys.smartwater.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a tiered pricing structure configuration per apartment building.
 */
@Entity
@Table(
        name = "tariff_tiers",
        indexes = {
                @Index(name = "idx_tariff_tiers_apartment", columnList = "apartment_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class TariffTier extends BaseEntity {

    @JsonIgnore
    @NotNull(message = "Apartment is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @NotBlank(message = "Tier name is required")
    @Column(name = "tier_name", nullable = false, length = 50)
    private String tierName;

    @NotNull(message = "Minimum kL is required")
    @Min(value = 0)
    @Column(name = "min_kl", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minKl = BigDecimal.ZERO;

    /**
     * Null indicates no upper bound (e.g. above 10 kL).
     */
    @Column(name = "max_kl", precision = 10, scale = 2)
    private BigDecimal maxKl;

    @NotNull(message = "Rate per kL is required")
    @Column(name = "rate_per_kl", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal ratePerKl = BigDecimal.ZERO;

    @Column(name = "fixed_charge", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal fixedCharge = BigDecimal.ZERO;
}
