package com.infosys.smartwater.repository;

import com.infosys.smartwater.entity.BillingCycle;
import com.infosys.smartwater.entity.enums.BillingStatus;
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
 * Repository for {@link BillingCycle} entities.
 *
 * <p>Provides standard CRUD via {@link JpaRepository} plus custom finders for:
 * <ul>
 *   <li>Per-household billing history (paginated)</li>
 *   <li>Exact month/year lookup (enforces the unique constraint at app level)</li>
 *   <li>Status-based queries for payment tracking and overdue detection</li>
 *   <li>Aggregation queries for financial reporting</li>
 * </ul>
 */
@Repository
public interface BillingCycleRepository extends JpaRepository<BillingCycle, UUID> {

    // -------------------------------------------------------------------------
    // Exact-period lookups
    // -------------------------------------------------------------------------

    /**
     * Finds the billing cycle for a specific household in a specific month/year.
     * Enforces the unique(household_id, billing_month, billing_year) constraint
     * at the application layer.
     *
     * @param householdId  the household UUID
     * @param billingMonth the month number (1–12)
     * @param billingYear  the calendar year
     * @return an {@link Optional} containing the billing cycle if one has been generated
     */
    Optional<BillingCycle> findByHouseholdIdAndBillingMonthAndBillingYear(
            UUID householdId, Integer billingMonth, Integer billingYear);

    /**
     * Checks whether a billing cycle already exists for a household in a given month/year.
     * Used to prevent duplicate billing cycle generation.
     *
     * @param householdId  the household UUID
     * @param billingMonth the month (1–12)
     * @param billingYear  the year
     * @return {@code true} if a billing cycle already exists for this period
     */
    boolean existsByHouseholdIdAndBillingMonthAndBillingYear(
            UUID householdId, Integer billingMonth, Integer billingYear);

    // -------------------------------------------------------------------------
    // Household-scoped finders
    // -------------------------------------------------------------------------

    /**
     * Returns all billing cycles for a specific household (paginated),
     * ordered by year and month descending (most recent first).
     *
     * @param householdId the household UUID
     * @param pageable    pagination and sorting parameters
     * @return a page of billing cycles for the household
     */
    Page<BillingCycle> findByHouseholdId(UUID householdId, Pageable pageable);

    /**
     * Returns all billing cycles for a household with a specific status (paginated).
     *
     * @param householdId the household UUID
     * @param status      the billing status filter
     * @param pageable    pagination and sorting parameters
     * @return a filtered page of billing cycles
     */
    Page<BillingCycle> findByHouseholdIdAndStatus(
            UUID householdId, BillingStatus status, Pageable pageable);

    // -------------------------------------------------------------------------
    // Status-based finders (system-wide)
    // -------------------------------------------------------------------------

    /**
     * Returns all billing cycles with a specific status across all households (paginated).
     * Used by ADMIN to view all pending or overdue bills.
     *
     * @param status   the billing status
     * @param pageable pagination and sorting parameters
     * @return a page of matching billing cycles
     */
    Page<BillingCycle> findByStatus(BillingStatus status, Pageable pageable);

    /**
     * Finds all PENDING billing cycles whose due date has passed.
     * Called by the overdue detection scheduled job to mark them as {@code OVERDUE}.
     *
     * @param status  the status to filter by (always {@link BillingStatus#PENDING})
     * @param dueDate all cycles with a due date strictly before this date are returned
     * @return list of overdue-eligible billing cycles
     */
    List<BillingCycle> findByStatusAndDueDateBefore(BillingStatus status, LocalDate dueDate);

    // -------------------------------------------------------------------------
    // Period-scoped finders (system-wide)
    // -------------------------------------------------------------------------

    /**
     * Returns all billing cycles for a specific month and year across all households.
     * Used to generate monthly billing reports.
     *
     * @param billingMonth the month (1–12)
     * @param billingYear  the year
     * @param pageable     pagination and sorting parameters
     * @return a page of billing cycles for the period
     */
    Page<BillingCycle> findByBillingMonthAndBillingYear(
            Integer billingMonth, Integer billingYear, Pageable pageable);

    // -------------------------------------------------------------------------
    // Aggregation — financial reporting
    // -------------------------------------------------------------------------

    /**
     * Computes the total outstanding balance (PENDING + OVERDUE) for a household.
     *
     * @param householdId the household UUID
     * @return the total unpaid amount, or {@link BigDecimal#ZERO} if none
     */
    @Query("""
            SELECT COALESCE(SUM(bc.totalAmount), 0)
            FROM BillingCycle bc
            WHERE bc.household.id = :householdId
              AND bc.status IN ('PENDING', 'OVERDUE')
            """)
    BigDecimal sumOutstandingAmountByHousehold(@Param("householdId") UUID householdId);

    /**
     * Computes the total revenue collected (PAID cycles) in a given month and year.
     *
     * @param billingMonth the month (1–12)
     * @param billingYear  the year
     * @return total collected amount for the period, or {@link BigDecimal#ZERO}
     */
    @Query("""
            SELECT COALESCE(SUM(bc.totalAmount), 0)
            FROM BillingCycle bc
            WHERE bc.billingMonth = :billingMonth
              AND bc.billingYear  = :billingYear
              AND bc.status = 'PAID'
            """)
    BigDecimal sumCollectedAmountForMonth(
            @Param("billingMonth") Integer billingMonth,
            @Param("billingYear") Integer billingYear);

    long countByHouseholdId(UUID householdId);

    /**
     * Returns the count of billing cycles grouped by status for a given month/year.
     * Result: [status (String), count (Long)]
     *
     * @param billingMonth the month (1–12)
     * @param billingYear  the year
     * @return list of Object arrays containing [status, count]
     */
    @Query("""
            SELECT bc.status, COUNT(bc)
            FROM BillingCycle bc
            WHERE bc.billingMonth = :billingMonth
              AND bc.billingYear  = :billingYear
            GROUP BY bc.status
            """)
    List<Object[]> countByStatusForMonth(
            @Param("billingMonth") Integer billingMonth,
            @Param("billingYear") Integer billingYear);
}
