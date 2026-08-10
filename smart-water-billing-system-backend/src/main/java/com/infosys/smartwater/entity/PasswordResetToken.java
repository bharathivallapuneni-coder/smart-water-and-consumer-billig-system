package com.infosys.smartwater.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents secure time-bound reset token/OTP for password reset.
 */
@Entity
@Table(
        name = "password_reset_tokens",
        indexes = {
                @Index(name = "idx_reset_token_token", columnList = "token"),
                @Index(name = "idx_reset_token_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class PasswordResetToken extends BaseEntity {

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Token is required")
    @Column(name = "token", nullable = false, unique = true, length = 100)
    private String token;

    @NotNull(message = "Expiry date is required")
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private Boolean isUsed = false;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
