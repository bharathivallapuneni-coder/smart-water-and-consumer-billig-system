package com.infosys.smartwater.controller;

import com.infosys.smartwater.dto.response.ApiResponse;
import com.infosys.smartwater.entity.Notification;
import com.infosys.smartwater.repository.NotificationRepository;
import com.infosys.smartwater.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/notifications", "/api/notifications"})
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notifications and water usage alert history")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final AuthService authService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get notifications for logged-in user")
    public ResponseEntity<ApiResponse<List<Notification>>> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID userId = authService.getCurrentUser().getId();
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications.getContent(), HttpStatus.OK.value()));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", HttpStatus.OK.value()));
    }
}
