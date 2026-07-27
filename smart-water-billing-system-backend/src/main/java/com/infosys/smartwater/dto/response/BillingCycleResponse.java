package com.infosys.smartwater.dto.response;

import com.infosys.smartwater.entity.enums.BillingStatus;
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
 * Response DTO for {@code BillingCycle} resources.
 *
 * <p>Includes flattened summaries of both the associated household
 * and tariff plan to avoid nested object hierarchies in the API response.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Monthly billing cycle record returned by the API")
public class BillingCycleResponse {

    @Schema(description = "Unique identifier of the billing cycle",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    // Flattened household reference
    @Schema(description = "UUID of the billed household",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID householdId;

    @Schema(description = "Household number for display", example = "APT-001-F02-U04")
    private String householdNumber;

    @Schema(description = "Name of the owner of the billed household", example = "Rajesh Kumar")
    private String ownerName;

    // Flattened tariff plan reference
    @Schema(description = "UUID of the tariff plan applied to this billing cycle",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID tariffPlanId;

    @Schema(description = "Name of the applied tariff plan", example = "Standard 2026")
    private String tariffPlanName;

    @Schema(description = "Rate per unit at the time of billing (₹ per kL/m³)",
            example = "12.5000")
    private BigDecimal ratePerUnit;

    // Billing period
    @Schema(description = "Billing month (1 = January ... 12 = December)", example = "7")
    private Integer billingMonth;

    @Schema(description = "Billing year", example = "2026")
    private Integer billingYear;

    // Computed amounts
    @Schema(description = "Total water units consumed by the household in this billing period",
            example = "45.25")
    private BigDecimal totalUnitsConsumed;

    @Schema(description = "Total bill amount due for this period (₹)", example = "565.63")
    private BigDecimal totalAmount;

    // Status and dates
    @Schema(description = "Current payment status", example = "PENDING")
    private BillingStatus status;

    @Schema(description = "Date by which payment must be made", example = "2026-08-31")
    private LocalDate dueDate;

    @Schema(description = "Date on which payment was received — null if unpaid",
            nullable = true, example = "2026-08-15")
    private LocalDate paidDate;

    @Schema(description = "Timestamp of billing cycle generation", example = "2026-08-01T00:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of last update", example = "2026-08-15T14:30:00")
    private LocalDateTime updatedAt;
}
