package com.infosys.smartwater.dto.response;

import com.infosys.smartwater.entity.enums.ReadingType;
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
 * Response DTO for {@code WaterUsage} resources.
 *
 * <p>Includes flattened household information (ID + household number)
 * to allow clients to display readings without a secondary API call.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Water usage meter reading returned by the API")
public class WaterUsageResponse {

    @Schema(description = "Unique identifier of the water usage record",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    // Flattened household reference
    @Schema(description = "UUID of the household this reading belongs to",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID householdId;

    @Schema(description = "Household number for display purposes", example = "APT-001-F02-U04")
    private String householdNumber;

    @Schema(description = "Date on which the meter reading was taken", example = "2026-07-23")
    private LocalDate readingDate;

    @Schema(description = "Cumulative meter display value at the reading date (kL/m³)",
            example = "1523.75")
    private BigDecimal meterReading;

    @Schema(description = "Meter value from the previous reading", example = "1498.25")
    private BigDecimal previousReading;

    @Schema(description = "Units of water consumed (meterReading - previousReading)",
            example = "25.50")
    private BigDecimal unitsConsumed;

    @Schema(description = "How the reading was entered into the system",
            example = "MANUAL")
    private ReadingType readingType;

    @Schema(description = "Optional notes about this reading",
            example = "Meter replaced — reading reset to 0", nullable = true)
    private String notes;

    @Schema(description = "Timestamp of record creation", example = "2026-07-23T18:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of last update", example = "2026-07-23T18:00:00")
    private LocalDateTime updatedAt;
}
