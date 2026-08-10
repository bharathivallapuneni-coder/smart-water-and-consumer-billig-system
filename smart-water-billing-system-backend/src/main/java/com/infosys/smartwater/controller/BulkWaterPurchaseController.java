package com.infosys.smartwater.controller;

import com.infosys.smartwater.dto.response.ApiResponse;
import com.infosys.smartwater.entity.BulkWaterPurchase;
import com.infosys.smartwater.service.BulkWaterPurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/bulk-purchases", "/api/bulk-purchases"})
@RequiredArgsConstructor
@Tag(name = "Bulk Water Purchases", description = "Endpoints for Building Owners to record apartment water procurement")
public class BulkWaterPurchaseController {

    private final BulkWaterPurchaseService purchaseService;

    @Data
    public static class BulkPurchaseDTO {
        private UUID apartmentId;
        private UUID billingCycleId;
        private String sourceType;
        private String supplierName;
        private LocalDate purchaseDate;
        private BigDecimal purchasedVolumeKl;
        private BigDecimal totalCost;
        private String notes;
    }

    @GetMapping("/building/{apartmentId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Get bulk water purchases for an apartment building")
    public ResponseEntity<ApiResponse<List<BulkWaterPurchase>>> getPurchases(@PathVariable UUID apartmentId) {
        List<BulkWaterPurchase> purchases = purchaseService.getPurchasesByApartment(apartmentId);
        return ResponseEntity.ok(ApiResponse.success("Purchases retrieved successfully", purchases, HttpStatus.OK.value()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Record new bulk water purchase [BUILDING_OWNER]")
    public ResponseEntity<ApiResponse<BulkWaterPurchase>> createPurchase(@RequestBody BulkPurchaseDTO dto) {
        BulkWaterPurchase purchase = purchaseService.createPurchase(
                dto.getApartmentId(),
                dto.getBillingCycleId(),
                dto.getSourceType(),
                dto.getSupplierName(),
                dto.getPurchaseDate(),
                dto.getPurchasedVolumeKl(),
                dto.getTotalCost(),
                dto.getNotes()
        );
        return new ResponseEntity<>(ApiResponse.success("Bulk water purchase recorded successfully", purchase, HttpStatus.CREATED.value()), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Update bulk water purchase [BUILDING_OWNER]")
    public ResponseEntity<ApiResponse<BulkWaterPurchase>> updatePurchase(@PathVariable UUID id, @RequestBody BulkPurchaseDTO dto) {
        BulkWaterPurchase purchase = purchaseService.updatePurchase(
                id,
                dto.getSourceType(),
                dto.getSupplierName(),
                dto.getPurchaseDate(),
                dto.getPurchasedVolumeKl(),
                dto.getTotalCost(),
                dto.getNotes()
        );
        return ResponseEntity.ok(ApiResponse.success("Purchase updated successfully", purchase, HttpStatus.OK.value()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Delete bulk water purchase [BUILDING_OWNER]")
    public ResponseEntity<ApiResponse<Void>> deletePurchase(@PathVariable UUID id) {
        purchaseService.deletePurchase(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase deleted successfully", HttpStatus.OK.value()));
    }
}
