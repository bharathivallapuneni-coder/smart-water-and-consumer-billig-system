package com.infosys.smartwater.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for user login ({@code POST /api/auth/login}).
 *
 * <p>Authenticates the user by email or username and password.
 * On success, the endpoint returns an {@code AuthResponse} containing
 * the JWT access token, user details, and token expiry.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for user authentication (login)")
public class LoginRequest {

    @Schema(description = "Registered email address used for login",
            example = "superadmin@smartwater.com")
    private String email;

    @Schema(description = "Registered username used for login",
            example = "super admin")
    private String username;

    @Schema(description = "Account password",
            example = "admin123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    private String password;

    public String getEmail() {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return username;
    }

    public String getUsername() {
        if (username != null && !username.isBlank()) {
            return username;
        }
        return email;
    }
}

