package com.infosys.smartwater.entity;

import com.infosys.smartwater.entity.enums.ReadingType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Records a single water meter reading for a {@link Household} on a given date.
 *
 * <p>Each entry represents the cumulative meter display value at {@code readingDate}.
 * The {@code unitsConsumed} field is the difference between the current and previous
 * reading and is computed at the service layer before persistence.
 *
 * <p><b>Uniqueness:</b> Only one reading per household per day is permitted,
 * enforced by the {@code uq_water_household_date} unique constraint.
 *
 * <p><b>Reading sources:</b>
 * <ul>
 *   <li>{@link ReadingType#MANUAL}     — entered via the REST API by an ADMIN</li>
 *   <li>{@link ReadingType#CSV_IMPORT} — bulk-loaded from a CSV file (Task 13)</li>
 * </ul>
 *
 * <p><b>Billing integration:</b> The billing service aggregates
 * {@code unitsConsumed} across all readings in a calendar month to populate
 * {@link BillingCycle#getTotalUnitsConsumed()}.
 *
 * <p><b>Deletion policy:</b> Blocked at the DB level. Readings are immutable once
 * a billing cycle has been generated for the same period.
 */
@Entity
@Table(
        name = "water_usages",
        indexes = {
                @Index(name = "idx_water_household",      columnList = "household_id"),
                @Index(name = "idx_water_date",           columnList = "reading_date"),
                @Index(name = "idx_water_household_date", columnList = "household_id,reading_date"),
                @Index(name = "idx_water_reading_type",   columnList = "reading_type")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name        = "uq_water_household_date",
                        columnNames = {"household_id", "reading_date"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "household")
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class WaterUsage extends BaseEntity {

    // -------------------------------------------------------------------------
    // Relationships
    // -------------------------------------------------------------------------

    /**
     * The household whose meter this reading belongs to.
     * LAZY-loaded; NOT NULL.
     */
    @NotNull(message = "Household is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name       = "household_id",
            nullable   = false,
            foreignKey = @ForeignKey(name = "fk_water_household")
    )
    private Household household;

    // -------------------------------------------------------------------------
    // Reading data
    // -------------------------------------------------------------------------

    /**
     * Date on which the meter reading was taken.
     * Combined with {@code household_id} forms the unique key for this table.
     */
    @EqualsAndHashCode.Include
    @NotNull(message = "Reading date is required")
    @Column(name = "reading_date", nullable = false)
    private LocalDate readingDate;

    /**
     * Cumulative meter reading (in cubic metres / kL) as shown on the physical meter.
     * Must be ≥ {@code previousReading}.
     */
    @NotNull(message = "Meter reading is required")
    @DecimalMin(value = "0.00", message = "Meter reading cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Meter reading must have at most 10 integer and 2 decimal digits")
    @Column(name = "meter_reading", nullable = false, precision = 12, scale = 2)
    private BigDecimal meterReading;

    /**
     * Meter reading from the immediately preceding entry for this household.
     * Defaults to {@code 0.00} for the very first reading.
     */
    @NotNull(message = "Previous reading is required")
    @DecimalMin(value = "0.00", message = "Previous reading cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Previous reading must have at most 10 integer and 2 decimal digits")
    @Column(name = "previous_reading", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal previousReading = BigDecimal.ZERO;

    /**
     * Units of water consumed in this reading period.
     * Computed by the service: {@code meterReading - previousReading}.
     * Stored for query performance and reporting; must be ≥ 0.
     */
    @NotNull(message = "Units consumed is required")
    @DecimalMin(value = "0.00", message = "Units consumed cannot be negative")
    @Column(name = "units_consumed", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal unitsConsumed = BigDecimal.ZERO;

    /**
     * How this reading was entered into the system.
     * Defaults to {@link ReadingType#MANUAL}.
     */
    @NotNull(message = "Reading type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "reading_type", nullable = false, length = 20)
    @Builder.Default
    private ReadingType readingType = ReadingType.MANUAL;

    /**
     * Optional free-text notes for this reading (e.g., "Meter replaced",
     * "Suspected leak — field officer notified").
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // -------------------------------------------------------------------------
    // Business logic helpers
    // -------------------------------------------------------------------------

    /**
     * Computes and sets {@code unitsConsumed} from the current and previous readings.
     * Must be called before persisting a new {@code WaterUsage} record.
     *
     * @throws IllegalArgumentException if {@code meterReading} is less than {@code previousReading}
     */
    public void computeUnitsConsumed() {
        if (meterReading == null || previousReading == null) {
            throw new IllegalStateException("Both meterReading and previousReading must be set before computing unitsConsumed");
        }
        if (meterReading.compareTo(previousReading) < 0) {
            throw new IllegalArgumentException(
                    String.format("Meter reading [%s] cannot be less than previous reading [%s]",
                            meterReading, previousReading));
        }
        this.unitsConsumed = meterReading.subtract(previousReading);
    }
}
