package com.infosys.smartwater.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating or updating a {@code Household}.
 *
 * <p>
 * Used by:
 * <ul>
 * <li>{@code POST /api/households} — create a new household</li>
 * <li>{@code PUT  /api/households/{id}} — update an existing household</li>
 * </ul>
 *
 * <p>
 * {@code userId} is optional — a household may be created without a linked
 * user account (resident can register later and be assigned to this household).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating or updating a household")
public class HouseholdRequest {

        @Schema(description = "Unique household code (e.g., APT-001-F02-U04)", example = "APT-001-F02-U04", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Household number is required")
        @Size(max = 50, message = "Household number cannot exceed 50 characters")
        private String householdNumber;

        @Schema(description = "Full name of the primary resident or property owner", example = "Rajesh Kumar", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Owner name is required")
        @Size(max = 100, message = "Owner name cannot exceed 100 characters")
        private String ownerName;

        @Schema(description = "Contact phone number (10–15 digits, optional + prefix)", example = "+919876543210")
        @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Contact phone must be 10 to 15 digits, optionally prefixed with +")
        private String contactPhone;

        @Schema(description = "UUID of the parent apartment this household belongs to", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Apartment ID is required")
        private UUID apartmentId;

        @Schema(description = "UUID of the resident user account to link — null if not yet assigned", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", nullable = true)
        private UUID userId;
}
