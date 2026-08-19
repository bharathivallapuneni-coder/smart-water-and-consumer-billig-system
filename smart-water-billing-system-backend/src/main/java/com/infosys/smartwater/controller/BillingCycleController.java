package com.infosys.smartwater.controller;

import com.infosys.smartwater.dto.request.BillingCycleRequest;
import com.infosys.smartwater.dto.request.UpdateBillingStatusRequest;
import com.infosys.smartwater.dto.response.ApiResponse;
import com.infosys.smartwater.dto.response.BillingCycleResponse;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.service.BillingCycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for Billing Cycle generation and payment management.
 */
@RestController
@RequestMapping({"/api/v1/billing-cycles", "/api/billing-cycles"})
@RequiredArgsConstructor
@Tag(name = "Billing Cycles", description = "Endpoints for monthly bill generation, payment updates, and overdue detection")
public class BillingCycleController {

    private final BillingCycleService billingCycleService;

    @PostMapping({"", "/generate"})
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Generate a monthly billing cycle for a household [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<BillingCycleResponse>> generateBillingCycle(@Valid @RequestBody BillingCycleRequest request) {
        BillingCycleResponse response = billingCycleService.generateBillingCycle(request);
        return new ResponseEntity<>(
                ApiResponse.success("Billing cycle generated successfully", response, HttpStatus.CREATED.value()),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN', 'RESIDENT')")
    @Operation(summary = "Get billing cycle details by ID")
    public ResponseEntity<ApiResponse<BillingCycleResponse>> getBillingCycleById(@PathVariable UUID id) {
        BillingCycleResponse response = billingCycleService.getBillingCycleById(id);
        return ResponseEntity.ok(ApiResponse.success("Billing cycle retrieved successfully", response, HttpStatus.OK.value()));
    }

    @GetMapping("/household/{householdId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN', 'RESIDENT')")
    @Operation(summary = "Get billing cycles for a household")
    public ResponseEntity<ApiResponse<PagedResponse<BillingCycleResponse>>> getBillingCyclesByHousehold(
            @PathVariable UUID householdId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<BillingCycleResponse> response = billingCycleService.getBillingCyclesByHousehold(householdId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Billing cycles retrieved successfully", response, HttpStatus.OK.value()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Get paginated list of all billing cycles [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<PagedResponse<BillingCycleResponse>>> getAllBillingCycles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        PagedResponse<BillingCycleResponse> response = billingCycleService.getAllBillingCycles(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Billing cycles retrieved successfully", response, HttpStatus.OK.value()));
    }

    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Get billing cycles by month and year [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<PagedResponse<BillingCycleResponse>>> getBillingCyclesByMonth(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<BillingCycleResponse> response = billingCycleService.getBillingCyclesByMonth(month, year, page, size);
        return ResponseEntity.ok(ApiResponse.success("Monthly billing cycles retrieved successfully", response, HttpStatus.OK.value()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Update billing cycle payment status [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<BillingCycleResponse>> updateBillingStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBillingStatusRequest request
    ) {
        BillingCycleResponse response = billingCycleService.updateBillingStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Billing status updated successfully", response, HttpStatus.OK.value()));
    }

    @PostMapping("/detect-overdue")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Trigger overdue billing cycle detection job [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<Integer>> detectAndMarkOverdue() {
        int updatedCount = billingCycleService.detectAndMarkOverdue();
        return ResponseEntity.ok(ApiResponse.success("Overdue check completed. " + updatedCount + " cycle(s) marked overdue.", updatedCount, HttpStatus.OK.value()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Delete a billing cycle [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<Void>> deleteBillingCycle(@PathVariable UUID id) {
        billingCycleService.deleteBillingCycle(id);
        return ResponseEntity.ok(ApiResponse.success("Billing cycle deleted successfully", HttpStatus.OK.value()));
    }
}
