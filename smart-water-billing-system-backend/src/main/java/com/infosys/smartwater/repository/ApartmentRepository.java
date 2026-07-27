package com.infosys.smartwater.repository;

import com.infosys.smartwater.entity.Apartment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Apartment} entities.
 *
 * <p>Provides standard CRUD via {@link JpaRepository} plus
 * custom finder and search queries for the Apartment management API.
 */
@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {

    // -------------------------------------------------------------------------
    // Lookup by business key
    // -------------------------------------------------------------------------

    /**
     * Finds an apartment by its unique apartment number.
     *
     * @param apartmentNumber the apartment code (e.g., "APT-001")
     * @return an {@link Optional} containing the apartment if found
     */
    Optional<Apartment> findByApartmentNumber(String apartmentNumber);

    /**
     * Checks whether an apartment with the given number already exists.
     * Used during create/update to enforce uniqueness without loading the entity.
     *
     * @param apartmentNumber the apartment code to check
     * @return {@code true} if an apartment with this number exists
     */
    boolean existsByApartmentNumber(String apartmentNumber);

    /**
     * Checks whether an apartment exists with the given number,
     * excluding a specific apartment ID (used for update uniqueness validation).
     *
     * @param apartmentNumber the apartment code to check
     * @param id              the ID of the apartment being updated (excluded from check)
     * @return {@code true} if another apartment with this number exists
     */
    boolean existsByApartmentNumberAndIdNot(String apartmentNumber, UUID id);

    // -------------------------------------------------------------------------
    // Paginated search
    // -------------------------------------------------------------------------

    /**
     * Full-text search across apartment number, building name, and address.
     * Case-insensitive partial match using LIKE.
     *
     * @param keyword  the search term
     * @param pageable pagination and sorting parameters
     * @return a page of matching apartments
     */
    @Query("""
            SELECT a FROM Apartment a
            WHERE LOWER(a.apartmentNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(a.buildingName)    LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(a.address)         LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Apartment> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Finds all apartments whose building name contains the given substring
     * (case-insensitive). Useful for filtering by building.
     *
     * @param buildingName partial building name to search
     * @param pageable     pagination and sorting parameters
     * @return a page of matching apartments
     */
    Page<Apartment> findByBuildingNameContainingIgnoreCase(String buildingName, Pageable pageable);

    // -------------------------------------------------------------------------
    // Reporting queries
    // -------------------------------------------------------------------------

    /**
     * Counts the total number of registered apartments in the system.
     * Delegates to the inherited {@code count()} method; documented here for clarity.
     */
    // count() — inherited from JpaRepository

    /**
     * Returns the total number of apartments that have at least one household.
     *
     * @return count of apartments with at least one household registered
     */
    @Query("SELECT COUNT(DISTINCT a) FROM Apartment a WHERE SIZE(a.households) > 0")
    long countApartmentsWithHouseholds();
}
