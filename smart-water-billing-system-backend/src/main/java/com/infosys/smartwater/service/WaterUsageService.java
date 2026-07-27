package com.infosys.smartwater.service;

import com.infosys.smartwater.dto.request.WaterUsageRequest;
import com.infosys.smartwater.dto.response.CsvImportSummaryResponse;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.dto.response.WaterUsageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service contract for water meter reading management and CSV bulk import.
 */
public interface WaterUsageService {

    WaterUsageResponse createWaterUsage(WaterUsageRequest request);

    WaterUsageResponse getWaterUsageById(UUID id);

    /**
     * Returns the single reading for a household on a specific date.
     *
     * @throws com.infosys.smartwater.exception.ResourceNotFoundException if no reading exists for that date
     */
    WaterUsageResponse getDailyReading(UUID householdId, LocalDate date);

    PagedResponse<WaterUsageResponse> getWaterUsageByHousehold(UUID householdId, int page, int size);

    /**
     * Returns all readings for a household within a specific billing month and year.
     *
     * @param householdId the household UUID
     * @param month       billing month (1–12)
     * @param year        billing year
     * @param page        zero-based page number
     * @param size        page size
     */
    PagedResponse<WaterUsageResponse> getMonthlyReadings(UUID householdId, int month, int year, int page, int size);

    WaterUsageResponse updateWaterUsage(UUID id, WaterUsageRequest request);

    void deleteWaterUsage(UUID id);

    /**
     * Bulk-imports water usage records from a CSV file.
     *
     * <p>Expected CSV format (with header row):
     * <pre>
     * household_number,reading_date,meter_reading,previous_reading,notes
     * APT-001-F02-U04,2026-07-23,1523.75,1498.25,Optional note
     * </pre>
     *
     * <p>Duplicate records (same household + date) are skipped, not failed.
     * Rows with validation errors are recorded in the summary response.
     *
     * @param file the uploaded CSV file
     * @return an import summary with success/skipped/failed counts and row-level errors
     * @throws com.infosys.smartwater.exception.CsvProcessingException if the file is empty or unreadable
     */
    CsvImportSummaryResponse importFromCsv(MultipartFile file);
}
