package com.infosys.smartwater.controller;

import com.infosys.smartwater.dto.response.AlertResponse;
import com.infosys.smartwater.entity.Notification;
import com.infosys.smartwater.entity.User;
import com.infosys.smartwater.entity.enums.Severity;
import com.infosys.smartwater.repository.NotificationRepository;
import com.infosys.smartwater.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/resident/alerts")
@RequiredArgsConstructor
public class ResidentAlertController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser(UserDetails userDetails) {
        String identifier = userDetails.getUsername();
        return userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier)
                        .orElseThrow(() -> new RuntimeException("Authenticated user not found")));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RESIDENT', 'BUILDING_OWNER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<List<AlertResponse>> getResidentAlerts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly
    ) {
        User user = getAuthenticatedUser(userDetails);

        List<Notification> notifications;
        if (severity != null && !severity.isBlank()) {
            try {
                Severity sev = Severity.valueOf(severity.toUpperCase());
                notifications = notificationRepository.findByUserIdAndSeverityOrderByCreatedAtDesc(user.getId(), sev);
            } catch (IllegalArgumentException e) {
                notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
            }
        } else if (unreadOnly) {
            notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        } else {
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        }

        List<AlertResponse> responseList = notifications.stream()
                .map(AlertResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('RESIDENT', 'BUILDING_OWNER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<List<AlertResponse>> getUnreadAlerts(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getAuthenticatedUser(userDetails);

        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        List<AlertResponse> responseList = unread.stream()
                .map(AlertResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('RESIDENT', 'BUILDING_OWNER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Long>> getUnreadAlertCount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getAuthenticatedUser(userDetails);

        long count = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('RESIDENT', 'BUILDING_OWNER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<AlertResponse> markAlertAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = getAuthenticatedUser(userDetails);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found with ID: " + id));

        // Strict authorization check: Resident can only modify their own alerts
        if (!notification.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);

        return ResponseEntity.ok(AlertResponse.fromEntity(notification));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('RESIDENT', 'BUILDING_OWNER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<AlertResponse> resolveAlert(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = getAuthenticatedUser(userDetails);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found with ID: " + id));

        // Strict authorization check: Resident can only modify their own alerts
        if (!notification.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        notification.setIsRead(true);
        notification.setIsResolved(true);
        notification.setResolvedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        return ResponseEntity.ok(AlertResponse.fromEntity(notification));
    }
}

