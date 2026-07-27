package com.infosys.smartwater.repository;

import com.infosys.smartwater.entity.TariffPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link TariffPlan} entities.
 *
 * <p>Provides standard CRUD via {@link JpaRepository} plus custom finders for:
 * <ul>
 *   <li>Business-key lookup and uniqueness checks</li>
 *   <li>Finding the currently active tariff plan</li>
 *   <li>Date-range validation (no overlapping active plans)</li>
 * </ul>
 */
@Repository
public interface TariffPlanRepository extends JpaRepository<TariffPlan, UUID> {

    // -------------------------------------------------------------------------
    // Business-key lookups
    // -------------------------------------------------------------------------

    /**
     * Finds a tariff plan by its unique plan name.
     *
     * @param planName the plan name (e.g., "Standard 2026")
     * @return an {@link Optional} containing the tariff plan if found
     */
    Optional<TariffPlan> findByPlanName(String planName);

    /**
     * Checks whether a tariff plan with the given name exists.
     *
     * @param planName the plan name to check
     * @return {@code true} if a plan with this name already exists
     */
    boolean existsByPlanName(String planName);

    /**
     * Checks for a duplicate plan name, excluding the given ID.
     * Used during update to allow the plan to retain its own name.
     *
     * @param planName the name to check
     * @param id       the plan ID to exclude
     * @return {@code true} if another plan has this name
     */
    boolean existsByPlanNameAndIdNot(String planName, UUID id);

    // -------------------------------------------------------------------------
    // Status-based finders
    // -------------------------------------------------------------------------

    /**
     * Returns all active tariff plans.
     * In normal operation, at most one plan should be active at a time.
     *
     * @return list of currently active tariff plans
     */
    List<TariffPlan> findByIsActiveTrue();

    /**
     * Returns all tariff plans filtered by active status (paginated).
     *
     * @param isActive {@code true} for active plans, {@code false} for deactivated
     * @param pageable pagination and sorting parameters
     * @return a page of matching tariff plans
     */
    Page<TariffPlan> findByIsActive(Boolean isActive, Pageable pageable);

    // -------------------------------------------------------------------------
    // Date-based finders
    // -------------------------------------------------------------------------

    /**
     * Finds all tariff plans that are valid on a given date.
     * A plan is valid if: {@code effectiveFrom <= date AND (effectiveTo IS NULL OR effectiveTo >= date)}.
     *
     * <p>Used by the billing service to select the applicable plan
     * when generating a billing cycle.
     *
     * @param date the date to check plan validity against
     * @return list of tariff plans valid on the given date
     */
    @Query("""
            SELECT tp FROM TariffPlan tp
            WHERE tp.effectiveFrom <= :date
              AND (tp.effectiveTo IS NULL OR tp.effectiveTo >= :date)
              AND tp.isActive = true
            ORDER BY tp.effectiveFrom DESC
            """)
    List<TariffPlan> findActivePlansOnDate(@Param("date") LocalDate date);

    /**
     * Finds the single active tariff plan valid for a specific billing month/year.
     * Returns the most recently effective plan if multiple match (edge case).
     *
     * <p>This is the primary query used by the billing cycle generation service.
     *
     * @param date the last day of the billing month (used to evaluate plan validity)
     * @return an {@link Optional} containing the applicable tariff plan
     */
    @Query("""
            SELECT tp FROM TariffPlan tp
            WHERE tp.effectiveFrom <= :date
              AND (tp.effectiveTo IS NULL OR tp.effectiveTo >= :date)
              AND tp.isActive = true
            ORDER BY tp.effectiveFrom DESC
            LIMIT 1
            """)
    Optional<TariffPlan> findApplicablePlanForDate(@Param("date") LocalDate date);

    /**
     * Checks whether any active tariff plan overlaps with the given date range.
     * Used to prevent creating plans whose validity periods collide with existing active plans.
     *
     * @param effectiveFrom start of the proposed plan period (inclusive)
     * @param effectiveTo   end of the proposed plan period (exclusive, nullable)
     * @param excludeId     UUID to exclude from the check (for updates)
     * @return {@code true} if an overlapping active plan exists
     */
    @Query("""
            SELECT COUNT(tp) > 0 FROM TariffPlan tp
            WHERE tp.isActive = true
              AND tp.id <> :excludeId
              AND tp.effectiveFrom <= COALESCE(:effectiveTo, CURRENT_DATE)
              AND (tp.effectiveTo IS NULL OR tp.effectiveTo >= :effectiveFrom)
            """)
    boolean existsOverlappingActivePlan(
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo,
            @Param("excludeId") UUID excludeId);
}
