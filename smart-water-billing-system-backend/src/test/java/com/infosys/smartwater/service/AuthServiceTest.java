package com.infosys.smartwater.service;

import com.infosys.smartwater.config.JwtProperties;
import com.infosys.smartwater.dto.request.LoginRequest;
import com.infosys.smartwater.dto.request.UserRegistrationRequest;
import com.infosys.smartwater.dto.response.AuthResponse;
import com.infosys.smartwater.entity.User;
import com.infosys.smartwater.entity.enums.ApprovalStatus;
import com.infosys.smartwater.entity.enums.Role;
import com.infosys.smartwater.exception.DuplicateResourceException;
import com.infosys.smartwater.exception.InvalidOperationException;
import com.infosys.smartwater.repository.UserRepository;
import com.infosys.smartwater.service.impl.AuthServiceImpl;
import com.infosys.smartwater.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRegistrationRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private UUID userId;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        registerRequest = UserRegistrationRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("Secret@123")
                .role(Role.BUILDING_OWNER)
                .build();

        loginRequest = LoginRequest.builder()
                .email("john@example.com")
                .password("Secret@123")
                .build();

        user = User.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("encoded_password")
                .role(Role.BUILDING_OWNER)
                .isEnabled(true)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();
        user.setId(userId);

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("john@example.com")
                .password("encoded_password")
                .roles("BUILDING_OWNER")
                .build();
    }

    @Test
    @DisplayName("Register Building Owner - Success (Status PENDING)")
    void register_Success() {
        given(userRepository.existsByEmail("john@example.com")).willReturn(false);
        given(userRepository.existsByUsername("john_doe")).willReturn(false);
        given(passwordEncoder.encode("Secret@123")).willReturn("encoded_password");
        given(userRepository.save(any(User.class))).willReturn(user);

        AuthResponse result = authService.register(registerRequest);

        assertNotNull(result);
        assertNull(result.getAccessToken());
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Register User - Duplicate Email Throws Exception")
    void register_DuplicateEmail_ThrowsException() {
        given(userRepository.existsByEmail("john@example.com")).willReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
    }

    @Test
    @DisplayName("Login - Success")
    void login_Success() {
        given(userRepository.findByEmail("john@example.com")).willReturn(Optional.of(user));
        given(userDetailsService.loadUserByUsername("john@example.com")).willReturn(userDetails);
        given(jwtUtils.generateToken(anyMap(), eq(userDetails))).willReturn("mock_jwt_token");
        given(jwtProperties.getExpiration()).willReturn(86400000L);

        AuthResponse result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("mock_jwt_token", result.getAccessToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Login - Invalid Credentials Throws Exception")
    void login_InvalidCredentials_ThrowsException() {
        User pendingUser = User.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("encoded_password")
                .role(Role.BUILDING_OWNER)
                .isEnabled(true)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();
        pendingUser.setId(userId);

        given(userRepository.findByEmail("john@example.com")).willReturn(Optional.of(pendingUser));
        given(authenticationManager.authenticate(any())).willThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(InvalidOperationException.class, () -> authService.login(loginRequest));
    }
}
