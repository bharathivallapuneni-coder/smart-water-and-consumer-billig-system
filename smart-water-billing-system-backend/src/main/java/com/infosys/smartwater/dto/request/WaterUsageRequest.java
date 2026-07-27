package com.infosys.smartwater.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.infosys.smartwater.entity.enums.ReadingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating or updating a {@code WaterUsage} meter reading.
 *
 * <p>Used by:
 * <ul>
 *   <li>{@code POST /api/water-usages}       — record a new meter reading</li>
 *   <li>{@code PUT  /api/water-usages/{id}}  — update an existing reading (ADMIN only)</li>
 * </ul>
 *
 * <p><b>Validation note:</b> Cross-field validation
 * ({@code meterReading >= previousReading}) is enforced in the service layer
 * since Bean Validation cannot cross-reference two fields on the same DTO
 * without a custom constraint.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for recording or updating a water meter reading")
public class WaterUsageRequest {

    @Schema(description = "UUID of the household whose meter is being read",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Household ID is required")
    @JsonAlias({"household_id", "household"})
    private UUID householdId;

    @Schema(description = "Date on which the meter reading was taken",
            example = "2026-07-23",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Reading date is required")
    @PastOrPresent(message = "Reading date cannot be in the future")
    @JsonAlias({"reading_date", "date"})
    private LocalDate readingDate;

    @Schema(description = "Cumulative meter display value at the reading date (kL/m³)",
            example = "1523.75",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Meter reading is required")
    @DecimalMin(value = "0.00", message = "Meter reading cannot be negative")
    @Digits(integer = 10, fraction = 2,
            message = "Meter reading must have at most 10 integer and 2 decimal digits")
    @JsonAlias({"consumptionLiters", "reading", "meter_reading", "currentReading", "current_reading", "consumption"})
    private BigDecimal meterReading;

    @Schema(description = "Meter reading from the immediately preceding entry — defaults to 0 for the first reading",
            example = "1498.25")
    @DecimalMin(value = "0.00", message = "Previous reading cannot be negative")
    @Digits(integer = 10, fraction = 2,
            message = "Previous reading must have at most 10 integer and 2 decimal digits")
    @JsonAlias({"previous_reading", "lastReading"})
    @Builder.Default
    private BigDecimal previousReading = BigDecimal.ZERO;

    @Schema(description = "How the reading is being entered — defaults to MANUAL",
            example = "MANUAL")
    @JsonAlias({"reading_type", "type"})
    @Builder.Default
    private ReadingType readingType = ReadingType.MANUAL;

    @Schema(description = "Optional notes about this reading",
            example = "Meter replaced on this date — reading reset to 0",
            nullable = true)
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
