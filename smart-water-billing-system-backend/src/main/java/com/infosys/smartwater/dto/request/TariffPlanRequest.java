package com.infosys.smartwater.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating or updating a {@code TariffPlan}.
 *
 * <p>Used by:
 * <ul>
 *   <li>{@code POST /api/tariff-plans}       — create a new tariff plan</li>
 *   <li>{@code PUT  /api/tariff-plans/{id}}  — update an existing plan</li>
 * </ul>
 *
 * <p><b>Date range validation:</b> {@code effectiveTo > effectiveFrom}
 * is enforced in the service layer along with overlap detection
 * against existing active plans.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating or updating a water tariff plan")
public class TariffPlanRequest {

    @Schema(description = "Unique human-readable name for the plan",
            example = "Standard 2026",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Plan name is required")
    @Size(max = 100, message = "Plan name cannot exceed 100 characters")
    @JsonAlias({"name", "plan_name"})
    private String planName;

    @Schema(description = "Cost per unit (kL/m³) of water consumed — must be > 0",
            example = "12.5000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Rate per unit is required")
    @DecimalMin(value = "0.0001", inclusive = true,
            message = "Rate per unit must be greater than 0")
    @Digits(integer = 6, fraction = 4,
            message = "Rate per unit must have at most 6 integer and 4 decimal digits")
    @JsonAlias({"baseRate", "rate_per_unit", "unitRate", "rate", "slab1Rate"})
    private BigDecimal ratePerUnit;

    @Schema(description = "Fixed monthly service/connection charge (₹) — defaults to 0 if omitted",
            example = "50.00")
    @DecimalMin(value = "0.00", message = "Fixed charge cannot be negative")
    @Digits(integer = 8, fraction = 2,
            message = "Fixed charge must have at most 8 integer and 2 decimal digits")
    @JsonAlias({"fixed_charge", "fixedRate", "fixedAmount"})
    @Builder.Default
    private BigDecimal fixedCharge = BigDecimal.ZERO;

    @Schema(description = "Minimum chargeable units per billing cycle — defaults to 0 if omitted",
            example = "5.00")
    @DecimalMin(value = "0.00", message = "Minimum units cannot be negative")
    @JsonAlias({"min_units", "minimumUnits"})
    @Builder.Default
    private BigDecimal minUnits = BigDecimal.ZERO;

    @Schema(description = "Date from which this plan is effective (inclusive)",
            example = "2026-01-01",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Effective from date is required")
    @JsonAlias({"effective_from", "startDate", "validFrom"})
    private LocalDate effectiveFrom;

    @Schema(description = "Date after which this plan expires — omit for an open-ended plan",
            example = "2026-12-31",
            nullable = true)
    @JsonAlias({"effective_to", "endDate", "validTo"})
    private LocalDate effectiveTo;

    @Schema(description = "Whether to set this plan as active immediately — defaults to true",
            example = "true")
    @JsonAlias({"is_active", "active"})
    @Builder.Default
    private Boolean isActive = true;
}
