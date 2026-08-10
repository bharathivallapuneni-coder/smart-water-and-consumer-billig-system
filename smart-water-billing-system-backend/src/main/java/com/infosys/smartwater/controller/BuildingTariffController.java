package com.infosys.smartwater.controller;

import com.infosys.smartwater.dto.response.ApiResponse;
import com.infosys.smartwater.entity.Apartment;
import com.infosys.smartwater.entity.TariffTier;
import com.infosys.smartwater.exception.ResourceNotFoundException;
import com.infosys.smartwater.repository.ApartmentRepository;
import com.infosys.smartwater.repository.TariffTierRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/tariffs", "/api/tariffs"})
@RequiredArgsConstructor
@Tag(name = "Building Tariffs", description = "Endpoints for configuring building-specific tiered water tariffs")
public class BuildingTariffController {

    private final TariffTierRepository tariffTierRepository;
    private final ApartmentRepository apartmentRepository;

    @Data
    public static class TariffTierDTO {
        private String tierName;
        private BigDecimal minKl;
        private BigDecimal maxKl;
        private BigDecimal ratePerKl;
        private BigDecimal fixedCharge;
    }

    @GetMapping("/building/{buildingId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN', 'RESIDENT')")
    @Operation(summary = "Get active tariff tiers for a building")
    public ResponseEntity<ApiResponse<List<TariffTier>>> getBuildingTariffs(@PathVariable UUID buildingId) {
        List<TariffTier> tiers = tariffTierRepository.findByApartmentIdOrderByMinKlAsc(buildingId);
        return ResponseEntity.ok(ApiResponse.success("Building tariffs retrieved", tiers, HttpStatus.OK.value()));
    }

    @PostMapping("/building/{buildingId}")
    @Transactional
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Configure tiered tariff rates for a building [BUILDING_OWNER / SUPERADMIN]")
    public ResponseEntity<ApiResponse<List<TariffTier>>> saveBuildingTariffs(
            @PathVariable UUID buildingId,
            @RequestBody List<TariffTierDTO> dtoList
    ) {
        Apartment apartment = apartmentRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment", "id", buildingId));

        tariffTierRepository.deleteByApartmentId(buildingId);

        List<TariffTier> savedTiers = new ArrayList<>();
        if (dtoList != null) {
            for (TariffTierDTO dto : dtoList) {
                TariffTier tier = TariffTier.builder()
                        .apartment(apartment)
                        .tierName(dto.getTierName() != null ? dto.getTierName() : "Standard Tier")
                        .minKl(dto.getMinKl() != null ? dto.getMinKl() : BigDecimal.ZERO)
                        .maxKl(dto.getMaxKl())
                        .ratePerKl(dto.getRatePerKl() != null ? dto.getRatePerKl() : BigDecimal.ZERO)
                        .fixedCharge(dto.getFixedCharge() != null ? dto.getFixedCharge() : BigDecimal.ZERO)
                        .build();
                savedTiers.add(tariffTierRepository.save(tier));
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Building tariff rates updated successfully", savedTiers, HttpStatus.OK.value()));
    }
}
