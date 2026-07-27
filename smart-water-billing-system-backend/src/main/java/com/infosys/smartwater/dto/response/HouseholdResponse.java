package com.infosys.smartwater.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for {@code Household} resources.
 *
 * <p>Includes a flattened summary of the parent apartment (number + building name)
 * and the linked user (ID + email) to eliminate extra API round-trips.
 * Does NOT nest full {@code Apartment} or {@code User} objects.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Household resource representation returned by the API")
public class HouseholdResponse {

    @Schema(description = "Unique identifier of the household",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Unique household code", example = "APT-001-F02-U04")
    private String householdNumber;

    @Schema(description = "Name of the primary resident / property owner",
            example = "Rajesh Kumar")
    private String ownerName;

    @Schema(description = "Contact phone number of the household", example = "+919876543210")
    private String contactPhone;

    @Schema(description = "Whether the household is currently active", example = "true")
    private Boolean isActive;

    // Flattened apartment summary
    @Schema(description = "UUID of the parent apartment",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID apartmentId;

    @Schema(description = "Apartment code of the parent building", example = "APT-001")
    private String apartmentNumber;

    @Schema(description = "Building name of the parent apartment", example = "Sunrise Towers")
    private String buildingName;

    // Flattened user summary (null if no user linked)
    @Schema(description = "UUID of the linked resident user account — null if unassigned",
            nullable = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID userId;

    @Schema(description = "Username of the linked resident — null if unassigned",
            nullable = true, example = "rajesh_kumar")
    private String username;

    @Schema(description = "Email of the linked resident — null if unassigned",
            nullable = true, example = "rajesh@example.com")
    private String userEmail;

    @Schema(description = "Timestamp of record creation", example = "2026-01-15T09:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of last update", example = "2026-07-23T21:00:00")
    private LocalDateTime updatedAt;
}
