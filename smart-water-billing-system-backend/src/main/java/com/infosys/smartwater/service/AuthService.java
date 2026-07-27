package com.infosys.smartwater.service;

import com.infosys.smartwater.dto.request.LoginRequest;
import com.infosys.smartwater.dto.request.UserRegistrationRequest;
import com.infosys.smartwater.dto.response.AuthResponse;
import com.infosys.smartwater.dto.response.UserResponse;

/**
 * Service interface for User Registration, Authentication (Login), and Context Retrieval.
 */
public interface AuthService {

    /**
     * Registers a new user (Resident or Admin).
     *
     * @param request the user registration details
     * @return AuthResponse containing the JWT token and registered user info
     */
    AuthResponse register(UserRegistrationRequest request);

    /**
     * Authenticates user credentials and generates a JWT access token.
     *
     * @param request the login request (email & password)
     * @return AuthResponse containing the JWT token and authenticated user info
     */
    AuthResponse login(LoginRequest request);

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @return UserResponse of the currently authenticated user
     */
    UserResponse getCurrentUser();
}
