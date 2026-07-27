package com.infosys.smartwater.controller;

import com.infosys.smartwater.dto.request.TariffPlanRequest;
import com.infosys.smartwater.dto.response.ApiResponse;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.dto.response.TariffPlanResponse;
import com.infosys.smartwater.service.TariffPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Tariff Plan configuration.
 */
@RestController
@RequestMapping({"/api/v1/tariff-plans", "/api/tariff-plans"})
@RequiredArgsConstructor
@Tag(name = "Tariff Plans", description = "Endpoints for managing water pricing structures and tariff plans")
public class TariffPlanController {

    private final TariffPlanService tariffPlanService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Create a new tariff plan [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<TariffPlanResponse>> createTariffPlan(@Valid @RequestBody TariffPlanRequest request) {
        TariffPlanResponse response = tariffPlanService.createTariffPlan(request);
        return new ResponseEntity<>(
                ApiResponse.success("Tariff plan created successfully", response, HttpStatus.CREATED.value()),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN', 'RESIDENT')")
    @Operation(summary = "Get tariff plan details by ID")
    public ResponseEntity<ApiResponse<TariffPlanResponse>> getTariffPlanById(@PathVariable UUID id) {
        TariffPlanResponse response = tariffPlanService.getTariffPlanById(id);
        return ResponseEntity.ok(ApiResponse.success("Tariff plan retrieved successfully", response, HttpStatus.OK.value()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN', 'RESIDENT')")
    @Operation(summary = "Get paginated list of all tariff plans")
    public ResponseEntity<ApiResponse<PagedResponse<TariffPlanResponse>>> getAllTariffPlans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "effectiveFrom") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        PagedResponse<TariffPlanResponse> response = tariffPlanService.getAllTariffPlans(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Tariff plans retrieved successfully", response, HttpStatus.OK.value()));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN', 'RESIDENT')")
    @Operation(summary = "Get all currently active tariff plans")
    public ResponseEntity<ApiResponse<List<TariffPlanResponse>>> getAllActiveTariffPlans() {
        List<TariffPlanResponse> response = tariffPlanService.getAllActiveTariffPlans();
        return ResponseEntity.ok(ApiResponse.success("Active tariff plans retrieved successfully", response, HttpStatus.OK.value()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Update a tariff plan [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<TariffPlanResponse>> updateTariffPlan(
            @PathVariable UUID id,
            @Valid @RequestBody TariffPlanRequest request
    ) {
        TariffPlanResponse response = tariffPlanService.updateTariffPlan(id, request);
        return ResponseEntity.ok(ApiResponse.success("Tariff plan updated successfully", response, HttpStatus.OK.value()));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Deactivate a tariff plan [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<TariffPlanResponse>> deactivateTariffPlan(@PathVariable UUID id) {
        TariffPlanResponse response = tariffPlanService.deactivateTariffPlan(id);
        return ResponseEntity.ok(ApiResponse.success("Tariff plan deactivated successfully", response, HttpStatus.OK.value()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Delete a tariff plan [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<Void>> deleteTariffPlan(@PathVariable UUID id) {
        tariffPlanService.deleteTariffPlan(id);
        return ResponseEntity.ok(ApiResponse.success("Tariff plan deleted successfully", HttpStatus.OK.value()));
    }
}
