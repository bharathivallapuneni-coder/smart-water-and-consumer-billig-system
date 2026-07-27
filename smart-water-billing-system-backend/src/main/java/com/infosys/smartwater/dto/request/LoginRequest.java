package com.infosys.smartwater.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user login ({@code POST /api/auth/login}).
 *
 * <p>Authenticates the user by email and password.
 * On success, the endpoint returns an {@code AuthResponse} containing
 * the JWT access token, user details, and token expiry.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for user authentication (login)")
public class LoginRequest {

    @Schema(description = "Registered email address used for login",
            example = "rajesh@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @Schema(description = "Account password",
            example = "SecurePass@123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    private String password;
}
