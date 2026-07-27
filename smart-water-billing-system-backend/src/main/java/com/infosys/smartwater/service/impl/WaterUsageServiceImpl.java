package com.infosys.smartwater.service.impl;

import com.infosys.smartwater.dto.request.WaterUsageRequest;
import com.infosys.smartwater.dto.response.CsvImportSummaryResponse;
import com.infosys.smartwater.dto.response.CsvImportSummaryResponse.CsvRowError;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.dto.response.WaterUsageResponse;
import com.infosys.smartwater.entity.Household;
import com.infosys.smartwater.entity.WaterUsage;
import com.infosys.smartwater.entity.enums.ReadingType;
import com.infosys.smartwater.exception.CsvProcessingException;
import com.infosys.smartwater.exception.DuplicateResourceException;
import com.infosys.smartwater.exception.InvalidOperationException;
import com.infosys.smartwater.exception.ResourceNotFoundException;
import com.infosys.smartwater.mapper.WaterUsageMapper;
import com.infosys.smartwater.repository.HouseholdRepository;
import com.infosys.smartwater.repository.WaterUsageRepository;
import com.infosys.smartwater.service.WaterUsageService;
import com.infosys.smartwater.utils.PageableUtils;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link WaterUsageService}.
 *
 * <p>Handles manual meter reading creation/update, date-range queries,
 * and CSV bulk import with row-level error tracking.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class WaterUsageServiceImpl implements WaterUsageService {

    private final WaterUsageRepository waterUsageRepository;
    private final HouseholdRepository  householdRepository;
    private final WaterUsageMapper     waterUsageMapper;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Override
    public WaterUsageResponse createWaterUsage(WaterUsageRequest request) {
        log.info("Recording water usage for household id={} on {}", request.getHouseholdId(), request.getReadingDate());

        Household household = findHousehold(request.getHouseholdId());

        // Duplicate reading check
        if (waterUsageRepository.existsByHouseholdIdAndReadingDate(household.getId(), request.getReadingDate())) {
            throw new DuplicateResourceException("WaterUsage",
                    "householdId + readingDate",
                    household.getHouseholdNumber() + " on " + request.getReadingDate());
        }

        // Auto-populate previousReading from latest reading if not explicitly provided
        BigDecimal previousReading = request.getPreviousReading();
        if (previousReading.compareTo(BigDecimal.ZERO) == 0) {
            previousReading = waterUsageRepository.findLatestByHouseholdId(household.getId())
                    .filter(latest -> latest.getReadingDate().isBefore(request.getReadingDate()))
                    .map(WaterUsage::getMeterReading)
                    .orElse(BigDecimal.ZERO);
        }

        // Cross-field validation
        if (request.getMeterReading().compareTo(previousReading) < 0) {
            throw new InvalidOperationException(
                    String.format("Meter reading [%s] cannot be less than previous reading [%s]",
                            request.getMeterReading(), previousReading));
        }

        WaterUsage waterUsage = WaterUsage.builder()
                .household(household)
                .readingDate(request.getReadingDate())
                .meterReading(request.getMeterReading())
                .previousReading(previousReading)
                .readingType(request.getReadingType() != null ? request.getReadingType() : ReadingType.MANUAL)
                .notes(request.getNotes())
                .build();
        waterUsage.computeUnitsConsumed();

        WaterUsage saved = waterUsageRepository.save(waterUsage);
        log.info("Water usage recorded — id={}, units={}", saved.getId(), saved.getUnitsConsumed());
        return waterUsageMapper.toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public WaterUsageResponse getWaterUsageById(UUID id) {
        return waterUsageMapper.toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public WaterUsageResponse getDailyReading(UUID householdId, LocalDate date) {
        return waterUsageRepository.findByHouseholdIdAndReadingDate(householdId, date)
                .map(waterUsageMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "WaterUsage", "householdId + readingDate", householdId + " on " + date));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<WaterUsageResponse> getWaterUsageByHousehold(UUID householdId, int page, int size) {
        verifyHouseholdExists(householdId);
        Pageable pageable = PageableUtils.createPageable(page, size,
                Sort.by("readingDate").descending());
        return PagedResponse.from(
                waterUsageRepository.findByHouseholdId(householdId, pageable)
                        .map(waterUsageMapper::toResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<WaterUsageResponse> getMonthlyReadings(
            UUID householdId, int month, int year, int page, int size) {
        verifyHouseholdExists(householdId);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate   = startDate.withDayOfMonth(startDate.lengthOfMonth());
        Pageable  pageable  = PageableUtils.createPageable(page, size,
                Sort.by("readingDate").ascending());
        return PagedResponse.from(
                waterUsageRepository.findByHouseholdIdAndReadingDateBetween(
                                householdId, startDate, endDate, pageable)
                        .map(waterUsageMapper::toResponse)
        );
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Override
    public WaterUsageResponse updateWaterUsage(UUID id, WaterUsageRequest request) {
        log.info("Updating water usage id={}", id);
        WaterUsage waterUsage = findById(id);

        Household household = waterUsage.getHousehold();

        // If household or date changed, check for duplicate
        boolean householdChanged = !household.getId().equals(request.getHouseholdId());
        boolean dateChanged      = !waterUsage.getReadingDate().equals(request.getReadingDate());

        if (householdChanged) {
            household = findHousehold(request.getHouseholdId());
            waterUsage.setHousehold(household);
        }

        if (householdChanged || dateChanged) {
            if (waterUsageRepository.existsByHouseholdIdAndReadingDate(
                    request.getHouseholdId(), request.getReadingDate())) {
                throw new DuplicateResourceException("WaterUsage",
                        "householdId + readingDate",
                        request.getHouseholdId() + " on " + request.getReadingDate());
            }
        }

        // Cross-field validation
        BigDecimal previousReading = request.getPreviousReading();
        if (request.getMeterReading().compareTo(previousReading) < 0) {
            throw new InvalidOperationException(
                    String.format("Meter reading [%s] cannot be less than previous reading [%s]",
                            request.getMeterReading(), previousReading));
        }

        waterUsage.setReadingDate(request.getReadingDate());
        waterUsage.setMeterReading(request.getMeterReading());
        waterUsage.setPreviousReading(previousReading);
        waterUsage.setReadingType(request.getReadingType() != null ? request.getReadingType() : ReadingType.MANUAL);
        waterUsage.setNotes(request.getNotes());
        waterUsage.computeUnitsConsumed();

        WaterUsage saved = waterUsageRepository.save(waterUsage);
        log.info("Water usage id={} updated — units={}", saved.getId(), saved.getUnitsConsumed());
        return waterUsageMapper.toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Override
    public void deleteWaterUsage(UUID id) {
        WaterUsage waterUsage = findById(id);
        waterUsageRepository.delete(waterUsage);
        log.info("Water usage id={} deleted.", id);
    }

    // -------------------------------------------------------------------------
    // CSV Import
    // -------------------------------------------------------------------------

    /**
     * CSV columns (with header row):
     * <pre>
     * household_number | reading_date | meter_reading | previous_reading (opt) | notes (opt)
     * </pre>
     */
    @Override
    public CsvImportSummaryResponse importFromCsv(MultipartFile file) {
        log.info("Starting CSV import — file='{}', size={} bytes",
                file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            throw new CsvProcessingException("The uploaded CSV file is empty.");
        }

        int totalRows = 0, successCount = 0, skippedCount = 0, failedCount = 0;
        List<CsvRowError>  errors  = new ArrayList<>();
        List<WaterUsage>   toSave  = new ArrayList<>();

        try (InputStreamReader isr    = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVReader         reader = new CSVReaderBuilder(isr).withSkipLines(1).build()) {

            String[] line;
            int rowNumber = 1; // header is row 0

            while ((line = reader.readNext()) != null) {
                rowNumber++;
                totalRows++;

                String householdNumberRaw = null;
                String readingDateRaw     = null;

                try {
                    if (line.length < 3) {
                        throw new IllegalArgumentException(
                                "Row has too few columns — expected at least 3 " +
                                "(household_number, reading_date, meter_reading).");
                    }

                    householdNumberRaw = line[0].trim();
                    readingDateRaw     = line[1].trim();
                    String meterReadingRaw    = line[2].trim();
                    String previousReadingRaw = line.length > 3 ? line[3].trim() : "0";
                    String notes              = line.length > 4 ? line[4].trim() : null;

                    // Resolve household (copy to effectively-final for lambda capture)
                    final String householdNumberFinal = householdNumberRaw;
                    Household household = householdRepository.findByHouseholdNumber(householdNumberFinal)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Household", "householdNumber", householdNumberFinal));

                    // Parse date
                    LocalDate readingDate = LocalDate.parse(readingDateRaw);

                    // Duplicate check → skip (not fail)
                    if (waterUsageRepository.existsByHouseholdIdAndReadingDate(household.getId(), readingDate)) {
                        skippedCount++;
                        errors.add(CsvRowError.builder()
                                .row(rowNumber)
                                .householdNumber(householdNumberRaw)
                                .readingDate(readingDateRaw)
                                .reason("Duplicate: a reading already exists for this household on " + readingDate)
                                .build());
                        continue;
                    }

                    // Parse readings
                    BigDecimal meterReading    = new BigDecimal(meterReadingRaw);
                    BigDecimal previousReading = previousReadingRaw.isBlank()
                            ? BigDecimal.ZERO : new BigDecimal(previousReadingRaw);

                    if (meterReading.compareTo(BigDecimal.ZERO) < 0 || previousReading.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("Meter readings cannot be negative.");
                    }
                    if (meterReading.compareTo(previousReading) < 0) {
                        throw new IllegalArgumentException(
                                "Meter reading [" + meterReading + "] cannot be less than previous reading [" + previousReading + "].");
                    }

                    WaterUsage waterUsage = WaterUsage.builder()
                            .household(household)
                            .readingDate(readingDate)
                            .meterReading(meterReading)
                            .previousReading(previousReading)
                            .readingType(ReadingType.CSV_IMPORT)
                            .notes(notes)
                            .build();
                    waterUsage.computeUnitsConsumed();
                    toSave.add(waterUsage);
                    successCount++;

                } catch (ResourceNotFoundException | IllegalArgumentException | DateTimeParseException e) {
                    failedCount++;
                    errors.add(CsvRowError.builder()
                            .row(rowNumber)
                            .householdNumber(householdNumberRaw)
                            .readingDate(readingDateRaw)
                            .reason(e.getMessage())
                            .build());
                }
            }

        } catch (IOException | CsvException e) {
            throw new CsvProcessingException("Failed to read CSV file: " + e.getMessage(), e);
        }

        // Bulk-save all valid records
        if (!toSave.isEmpty()) {
            waterUsageRepository.saveAll(toSave);
        }

        log.info("CSV import complete — total={}, success={}, skipped={}, failed={}",
                totalRows, successCount, skippedCount, failedCount);

        return CsvImportSummaryResponse.builder()
                .totalRows(totalRows)
                .successCount(successCount)
                .skippedCount(skippedCount)
                .failedCount(failedCount)
                .errors(errors)
                .build();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private WaterUsage findById(UUID id) {
        return waterUsageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WaterUsage", "id", id));
    }

    private Household findHousehold(UUID householdId) {
        return householdRepository.findById(householdId)
                .orElseThrow(() -> new ResourceNotFoundException("Household", "id", householdId));
    }

    private void verifyHouseholdExists(UUID householdId) {
        if (!householdRepository.existsById(householdId)) {
            throw new ResourceNotFoundException("Household", "id", householdId);
        }
    }
}
