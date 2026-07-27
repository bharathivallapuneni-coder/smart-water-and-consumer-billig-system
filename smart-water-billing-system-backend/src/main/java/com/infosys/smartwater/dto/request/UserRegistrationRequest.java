package com.infosys.smartwater.dto.request;

import com.infosys.smartwater.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user registration ({@code POST /api/auth/register}).
 *
 * <p>Password requirements enforced by bean validation:
 * <ul>
 *   <li>Minimum 8 characters, maximum 100 characters</li>
 *   <li>At least one uppercase letter</li>
 *   <li>At least one lowercase letter</li>
 *   <li>At least one digit</li>
 *   <li>At least one special character: {@code @#$%^&+=!}</li>
 * </ul>
 *
 * <p>The {@code role} field is optional. When omitted it defaults to
 * {@link Role#RESIDENT}. Only an authenticated ADMIN can register another ADMIN.
 * This business rule is enforced in the service layer.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for new user registration")
public class UserRegistrationRequest {

    @Schema(description = "Unique login username (3–50 characters)",
            example = "rajesh_kumar",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(
            regexp  = "^[a-zA-Z0-9._-]{3,50}$",
            message = "Username may only contain letters, digits, dots, underscores, and hyphens"
    )
    private String username;

    @Schema(description = "Valid email address used for login and notifications",
            example = "rajesh@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Schema(description = "Password (8–100 chars; must include upper, lower, digit, special char)",
            example = "SecurePass@123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
            regexp  = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@#$%^&+=!)"
    )
    private String password;

    @Schema(description = "Contact phone number (optional, 10–15 digits)",
            example = "+919876543210")
    @Pattern(
            regexp  = "^[+]?[0-9]{10,15}$",
            message = "Phone number must be 10 to 15 digits, optionally prefixed with +"
    )
    private String phone;

    @Schema(description = "User role — defaults to RESIDENT when not specified",
            example = "RESIDENT",
            nullable = true)
    private Role role;
}
