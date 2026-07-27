package com.infosys.smartwater.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical apartment building registered in the billing system.
 *
 * <p>An {@code Apartment} is the top-level organisational unit:
 * <pre>
 *   Apartment  1 ──────────── * Household
 * </pre>
 *
 * <p><b>Cascade strategy:</b> Only {@code PERSIST} and {@code MERGE} are cascaded
 * to households. Deletion of an apartment is blocked at the DB level
 * ({@code ON DELETE RESTRICT}) to prevent accidental data loss.
 *
 * <p><b>totalHouseholds:</b> A denormalised count maintained programmatically by
 * the service layer whenever a household is created or deactivated.
 * This avoids expensive COUNT queries on the households table for dashboards.
 */
@Entity
@Table(
        name = "apartments",
        indexes = {
                @Index(name = "idx_apartments_number",   columnList = "apartment_number"),
                @Index(name = "idx_apartments_building", columnList = "building_name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "households")
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Apartment extends BaseEntity {

    // -------------------------------------------------------------------------
    // Core fields
    // -------------------------------------------------------------------------

    /**
     * Unique alphanumeric code identifying this apartment block (e.g., {@code APT-001}).
     * Used in all cross-references and reports.
     */
    @EqualsAndHashCode.Include
    @NotBlank(message = "Apartment number is required")
    @Size(max = 50, message = "Apartment number cannot exceed 50 characters")
    @Column(name = "apartment_number", nullable = false, unique = true, length = 50)
    private String apartmentNumber;

    /**
     * Human-readable name of the building (e.g., {@code "Sunrise Towers"}).
     */
    @NotBlank(message = "Building name is required")
    @Size(max = 100, message = "Building name cannot exceed 100 characters")
    @Column(name = "building_name", nullable = false, length = 100)
    private String buildingName;

    /**
     * Full postal address of the apartment building.
     */
    @NotBlank(message = "Address is required")
    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    /**
     * Number of floors in the building. Must be at least 1.
     */
    @Min(value = 1, message = "Total floors must be at least 1")
    @Column(name = "total_floors", nullable = false)
    @Builder.Default
    private Integer totalFloors = 1;

    /**
     * Denormalised count of registered (active) households.
     * Updated by the service layer on household create/deactivate.
     */
    @Min(value = 0, message = "Total households count cannot be negative")
    @Column(name = "total_households", nullable = false)
    @Builder.Default
    private Integer totalHouseholds = 0;

    // -------------------------------------------------------------------------
    // Relationships
    // -------------------------------------------------------------------------

    /**
     * All households belonging to this apartment.
     * Cascade PERSIST + MERGE only — removal requires explicit household service calls.
     */
    @OneToMany(
            mappedBy = "apartment",
            cascade  = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch    = FetchType.LAZY
    )
    @Builder.Default
    private List<Household> households = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Convenience helpers
    // -------------------------------------------------------------------------

    /**
     * Adds a household to this apartment and sets the back-reference.
     * Also increments the denormalised {@code totalHouseholds} counter.
     *
     * @param household the household to add
     */
    public void addHousehold(Household household) {
        households.add(household);
        household.setApartment(this);
        this.totalHouseholds++;
    }

    /**
     * Removes a household from this apartment and clears the back-reference.
     * Also decrements the denormalised {@code totalHouseholds} counter.
     *
     * @param household the household to remove
     */
    public void removeHousehold(Household household) {
        households.remove(household);
        household.setApartment(null);
        if (this.totalHouseholds > 0) {
            this.totalHouseholds--;
        }
    }
}
