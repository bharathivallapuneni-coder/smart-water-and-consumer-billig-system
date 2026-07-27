package com.infosys.smartwater.controller;

import com.infosys.smartwater.dto.request.HouseholdRequest;
import com.infosys.smartwater.dto.response.ApiResponse;
import com.infosys.smartwater.dto.response.HouseholdResponse;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.service.HouseholdService;
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
 * REST Controller for Household management.
 */
@RestController
@RequestMapping({"/api/v1/households", "/api/households"})
@RequiredArgsConstructor
@Tag(name = "Households", description = "Endpoints for consumer household management")
public class HouseholdController {

    private final HouseholdService householdService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Create a new household [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<HouseholdResponse>> createHousehold(@Valid @RequestBody HouseholdRequest request) {
        HouseholdResponse response = householdService.createHousehold(request);
        return new ResponseEntity<>(
                ApiResponse.success("Household created successfully", response, HttpStatus.CREATED.value()),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN', 'RESIDENT')")
    @Operation(summary = "Get household details by ID")
    public ResponseEntity<ApiResponse<HouseholdResponse>> getHouseholdById(@PathVariable UUID id) {
        HouseholdResponse response = householdService.getHouseholdById(id);
        return ResponseEntity.ok(ApiResponse.success("Household retrieved successfully", response, HttpStatus.OK.value()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Get paginated list of all households [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<PagedResponse<HouseholdResponse>>> getAllHouseholds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "householdNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        PagedResponse<HouseholdResponse> response = householdService.getAllHouseholds(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Households retrieved successfully", response, HttpStatus.OK.value()));
    }

    @GetMapping("/apartment/{apartmentId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Get households under an apartment [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<PagedResponse<HouseholdResponse>>> getHouseholdsByApartment(
            @PathVariable UUID apartmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "householdNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        PagedResponse<HouseholdResponse> response = householdService.getHouseholdsByApartment(apartmentId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Households retrieved successfully", response, HttpStatus.OK.value()));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Search households by keyword [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<PagedResponse<HouseholdResponse>>> searchHouseholds(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<HouseholdResponse> response = householdService.searchHouseholds(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", response, HttpStatus.OK.value()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Update a household [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<HouseholdResponse>> updateHousehold(
            @PathVariable UUID id,
            @Valid @RequestBody HouseholdRequest request
    ) {
        HouseholdResponse response = householdService.updateHousehold(id, request);
        return ResponseEntity.ok(ApiResponse.success("Household updated successfully", response, HttpStatus.OK.value()));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Activate a household [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<HouseholdResponse>> activateHousehold(@PathVariable UUID id) {
        HouseholdResponse response = householdService.activateHousehold(id);
        return ResponseEntity.ok(ApiResponse.success("Household activated successfully", response, HttpStatus.OK.value()));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Deactivate a household [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<HouseholdResponse>> deactivateHousehold(@PathVariable UUID id) {
        HouseholdResponse response = householdService.deactivateHousehold(id);
        return ResponseEntity.ok(ApiResponse.success("Household deactivated successfully", response, HttpStatus.OK.value()));
    }

    @PostMapping("/{householdId}/assign-user/{userId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Assign a user account to a household [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<HouseholdResponse>> assignUser(
            @PathVariable UUID householdId,
            @PathVariable UUID userId
    ) {
        HouseholdResponse response = householdService.assignUser(householdId, userId);
        return ResponseEntity.ok(ApiResponse.success("User assigned to household successfully", response, HttpStatus.OK.value()));
    }

    @PostMapping("/{householdId}/create-resident")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Create and assign a RESIDENT user account to a household [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<HouseholdResponse>> createAndAssignResident(
            @PathVariable UUID householdId,
            @Valid @RequestBody com.infosys.smartwater.dto.request.UserRegistrationRequest residentRequest
    ) {
        HouseholdResponse response = householdService.createAndAssignResident(householdId, residentRequest);
        return new ResponseEntity<>(
                ApiResponse.success("Resident account created and linked to household successfully", response, HttpStatus.CREATED.value()),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{householdId}/remove-user")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Remove user account from a household [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<HouseholdResponse>> removeUser(@PathVariable UUID householdId) {
        HouseholdResponse response = householdService.removeUser(householdId);
        return ResponseEntity.ok(ApiResponse.success("User removed from household successfully", response, HttpStatus.OK.value()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a household [ADMIN]")
    public ResponseEntity<ApiResponse<Void>> deleteHousehold(@PathVariable UUID id) {
        householdService.deleteHousehold(id);
        return ResponseEntity.ok(ApiResponse.success("Household deleted successfully", HttpStatus.OK.value()));
    }
}
