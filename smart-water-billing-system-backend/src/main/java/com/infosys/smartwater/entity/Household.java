package com.infosys.smartwater.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an individual dwelling unit (flat/unit) within an {@link Apartment}.
 *
 * <p><b>Relationship summary:</b>
 * <pre>
 *   Apartment  1 ── * Household * ── 1  User       (owns FK: user_id)
 *                                    1 ── * WaterUsage
 *                                    1 ── * BillingCycle
 * </pre>
 *
 * <p><b>FK ownership:</b>
 * <ul>
 *   <li>{@code apartment_id} — FK from this table to {@code apartments}</li>
 *   <li>{@code user_id}      — FK from this table to {@code users} (UNIQUE — one resident per household)</li>
 * </ul>
 *
 * <p><b>isActive:</b> Soft-delete mechanism. Inactive households are excluded
 * from billing generation and usage aggregation but are preserved for audit trails.
 *
 * <p><b>Cascade strategy for collections:</b> PERSIST + MERGE only.
 * Deletion of water readings or billing cycles must go through their
 * respective services to apply business rule validation.
 */
@Entity
@Table(
        name = "households",
        indexes = {
                @Index(name = "idx_households_apartment", columnList = "apartment_id"),
                @Index(name = "idx_households_number",    columnList = "household_number"),
                @Index(name = "idx_households_active",    columnList = "is_active"),
                @Index(name = "idx_households_user",      columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"apartment", "user", "waterUsages", "billingCycles"})
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Household extends BaseEntity {

    // -------------------------------------------------------------------------
    // Core fields
    // -------------------------------------------------------------------------

    /**
     * Unique identifier code for this household (e.g., {@code APT-001-F02-U04}).
     */
    @EqualsAndHashCode.Include
    @NotBlank(message = "Household number is required")
    @Size(max = 50, message = "Household number cannot exceed 50 characters")
    @Column(name = "household_number", nullable = false, unique = true, length = 50)
    private String householdNumber;

    /**
     * Full name of the primary resident / property owner.
     */
    @NotBlank(message = "Owner name is required")
    @Size(max = 100, message = "Owner name cannot exceed 100 characters")
    @Column(name = "owner_name", nullable = false, length = 100)
    private String ownerName;

    /**
     * Primary contact phone number for the household.
     * Optional at database level; recommended for billing communications.
     */
    @Pattern(
            regexp  = "^[+]?[0-9]{10,15}$",
            message = "Contact phone must be 10 to 15 digits, optionally prefixed with +"
    )
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /**
     * Whether this household is currently active.
     * Inactive households are skipped during billing cycle generation.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Flat area in sq ft or sq m used for fallback water cost distribution.
     */
    @Column(name = "flat_area", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private java.math.BigDecimal flatArea = new java.math.BigDecimal("1000.00");

    /**
     * Flag indicating if the household has an active meter installed.
     */
    @Column(name = "is_metered", nullable = false)
    @Builder.Default
    private Boolean isMetered = true;

    /**
     * Configurable alert threshold in kL for high usage alerts.
     */
    @Column(name = "alert_threshold_kl", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private java.math.BigDecimal alertThresholdKl = new java.math.BigDecimal("20.00");

    // -------------------------------------------------------------------------
    // Relationships
    // -------------------------------------------------------------------------

    /**
     * The apartment building this household belongs to.
     * LAZY-loaded; fetched on demand. NOT NULL — every household must have a parent apartment.
     */
    @NotNull(message = "Apartment is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name           = "apartment_id",
            nullable       = false,
            foreignKey     = @ForeignKey(name = "fk_households_apartment")
    )
    private Apartment apartment;

    /**
     * The resident {@link User} account linked to this household.
     * {@code null} is permitted — a household may exist before a resident registers.
     * The FK ({@code user_id}) is owned by this table with a UNIQUE constraint
     * (one household ↔ one resident account).
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name       = "user_id",
            unique     = true,
            foreignKey = @ForeignKey(name = "fk_households_user")
    )
    private User user;

    /**
     * All water meter readings for this household.
     * Cascade PERSIST + MERGE; use WaterUsageService for deletion.
     */
    @OneToMany(
            mappedBy = "household",
            cascade  = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch    = FetchType.LAZY
    )
    @Builder.Default
    private List<WaterUsage> waterUsages = new ArrayList<>();

    /**
     * All monthly billing cycles generated for this household.
     * Cascade PERSIST + MERGE; use BillingCycleService for deletion.
     */
    @OneToMany(
            mappedBy = "household",
            cascade  = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch    = FetchType.LAZY
    )
    @Builder.Default
    private List<BillingCycle> billingCycles = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Convenience helpers
    // -------------------------------------------------------------------------

    /**
     * Assigns a resident user to this household, setting the bidirectional link.
     *
     * @param user the resident {@link User} to assign
     */
    public void assignUser(User user) {
        this.user = user;
    }

    /**
     * Removes the resident user from this household, breaking the bidirectional link.
     */
    public void removeUser() {
        this.user = null;
    }

    /**
     * Adds a water usage reading to this household and sets the back-reference.
     *
     * @param usage the {@link WaterUsage} record to add
     */
    public void addWaterUsage(WaterUsage usage) {
        waterUsages.add(usage);
        usage.setHousehold(this);
    }

    /**
     * Adds a billing cycle to this household and sets the back-reference.
     *
     * @param cycle the {@link BillingCycle} to add
     */
    public void addBillingCycle(BillingCycle cycle) {
        billingCycles.add(cycle);
        cycle.setHousehold(this);
    }
}
