package com.infosys.smartwater.controller;

import com.infosys.smartwater.dto.request.ApartmentRequest;
import com.infosys.smartwater.dto.response.ApiResponse;
import com.infosys.smartwater.dto.response.ApartmentResponse;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.service.ApartmentService;
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
 * REST Controller for Apartment management.
 */
@RestController
@RequestMapping({"/api/v1/apartments", "/api/apartments"})
@RequiredArgsConstructor
@Tag(name = "Apartments", description = "Endpoints for building / apartment complex management")
public class ApartmentController {

    private final ApartmentService apartmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Create a new apartment [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<ApartmentResponse>> createApartment(@Valid @RequestBody ApartmentRequest request) {
        ApartmentResponse response = apartmentService.createApartment(request);
        return new ResponseEntity<>(
                ApiResponse.success("Apartment created successfully", response, HttpStatus.CREATED.value()),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN', 'RESIDENT')")
    @Operation(summary = "Get apartment details by ID")
    public ResponseEntity<ApiResponse<ApartmentResponse>> getApartmentById(@PathVariable UUID id) {
        ApartmentResponse response = apartmentService.getApartmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Apartment retrieved successfully", response, HttpStatus.OK.value()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Get paginated list of apartments [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<PagedResponse<ApartmentResponse>>> getAllApartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "apartmentNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        PagedResponse<ApartmentResponse> response = apartmentService.getAllApartments(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Apartments retrieved successfully", response, HttpStatus.OK.value()));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Search apartments by keyword [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<PagedResponse<ApartmentResponse>>> searchApartments(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "apartmentNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        PagedResponse<ApartmentResponse> response = apartmentService.searchApartments(keyword, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", response, HttpStatus.OK.value()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Update an apartment [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<ApartmentResponse>> updateApartment(
            @PathVariable UUID id,
            @Valid @RequestBody ApartmentRequest request
    ) {
        ApartmentResponse response = apartmentService.updateApartment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Apartment updated successfully", response, HttpStatus.OK.value()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Delete an apartment [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<Void>> deleteApartment(@PathVariable UUID id) {
        apartmentService.deleteApartment(id);
        return ResponseEntity.ok(ApiResponse.success("Apartment deleted successfully", HttpStatus.OK.value()));
    }
}
