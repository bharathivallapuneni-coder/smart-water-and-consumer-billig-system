package com.infosys.smartwater.controller;

import com.infosys.smartwater.dto.request.*;
import com.infosys.smartwater.dto.response.ApiResponse;
import com.infosys.smartwater.dto.response.AuthResponse;
import com.infosys.smartwater.dto.response.UserResponse;
import com.infosys.smartwater.service.AuthService;
import com.infosys.smartwater.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST Controller providing User Registration, Login, Profile, and Password Reset endpoints.
 */
@RestController
@RequestMapping({"/api/v1/auth", "/api/auth"})
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, authentication (JWT), password reset, and user context")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user account (Resident / Admin)")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody UserRegistrationRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(
                ApiResponse.success("User registered successfully", response, HttpStatus.CREATED.value()),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user credentials and receive JWT access token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response, HttpStatus.OK.value()));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Current user profile retrieved", response, HttpStatus.OK.value()));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Initiate password reset request by sending token/OTP to email")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String msg = passwordResetService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(msg, msg, HttpStatus.OK.value()));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using OTP / Token received via email")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("New password and confirm password do not match", HttpStatus.BAD_REQUEST.value()));
        }
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully. You can now log in with your new password.", HttpStatus.OK.value()));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for logged-in user")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("New password and confirm password do not match", HttpStatus.BAD_REQUEST.value()));
        }
        UserResponse currentUser = authService.getCurrentUser();
        passwordResetService.changePassword(currentUser.getId(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", HttpStatus.OK.value()));
    }
}
