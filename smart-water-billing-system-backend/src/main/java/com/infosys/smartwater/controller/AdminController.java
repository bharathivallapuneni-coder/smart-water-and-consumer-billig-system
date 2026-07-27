package com.infosys.smartwater.controller;

import com.infosys.smartwater.dto.response.ApiResponse;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.dto.response.UserResponse;
import com.infosys.smartwater.entity.User;
import com.infosys.smartwater.entity.enums.ApprovalStatus;
import com.infosys.smartwater.exception.ResourceNotFoundException;
import com.infosys.smartwater.mapper.UserMapper;
import com.infosys.smartwater.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Superadmin system management and Building Owner registration application reviews.
 */
@Slf4j
@RestController
@RequestMapping({"/api/v1/admin", "/api/admin"})
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPERADMIN')")
@Tag(name = "Admin Management", description = "Superadmin endpoints for approving/rejecting Building Owner applications")
public class AdminController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping("/pending-owners")
    @Operation(summary = "Get paginated list of pending Building Owner registration applications [SUPERADMIN]")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getPendingOwnerApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> pendingUsers = userRepository.findByApprovalStatus(ApprovalStatus.PENDING, pageable);

        List<UserResponse> content = pendingUsers.getContent().stream()
                .map(userMapper::toResponse)
                .toList();

        PagedResponse<UserResponse> pagedResponse = PagedResponse.<UserResponse>builder()
                .content(content)
                .page(pendingUsers.getNumber())
                .size(pendingUsers.getSize())
                .totalElements(pendingUsers.getTotalElements())
                .totalPages(pendingUsers.getTotalPages())
                .last(pendingUsers.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Pending Building Owner applications retrieved successfully", pagedResponse, HttpStatus.OK.value()));
    }

    @PatchMapping("/approve-owner/{userId}")
    @Transactional
    @Operation(summary = "Approve a Building Owner registration application [SUPERADMIN]")
    public ResponseEntity<ApiResponse<UserResponse>> approveOwnerApplication(@PathVariable UUID userId) {
        log.info("Superadmin approving Building Owner application for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setIsEnabled(true);

        User updatedUser = userRepository.save(user);
        log.info("Building Owner application approved successfully — email: '{}'", updatedUser.getEmail());

        return ResponseEntity.ok(ApiResponse.success("Building Owner application approved successfully", userMapper.toResponse(updatedUser), HttpStatus.OK.value()));
    }

    @PatchMapping("/reject-owner/{userId}")
    @Transactional
    @Operation(summary = "Reject a Building Owner registration application [SUPERADMIN]")
    public ResponseEntity<ApiResponse<UserResponse>> rejectOwnerApplication(@PathVariable UUID userId) {
        log.info("Superadmin rejecting Building Owner application for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setApprovalStatus(ApprovalStatus.REJECTED);
        user.setIsEnabled(false);

        User updatedUser = userRepository.save(user);
        log.info("Building Owner application rejected — email: '{}'", updatedUser.getEmail());

        return ResponseEntity.ok(ApiResponse.success("Building Owner application rejected", userMapper.toResponse(updatedUser), HttpStatus.OK.value()));
    }
}
