package com.infosys.smartwater.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.infosys.smartwater.entity.enums.BillingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for generating a monthly {@code BillingCycle} for a household.
 *
 * <p>Used by {@code POST /api/billing-cycles}.
 *
 * <p>The service layer computes {@code totalUnitsConsumed} and {@code totalAmount}
 * automatically from water usage records and the tariff plan.
 * These fields are NOT part of this request DTO.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for generating a monthly billing cycle for a household")
public class BillingCycleRequest {

    @Schema(description = "UUID of the household to generate the billing cycle for",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Household ID is required")
    @JsonAlias({"household_id", "household"})
    private UUID householdId;

    @Schema(description = "UUID of the tariff plan to apply for this billing cycle — defaults to active plan if omitted",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            nullable = true)
    @JsonAlias({"tariff_plan_id", "tariffPlan", "planId"})
    private UUID tariffPlanId;

    @Schema(description = "Calendar month for this billing cycle (1 = Jan … 12 = Dec)",
            example = "7",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Billing month is required")
    @Min(value = 1,  message = "Billing month must be between 1 and 12")
    @Max(value = 12, message = "Billing month must be between 1 and 12")
    @JsonAlias({"billing_month", "month"})
    private Integer billingMonth;

    @Schema(description = "Calendar year for this billing cycle",
            example = "2026",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Billing year is required")
    @Min(value = 2020, message = "Billing year must be 2020 or later")
    @JsonAlias({"billing_year", "year"})
    private Integer billingYear;

    @Schema(description = "Payment due date — typically the last day of the following month",
            example = "2026-08-31",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Due date is required")
    @FutureOrPresent(message = "Due date must be today or in the future")
    @JsonAlias({"due_date", "due"})
    private LocalDate dueDate;
}
