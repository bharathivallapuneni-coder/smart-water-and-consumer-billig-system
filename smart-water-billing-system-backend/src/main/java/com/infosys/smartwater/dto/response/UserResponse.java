package com.infosys.smartwater.dto.response;

import com.infosys.smartwater.entity.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for {@code User} resources.
 *
 * <p><b>Security:</b> The {@code password} field is <em>never</em> included
 * in this DTO. Callers cannot retrieve passwords through any API endpoint.
 *
 * <p>Includes an optional flattened household summary for RESIDENT users.
 * ADMIN users will have {@code householdId} and {@code householdNumber} as {@code null}.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User resource representation — password is never included")
public class UserResponse {

    @Schema(description = "Unique identifier of the user",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "User's login username", example = "rajesh_kumar")
    private String username;

    @Schema(description = "User's email address", example = "rajesh@example.com")
    private String email;

    @Schema(description = "User's contact phone number", example = "+919876543210",
            nullable = true)
    private String phone;

    @Schema(description = "User's assigned role", example = "RESIDENT")
    private Role role;

    @Schema(description = "Whether the user account is currently enabled", example = "true")
    private Boolean isEnabled;

    @Schema(description = "Approval status of the user application", example = "APPROVED")
    private com.infosys.smartwater.entity.enums.ApprovalStatus approvalStatus;

    // Flattened household summary (RESIDENT only — null for ADMIN)
    @Schema(description = "UUID of the linked household — null for ADMIN users",
            nullable = true, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID householdId;

    @Schema(description = "Household number of the linked household — null for ADMIN users",
            nullable = true, example = "APT-001-F02-U04")
    private String householdNumber;

    @Schema(description = "Timestamp of account creation", example = "2026-01-15T09:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of last account update", example = "2026-07-23T21:00:00")
    private LocalDateTime updatedAt;
}
