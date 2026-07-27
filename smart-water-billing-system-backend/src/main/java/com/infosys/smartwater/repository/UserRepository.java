package com.infosys.smartwater.repository;

import com.infosys.smartwater.entity.User;
import com.infosys.smartwater.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link User} entities.
 *
 * <p>Provides standard CRUD via {@link JpaRepository} plus
 * custom finders for authentication, uniqueness validation,
 * and role-based user management.
 *
 * <p><b>Note:</b> The {@link #findByEmail(String)} method is the primary
 * lookup used by Spring Security's {@code UserDetailsService} implementation
 * (added in Module 4).
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // -------------------------------------------------------------------------
    // Authentication lookups
    // -------------------------------------------------------------------------

    /**
     * Finds a user by their email address.
     * Used as the primary lookup by the Spring Security {@code UserDetailsService}.
     *
     * @param email the user's email address
     * @return an {@link Optional} containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by their username.
     *
     * @param username the user's login username
     * @return an {@link Optional} containing the user if found
     */
    Optional<User> findByUsername(String username);

    // -------------------------------------------------------------------------
    // Uniqueness checks (used during registration / update)
    // -------------------------------------------------------------------------

    /**
     * Checks whether a user with the given email already exists.
     * Used to prevent duplicate registrations.
     *
     * @param email the email to check
     * @return {@code true} if the email is already registered
     */
    boolean existsByEmail(String email);

    /**
     * Checks whether a user with the given username already exists.
     *
     * @param username the username to check
     * @return {@code true} if the username is taken
     */
    boolean existsByUsername(String username);

    /**
     * Checks for duplicate email, excluding the given user ID.
     * Used during profile update to allow the user to keep their own email.
     *
     * @param email the email to check
     * @param id    the user ID to exclude from the check
     * @return {@code true} if another user has this email
     */
    boolean existsByEmailAndIdNot(String email, UUID id);

    /**
     * Checks for duplicate username, excluding the given user ID.
     *
     * @param username the username to check
     * @param id       the user ID to exclude from the check
     * @return {@code true} if another user has this username
     */
    boolean existsByUsernameAndIdNot(String username, UUID id);

    // -------------------------------------------------------------------------
    // Role-based finders
    // -------------------------------------------------------------------------

    /**
     * Returns all users with a specific role (paginated).
     *
     * @param role     the role to filter by ({@code ADMIN} or {@code RESIDENT})
     * @param pageable pagination and sorting parameters
     * @return a page of users with the specified role
     */
    Page<User> findByRole(Role role, Pageable pageable);

    /**
     * Returns all users with a specific approval status (paginated).
     *
     * @param approvalStatus the approval status to filter by
     * @param pageable       pagination and sorting parameters
     * @return a page of users with the specified approval status
     */
    Page<User> findByApprovalStatus(com.infosys.smartwater.entity.enums.ApprovalStatus approvalStatus, Pageable pageable);

    /**
     * Returns all enabled/disabled users (paginated).
     *
     * @param isEnabled {@code true} for active accounts, {@code false} for suspended
     * @param pageable  pagination and sorting parameters
     * @return a page of matching users
     */
    Page<User> findByIsEnabled(Boolean isEnabled, Pageable pageable);

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    /**
     * Full-text search across username and email (case-insensitive).
     *
     * @param keyword  the search term
     * @param pageable pagination and sorting parameters
     * @return a page of matching users
     */
    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.phone)    LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // -------------------------------------------------------------------------
    // Reporting
    // -------------------------------------------------------------------------

    /**
     * Counts the number of users with each role.
     *
     * @param role the role to count
     * @return count of users with the given role
     */
    long countByRole(Role role);

    /**
     * Returns the count of enabled accounts.
     *
     * @return number of active user accounts
     */
    long countByIsEnabled(Boolean isEnabled);
}
