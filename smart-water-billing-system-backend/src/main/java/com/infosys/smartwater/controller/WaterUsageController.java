package com.infosys.smartwater.controller;

import com.infosys.smartwater.dto.request.WaterUsageRequest;
import com.infosys.smartwater.dto.response.ApiResponse;
import com.infosys.smartwater.dto.response.CsvImportSummaryResponse;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.dto.response.WaterUsageResponse;
import com.infosys.smartwater.service.WaterUsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST Controller for Water Usage reading management and CSV bulk import.
 */
@RestController
@RequestMapping({"/api/v1/water-usage", "/api/water-usage"})
@RequiredArgsConstructor
@Tag(name = "Water Usage", description = "Endpoints for daily meter readings and bulk CSV import")
public class WaterUsageController {

    private final WaterUsageService waterUsageService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Record a single water usage reading [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<WaterUsageResponse>> createWaterUsage(@Valid @RequestBody WaterUsageRequest request) {
        WaterUsageResponse response = waterUsageService.createWaterUsage(request);
        return new ResponseEntity<>(
                ApiResponse.success("Water usage recorded successfully", response, HttpStatus.CREATED.value()),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN', 'RESIDENT')")
    @Operation(summary = "Get water usage record by ID")
    public ResponseEntity<ApiResponse<WaterUsageResponse>> getWaterUsageById(@PathVariable UUID id) {
        WaterUsageResponse response = waterUsageService.getWaterUsageById(id);
        return ResponseEntity.ok(ApiResponse.success("Water usage record retrieved successfully", response, HttpStatus.OK.value()));
    }

    @GetMapping("/daily")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN', 'RESIDENT')")
    @Operation(summary = "Get daily reading for a household on a specific date")
    public ResponseEntity<ApiResponse<WaterUsageResponse>> getDailyReading(
            @RequestParam UUID householdId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        WaterUsageResponse response = waterUsageService.getDailyReading(householdId, date);
        return ResponseEntity.ok(ApiResponse.success("Daily reading retrieved successfully", response, HttpStatus.OK.value()));
    }

    @GetMapping("/household/{householdId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN', 'RESIDENT')")
    @Operation(summary = "Get paginated water usage records for a household")
    public ResponseEntity<ApiResponse<PagedResponse<WaterUsageResponse>>> getWaterUsageByHousehold(
            @PathVariable UUID householdId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<WaterUsageResponse> response = waterUsageService.getWaterUsageByHousehold(householdId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Water usage records retrieved successfully", response, HttpStatus.OK.value()));
    }

    @GetMapping("/household/{householdId}/monthly")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN', 'RESIDENT')")
    @Operation(summary = "Get water usage records for a household in a specific month/year")
    public ResponseEntity<ApiResponse<PagedResponse<WaterUsageResponse>>> getMonthlyReadings(
            @PathVariable UUID householdId,
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "31") int size
    ) {
        PagedResponse<WaterUsageResponse> response = waterUsageService.getMonthlyReadings(householdId, month, year, page, size);
        return ResponseEntity.ok(ApiResponse.success("Monthly readings retrieved successfully", response, HttpStatus.OK.value()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Update a water usage record [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<WaterUsageResponse>> updateWaterUsage(
            @PathVariable UUID id,
            @Valid @RequestBody WaterUsageRequest request
    ) {
        WaterUsageResponse response = waterUsageService.updateWaterUsage(id, request);
        return ResponseEntity.ok(ApiResponse.success("Water usage record updated successfully", response, HttpStatus.OK.value()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Delete a water usage record [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<Void>> deleteWaterUsage(@PathVariable UUID id) {
        waterUsageService.deleteWaterUsage(id);
        return ResponseEntity.ok(ApiResponse.success("Water usage record deleted successfully", HttpStatus.OK.value()));
    }

    @PostMapping(value = "/import-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Bulk import meter readings from a CSV file [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<CsvImportSummaryResponse>> importFromCsv(@RequestParam("file") MultipartFile file) {
        CsvImportSummaryResponse response = waterUsageService.importFromCsv(file);
        return ResponseEntity.ok(ApiResponse.success("CSV import processed", response, HttpStatus.OK.value()));
    }
}
