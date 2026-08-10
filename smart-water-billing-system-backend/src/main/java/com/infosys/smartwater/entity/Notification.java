package com.infosys.smartwater.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Represents in-app alert/notification sent to Resident or Building Owner.
 */
@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_user", columnList = "user_id"),
                @Index(name = "idx_notifications_building", columnList = "building_id"),
                @Index(name = "idx_notifications_read", columnList = "is_read")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    private Apartment apartment;

    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @NotBlank(message = "Message is required")
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @NotBlank(message = "Notification type is required")
    @Column(name = "notification_type", nullable = false, length = 50)
    @Builder.Default
    private String notificationType = "INFO"; // HIGH_USAGE, LEAK_ALERT, INVOICE_GENERATED, CYCLE_UPDATE, GENERAL

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;
}
