package com.infosys.smartwater.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for {@code TariffPlan} resources.
 *
 * <p>Exposes all tariff plan fields needed for display and billing calculation.
 * The billing service uses {@code ratePerUnit}, {@code fixedCharge}, and
 * {@code minUnits} to compute bill amounts.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Water tariff pricing plan returned by the API")
public class TariffPlanResponse {

    @Schema(description = "Unique identifier of the tariff plan",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Human-readable unique name of the plan", example = "Standard 2026")
    private String planName;

    @Schema(description = "Cost per unit (kL/m³) of water consumed (₹)",
            example = "12.5000")
    private BigDecimal ratePerUnit;

    @Schema(description = "Fixed monthly service/connection charge (₹) regardless of usage",
            example = "50.00")
    private BigDecimal fixedCharge;

    @Schema(description = "Minimum chargeable units per billing cycle",
            example = "5.00")
    private BigDecimal minUnits;

    @Schema(description = "Date from which this tariff plan is effective (inclusive)",
            example = "2026-01-01")
    private LocalDate effectiveFrom;

    @Schema(description = "Date after which this tariff plan expires — null means open-ended",
            nullable = true, example = "2026-12-31")
    private LocalDate effectiveTo;

    @Schema(description = "Whether this tariff plan is currently active", example = "true")
    private Boolean isActive;

    @Schema(description = "Timestamp of plan creation", example = "2026-01-01T00:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of last update", example = "2026-07-23T21:00:00")
    private LocalDateTime updatedAt;
}
