package com.infosys.smartwater.service;

import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.dto.response.UserResponse;
import com.infosys.smartwater.entity.enums.Role;

import java.util.UUID;

/**
 * Service contract for {@code User} management operations.
 *
 * <p>Registration and authentication are handled by {@code AuthService} (Module 4).
 * This service handles ADMIN-facing user management: lookup, role filtering,
 * enable/disable, and deletion.
 */
public interface UserService {

    UserResponse getUserById(UUID id);

    UserResponse getUserByEmail(String email);

    PagedResponse<UserResponse> getAllUsers(int page, int size, String sortBy, String sortDir);

    PagedResponse<UserResponse> getUsersByRole(Role role, int page, int size);

    PagedResponse<UserResponse> searchUsers(String keyword, int page, int size);

    /** Enables a previously disabled user account. */
    UserResponse enableUser(UUID id);

    /** Disables a user account (blocks login without deleting). */
    UserResponse disableUser(UUID id);

    /**
     * Hard-deletes a user account.
     * Throws {@code InvalidOperationException} if the user is still linked to a household.
     */
    void deleteUser(UUID id);
}
