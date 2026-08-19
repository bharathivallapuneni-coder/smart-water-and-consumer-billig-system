package com.infosys.smartwater.entity;

import com.infosys.smartwater.entity.enums.AlertType;
import com.infosys.smartwater.entity.enums.Severity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents in-app alert/notification sent to Resident or Building Owner.
 */
@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_user", columnList = "user_id"),
                @Index(name = "idx_notifications_building", columnList = "building_id"),
                @Index(name = "idx_notifications_household", columnList = "household_id"),
                @Index(name = "idx_notifications_read", columnList = "is_read"),
                @Index(name = "idx_notifications_resolved", columnList = "is_resolved")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id")
    private Household household;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_cycle_id")
    private BillingCycle billingCycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 50)
    @Builder.Default
    private AlertType alertType = AlertType.HIGH_CONSUMPTION;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    @Builder.Default
    private Severity severity = Severity.WARNING;

    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @NotBlank(message = "Message is required")
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @NotBlank(message = "Notification type is required")
    @Column(name = "notification_type", nullable = false, length = 50)
    @Builder.Default
    private String notificationType = "INFO";

    @Column(name = "tariff_tier", length = 100)
    private String tariffTier;

    @Column(name = "current_consumption", precision = 10, scale = 2)
    private BigDecimal currentConsumption;

    @Column(name = "average_consumption", precision = 10, scale = 2)
    private BigDecimal averageConsumption;

    @Column(name = "standard_deviation", precision = 10, scale = 2)
    private BigDecimal standardDeviation;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "is_resolved", nullable = false)
    @Builder.Default
    private Boolean isResolved = false;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
