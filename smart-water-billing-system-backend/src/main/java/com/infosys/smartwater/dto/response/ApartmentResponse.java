package com.infosys.smartwater.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for {@code Apartment} resources.
 *
 * <p>Exposes only safe, non-sensitive apartment fields.
 * Does NOT include the nested {@code households} collection to avoid
 * N+1 problems — use the {@code GET /api/apartments/{id}/households} endpoint
 * to retrieve households for a specific apartment.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Apartment resource representation returned by the API")
public class ApartmentResponse {

    @Schema(description = "Unique identifier of the apartment",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Unique apartment code", example = "APT-001")
    private String apartmentNumber;

    @Schema(description = "Name of the apartment building", example = "Sunrise Towers")
    private String buildingName;

    @Schema(description = "Full postal address of the building",
            example = "42 Main Street, Chennai, Tamil Nadu 600001")
    private String address;

    @Schema(description = "Total number of floors in the building", example = "10")
    private Integer totalFloors;

    @Schema(description = "Denormalised count of registered households in this apartment",
            example = "24")
    private Integer totalHouseholds;

    @Schema(description = "Timestamp of record creation", example = "2026-01-15T09:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of last update", example = "2026-07-23T21:00:00")
    private LocalDateTime updatedAt;
}
