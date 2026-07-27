package com.infosys.smartwater.repository;

import com.infosys.smartwater.entity.WaterUsage;
import com.infosys.smartwater.entity.enums.ReadingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link WaterUsage} entities.
 *
 * <p>Provides standard CRUD via {@link JpaRepository} plus custom queries for:
 * <ul>
 *   <li>Daily reading lookup (exact date)</li>
 *   <li>Monthly reading lookup (date range)</li>
 *   <li>Monthly unit aggregation for billing cycle computation</li>
 *   <li>Duplicate detection for both manual entry and CSV import</li>
 * </ul>
 */
@Repository
public interface WaterUsageRepository extends JpaRepository<WaterUsage, UUID> {

    // -------------------------------------------------------------------------
    // Exact-date lookups
    // -------------------------------------------------------------------------

    /**
     * Finds the water usage record for a specific household on a specific date.
     * Enforces the one-reading-per-household-per-day rule.
     *
     * @param householdId the household UUID
     * @param readingDate the exact reading date
     * @return an {@link Optional} containing the record if found
     */
    Optional<WaterUsage> findByHouseholdIdAndReadingDate(UUID householdId, LocalDate readingDate);

    /**
     * Checks whether a reading already exists for the given household and date.
     * Used for duplicate detection during manual entry and CSV import.
     *
     * @param householdId the household UUID
     * @param readingDate the reading date to check
     * @return {@code true} if a reading already exists for this household on this date
     */
    boolean existsByHouseholdIdAndReadingDate(UUID householdId, LocalDate readingDate);

    // -------------------------------------------------------------------------
    // Range-based lookups (household-scoped)
    // -------------------------------------------------------------------------

    /**
     * Returns all water usage records for a household within a date range (paginated).
     * The primary query used by the "Get Daily Reading" and "Get Monthly Reading" APIs.
     *
     * @param householdId the household UUID
     * @param startDate   the start date (inclusive)
     * @param endDate     the end date (inclusive)
     * @param pageable    pagination and sorting parameters
     * @return a page of water usage records in the date range
     */
    Page<WaterUsage> findByHouseholdIdAndReadingDateBetween(
            UUID householdId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Returns all water usage records for a household within a date range (no pagination).
     * Used by the billing service to aggregate units for a billing period.
     *
     * @param householdId the household UUID
     * @param startDate   the start date (inclusive)
     * @param endDate     the end date (inclusive)
     * @return list of water usage records ordered by reading date
     */
    List<WaterUsage> findByHouseholdIdAndReadingDateBetweenOrderByReadingDateAsc(
            UUID householdId, LocalDate startDate, LocalDate endDate);

    /**
     * Returns all water usage records for a household (paginated).
     *
     * @param householdId the household UUID
     * @param pageable    pagination and sorting parameters
     * @return a page of water usage records for the household
     */
    Page<WaterUsage> findByHouseholdId(UUID householdId, Pageable pageable);

    // -------------------------------------------------------------------------
    // Aggregation — used by billing service
    // -------------------------------------------------------------------------

    /**
     * Computes the total units consumed by a household within a date range.
     * Called by the billing service to populate {@code BillingCycle.totalUnitsConsumed}.
     *
     * <p>Returns {@code 0} via {@code COALESCE} when no records exist in the range
     * (avoids {@code null} handling in the service layer).
     *
     * @param householdId the household UUID
     * @param startDate   billing period start date (first day of the month)
     * @param endDate     billing period end date (last day of the month)
     * @return total units consumed in the period, or {@link BigDecimal#ZERO}
     */
    @Query("""
            SELECT COALESCE(SUM(wu.unitsConsumed), 0)
            FROM WaterUsage wu
            WHERE wu.household.id = :householdId
              AND wu.readingDate BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumUnitsConsumedBetween(
            @Param("householdId") UUID householdId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Returns the most recent water usage record for a household.
     * Used to determine the {@code previousReading} value for new entries.
     *
     * @param householdId the household UUID
     * @return an {@link Optional} containing the latest reading, if any
     */
    @Query("""
            SELECT wu FROM WaterUsage wu
            WHERE wu.household.id = :householdId
            ORDER BY wu.readingDate DESC
            LIMIT 1
            """)
    Optional<WaterUsage> findLatestByHouseholdId(@Param("householdId") UUID householdId);

    // -------------------------------------------------------------------------
    // Reading-type filter
    // -------------------------------------------------------------------------

    /**
     * Returns all water usage records with a specific reading type (paginated).
     * Useful for auditing CSV imports vs manual entries.
     *
     * @param readingType the reading type to filter by
     * @param pageable    pagination and sorting parameters
     * @return a page of matching water usage records
     */
    Page<WaterUsage> findByReadingType(ReadingType readingType, Pageable pageable);

    // -------------------------------------------------------------------------
    // Reporting
    // -------------------------------------------------------------------------

    /**
     * Returns the count of readings for a specific household.
     *
     * @param householdId the household UUID
     * @return total number of readings for the household
     */
    long countByHouseholdId(UUID householdId);

    /**
     * Returns total units consumed by all households in the system
     * within a date range. Used for system-wide reporting dashboards.
     *
     * @param startDate range start (inclusive)
     * @param endDate   range end (inclusive)
     * @return total units consumed across all households in the range
     */
    @Query("""
            SELECT COALESCE(SUM(wu.unitsConsumed), 0)
            FROM WaterUsage wu
            WHERE wu.readingDate BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumTotalUnitsConsumedBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
