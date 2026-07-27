package com.infosys.smartwater.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO returned after a CSV water usage import operation (Task 13).
 *
 * <p>Provides a detailed import summary so clients can understand the result
 * of the batch operation without needing to inspect server logs:
 *
 * <pre>
 * {
 *   "totalRows"    : 150,
 *   "successCount" : 143,
 *   "skippedCount" : 5,
 *   "failedCount"  : 2,
 *   "errors"       : [
 *     { "row": 12, "householdNumber": "APT-001-F2-U3", "reason": "Duplicate reading for 2026-07-01" },
 *     { "row": 87, "householdNumber": "APT-002-F3-U1", "reason": "Household not found" }
 *   ]
 * }
 * </pre>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Summary of a CSV water usage bulk import operation")
public class CsvImportSummaryResponse {

    @Schema(description = "Total number of data rows found in the uploaded CSV file (excluding header)",
            example = "150")
    private int totalRows;

    @Schema(description = "Number of rows successfully imported into the database",
            example = "143")
    private int successCount;

    @Schema(description = "Number of rows skipped because they were exact duplicates of existing records",
            example = "5")
    private int skippedCount;

    @Schema(description = "Number of rows that failed validation or processing",
            example = "2")
    private int failedCount;

    @Schema(description = "List of errors for rows that failed — empty if all rows succeeded",
            nullable = true)
    @Builder.Default
    private List<CsvRowError> errors = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Nested error detail record
    // -------------------------------------------------------------------------

    /**
     * Describes a single row-level error encountered during CSV import.
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Details of a single failed or skipped CSV row")
    public static class CsvRowError {

        @Schema(description = "1-based row number in the CSV file (header = row 0)",
                example = "12")
        private int row;

        @Schema(description = "Household number from the CSV row (may be null if unparseable)",
                example = "APT-001-F2-U3")
        private String householdNumber;

        @Schema(description = "Reading date from the CSV row (may be null if unparseable)",
                example = "2026-07-01")
        private String readingDate;

        @Schema(description = "Human-readable reason for the failure or skip",
                example = "Duplicate reading already exists for this household on 2026-07-01")
        private String reason;
    }

    // -------------------------------------------------------------------------
    // Convenience helpers
    // -------------------------------------------------------------------------

    /**
     * Adds a row-level error to the errors list.
     *
     * @param error the error to add
     */
    public void addError(CsvRowError error) {
        this.errors.add(error);
    }

    /**
     * Returns {@code true} if the import had no failures.
     *
     * @return {@code true} when {@code failedCount == 0}
     */
    public boolean isFullySuccessful() {
        return failedCount == 0;
    }
}
