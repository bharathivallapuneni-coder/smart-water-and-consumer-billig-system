package com.infosys.smartwater.repository;

import com.infosys.smartwater.entity.Household;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Household} entities.
 *
 * <p>Provides standard CRUD via {@link JpaRepository} plus
 * custom finders and aggregation queries for household management,
 * apartment-scoped listing, and billing support.
 */
@Repository
public interface HouseholdRepository extends JpaRepository<Household, UUID> {

    // -------------------------------------------------------------------------
    // Lookup by business key
    // -------------------------------------------------------------------------

    /**
     * Finds a household by its unique household number.
     *
     * @param householdNumber the household code (e.g., "APT-001-F02-U04")
     * @return an {@link Optional} containing the household if found
     */
    Optional<Household> findByHouseholdNumber(String householdNumber);

    /**
     * Checks whether a household with the given number exists.
     *
     * @param householdNumber the household code to check
     * @return {@code true} if a household with this number exists
     */
    boolean existsByHouseholdNumber(String householdNumber);

    /**
     * Checks for duplicate household number, excluding the given ID.
     * Used during update operations to enforce uniqueness.
     *
     * @param householdNumber the code to check
     * @param id              the ID of the household being updated (excluded)
     * @return {@code true} if another household with this number exists
     */
    boolean existsByHouseholdNumberAndIdNot(String householdNumber, UUID id);

    /**
     * Finds the household assigned to a specific user account.
     *
     * @param userId the UUID of the user
     * @return an {@link Optional} containing the linked household, if any
     */
    Optional<Household> findByUserId(UUID userId);

    /**
     * Checks if a user ID is already linked to any household.
     * Enforces the OneToOne constraint at the application layer.
     *
     * @param userId the user UUID to check
     * @return {@code true} if a household is linked to this user
     */
    boolean existsByUserId(UUID userId);

    // -------------------------------------------------------------------------
    // Apartment-scoped finders
    // -------------------------------------------------------------------------

    /**
     * Returns all households in a specific apartment (paginated).
     *
     * @param apartmentId the apartment UUID
     * @param pageable    pagination and sorting parameters
     * @return a page of households in the apartment
     */
    Page<Household> findByApartmentId(UUID apartmentId, Pageable pageable);

    /**
     * Returns all households in an apartment filtered by active status (paginated).
     *
     * @param apartmentId the apartment UUID
     * @param isActive    {@code true} for active households, {@code false} for inactive
     * @param pageable    pagination and sorting parameters
     * @return a filtered page of households
     */
    Page<Household> findByApartmentIdAndIsActive(UUID apartmentId, Boolean isActive, Pageable pageable);

    /**
     * Returns all active households in a specific apartment (no pagination).
     * Used by billing cycle generation service to enumerate billing targets.
     *
     * @param apartmentId the apartment UUID
     * @param isActive    active status filter
     * @return list of matching households
     */
    List<Household> findByApartmentIdAndIsActive(UUID apartmentId, Boolean isActive);

    /**
     * Counts households in a given apartment.
     *
     * @param apartmentId the apartment UUID
     * @return total count of households (active + inactive)
     */
    long countByApartmentId(UUID apartmentId);

    /**
     * Counts households in a given apartment filtered by active status.
     *
     * @param apartmentId the apartment UUID
     * @param isActive    active status filter
     * @return count of matching households
     */
    long countByApartmentIdAndIsActive(UUID apartmentId, Boolean isActive);

    // -------------------------------------------------------------------------
    // Status-scoped finders
    // -------------------------------------------------------------------------

    /**
     * Returns all households filtered by their active status (paginated).
     *
     * @param isActive {@code true} for active, {@code false} for inactive
     * @param pageable pagination and sorting parameters
     * @return a page of matching households
     */
    Page<Household> findByIsActive(Boolean isActive, Pageable pageable);

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    /**
     * Full-text search across household number and owner name.
     * Case-insensitive partial match.
     *
     * @param keyword  the search term
     * @param pageable pagination and sorting parameters
     * @return a page of matching households
     */
    @Query("""
            SELECT h FROM Household h
            WHERE LOWER(h.householdNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(h.ownerName)       LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(h.contactPhone)    LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Household> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
