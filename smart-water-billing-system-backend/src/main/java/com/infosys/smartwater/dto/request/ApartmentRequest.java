package com.infosys.smartwater.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating an {@code Apartment}.
 *
 * <p>Used by:
 * <ul>
 *   <li>{@code POST /api/apartments}  — create a new apartment</li>
 *   <li>{@code PUT  /api/apartments/{id}} — update an existing apartment</li>
 * </ul>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating or updating an apartment")
public class ApartmentRequest {

    @Schema(description = "Unique apartment block code", example = "APT-001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Apartment number is required")
    @Size(max = 50, message = "Apartment number cannot exceed 50 characters")
    private String apartmentNumber;

    @Schema(description = "Name of the apartment building", example = "Sunrise Towers",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Building name is required")
    @Size(max = 100, message = "Building name cannot exceed 100 characters")
    private String buildingName;

    @Schema(description = "Full postal address of the building",
            example = "42 Main Street, Chennai, Tamil Nadu 600001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Address is required")
    private String address;

    @Schema(description = "Number of floors in the building (minimum 1)", example = "10",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Total floors is required")
    @Min(value = 1, message = "Total floors must be at least 1")
    @JsonAlias({"totalUnits", "total_units", "total_floors"})
    private Integer totalFloors;
}
