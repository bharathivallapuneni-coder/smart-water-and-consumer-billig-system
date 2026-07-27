package com.infosys.smartwater.controller;

import com.infosys.smartwater.dto.response.ApiResponse;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.dto.response.UserResponse;
import com.infosys.smartwater.entity.enums.Role;
import com.infosys.smartwater.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for User Account administration [ADMIN].
 */
@RestController
@RequestMapping({"/api/v1/users", "/api/users"})
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
@Tag(name = "Users", description = "Endpoints for user account management")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "Get user details by ID [ADMIN]")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response, HttpStatus.OK.value()));
    }

    @GetMapping
    @Operation(summary = "Get paginated list of all users [ADMIN]")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        PagedResponse<UserResponse> response = userService.getAllUsers(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response, HttpStatus.OK.value()));
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Filter users by role [ADMIN]")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getUsersByRole(
            @PathVariable Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<UserResponse> response = userService.getUsersByRole(role, page, size);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response, HttpStatus.OK.value()));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by keyword [ADMIN]")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<UserResponse> response = userService.searchUsers(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", response, HttpStatus.OK.value()));
    }

    @PatchMapping("/{id}/enable")
    @Operation(summary = "Enable user account [ADMIN]")
    public ResponseEntity<ApiResponse<UserResponse>> enableUser(@PathVariable UUID id) {
        UserResponse response = userService.enableUser(id);
        return ResponseEntity.ok(ApiResponse.success("User enabled successfully", response, HttpStatus.OK.value()));
    }

    @PatchMapping("/{id}/disable")
    @Operation(summary = "Disable user account [ADMIN]")
    public ResponseEntity<ApiResponse<UserResponse>> disableUser(@PathVariable UUID id) {
        UserResponse response = userService.disableUser(id);
        return ResponseEntity.ok(ApiResponse.success("User disabled successfully", response, HttpStatus.OK.value()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user account [ADMIN]")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", HttpStatus.OK.value()));
    }
}
