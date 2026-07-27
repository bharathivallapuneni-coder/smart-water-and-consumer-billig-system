package com.infosys.smartwater.dto.response;

import com.infosys.smartwater.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned by the login ({@code POST /api/auth/login}) endpoint.
 *
 * <p>Contains the JWT access token, token metadata, and the authenticated
 * user's basic profile to avoid requiring a separate profile fetch after login.
 *
 * <p>Example response:
 * <pre>
 * {
 *   "accessToken"  : "eyJhbGciOiJIUzI1NiIs...",
 *   "tokenType"    : "Bearer",
 *   "expiresIn"    : 86400000,
 *   "userId"       : "3fa85f64-5717-4562-b3fc-2c963f66afa6",
 *   "username"     : "john_doe",
 *   "email"        : "john@example.com",
 *   "role"         : "RESIDENT"
 * }
 * </pre>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JWT authentication response returned on successful login")
public class AuthResponse {

    @Schema(description = "JWT access token to include in the Authorization header",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Token type prefix for the Authorization header",
            example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Token validity duration in milliseconds",
            example = "86400000")
    private long expiresIn;

    @Schema(description = "Authenticated user's UUID",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String userId;

    @Schema(description = "Authenticated user's username",
            example = "john_doe")
    private String username;

    @Schema(description = "Authenticated user's email address",
            example = "john@example.com")
    private String email;

    @Schema(description = "Authenticated user's assigned role",
            example = "RESIDENT")
    private Role role;
}
