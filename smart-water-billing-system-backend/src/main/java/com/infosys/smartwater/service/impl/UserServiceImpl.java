package com.infosys.smartwater.service.impl;

import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.dto.response.UserResponse;
import com.infosys.smartwater.entity.User;
import com.infosys.smartwater.entity.enums.Role;
import com.infosys.smartwater.exception.InvalidOperationException;
import com.infosys.smartwater.exception.ResourceNotFoundException;
import com.infosys.smartwater.mapper.UserMapper;
import com.infosys.smartwater.repository.HouseholdRepository;
import com.infosys.smartwater.repository.UserRepository;
import com.infosys.smartwater.service.UserService;
import com.infosys.smartwater.utils.PageableUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of {@link UserService}.
 *
 * <p>Registration and JWT authentication are handled by {@code AuthService} (Module 4).
 * This service covers ADMIN-facing user management operations only.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository      userRepository;
    private final HouseholdRepository householdRepository;
    private final UserMapper          userMapper;

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        return userMapper.toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, sortDir);
        return PagedResponse.from(
                userRepository.findAll(pageable).map(userMapper::toResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getUsersByRole(Role role, int page, int size) {
        Pageable pageable = PageableUtils.createPageable(page, size, "username", "asc");
        return PagedResponse.from(
                userRepository.findByRole(role, pageable).map(userMapper::toResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> searchUsers(String keyword, int page, int size) {
        Pageable pageable = PageableUtils.createPageable(page, size, "username", "asc");
        return PagedResponse.from(
                userRepository.searchByKeyword(keyword, pageable).map(userMapper::toResponse)
        );
    }

    // -------------------------------------------------------------------------
    // Enable / Disable
    // -------------------------------------------------------------------------

    @Override
    public UserResponse enableUser(UUID id) {
        User user = findById(id);
        user.setIsEnabled(true);
        log.info("User '{}' (id={}) enabled.", user.getEmail(), id);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse disableUser(UUID id) {
        User user = findById(id);
        user.setIsEnabled(false);
        log.info("User '{}' (id={}) disabled.", user.getEmail(), id);
        return userMapper.toResponse(userRepository.save(user));
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Override
    public void deleteUser(UUID id) {
        log.info("Deleting user id={}", id);
        User user = findById(id);

        // Block deletion if user is linked to a household
        if (householdRepository.existsByUserId(id)) {
            throw new InvalidOperationException(
                    String.format("Cannot delete user '%s' — they are linked to a household. " +
                                  "Remove the household user link first.", user.getEmail()));
        }

        userRepository.delete(user);
        log.info("User '{}' (id={}) deleted.", user.getEmail(), id);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}
