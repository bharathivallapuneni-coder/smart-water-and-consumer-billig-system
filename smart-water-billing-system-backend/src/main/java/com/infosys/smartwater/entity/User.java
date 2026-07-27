package com.infosys.smartwater.entity;

import com.infosys.smartwater.entity.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Represents a system user — either an {@link Role#ADMIN} member of staff
 * or a {@link Role#RESIDENT} account holder linked to a household.
 *
 * <p><b>Relationship summary:</b>
 * <pre>
 *   User  1 ──────────── 0..1  Household   (back-reference; Household owns the FK)
 * </pre>
 *
 * <p><b>Security note:</b> The {@code password} field always stores a BCrypt hash.
 * Raw passwords must never be persisted. The {@code UserDetails} adapter for
 * Spring Security will be implemented in the Security module (Module 4).
 *
 * <p><b>@EqualsAndHashCode:</b> Based on {@code email} (business key),
 * which is guaranteed unique and stable. Using the surrogate UUID {@code id}
 * would cause issues with transient (pre-persist) entities in sets/maps.
 */
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email",    columnList = "email"),
                @Index(name = "idx_users_username", columnList = "username"),
                @Index(name = "idx_users_role",     columnList = "role")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "household")
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class User extends BaseEntity {

    // -------------------------------------------------------------------------
    // Core fields
    // -------------------------------------------------------------------------

    /**
     * Unique login username (3–50 characters).
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Unique email address used for login and notifications.
     * Also serves as the business key for equality checks.
     */
    @EqualsAndHashCode.Include
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * BCrypt-hashed password. Never expose this field in API responses.
     * The service layer must hash the raw password before assigning it here.
     */
    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * Contact phone number (international format, 10–15 digits).
     * Optional; used for SMS notifications (future feature).
     */
    @Pattern(
            regexp  = "^[+]?[0-9]{10,15}$",
            message = "Phone number must be 10 to 15 digits, optionally prefixed with +"
    )
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * Assigned role governing API access.
     * Defaults to {@link Role#RESIDENT}.
     */
    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.RESIDENT;

    /**
     * Account enabled flag. Disabled accounts cannot authenticate.
     * Used for soft-suspension without data deletion.
     */
    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;

    @NotNull(message = "Approval status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'APPROVED'")
    @Builder.Default
    private com.infosys.smartwater.entity.enums.ApprovalStatus approvalStatus = com.infosys.smartwater.entity.enums.ApprovalStatus.APPROVED;

    // -------------------------------------------------------------------------
    // Relationships
    // -------------------------------------------------------------------------

    /**
     * Back-reference to the household this user is assigned to.
     * {@code null} for ADMIN / BUILDING_OWNER / SUPERADMIN users who are not residents.
     * The FK ({@code user_id}) is owned by the {@link Household} table.
     */
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Household household;

    // -------------------------------------------------------------------------
    // Convenience helpers
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if this user has the {@link Role#SUPERADMIN} role.
     */
    public boolean isSuperAdmin() {
        return Role.SUPERADMIN.equals(this.role);
    }

    /**
     * Returns {@code true} if this user has the {@link Role#BUILDING_OWNER} or legacy {@link Role#ADMIN} role.
     */
    public boolean isBuildingOwner() {
        return Role.BUILDING_OWNER.equals(this.role) || Role.ADMIN.equals(this.role);
    }

    /**
     * Returns {@code true} if this user has the {@link Role#RESIDENT} role.
     */
    public boolean isResident() {
        return Role.RESIDENT.equals(this.role);
    }
}
