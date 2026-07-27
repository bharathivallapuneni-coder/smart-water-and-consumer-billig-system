package com.infosys.smartwater.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infosys.smartwater.dto.request.LoginRequest;
import com.infosys.smartwater.dto.request.UserRegistrationRequest;
import com.infosys.smartwater.dto.response.AuthResponse;
import com.infosys.smartwater.dto.response.UserResponse;
import com.infosys.smartwater.entity.enums.Role;
import com.infosys.smartwater.exception.DuplicateResourceException;
import com.infosys.smartwater.exception.GlobalExceptionHandler;
import com.infosys.smartwater.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController REST Endpoint Unit Tests")
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Success (201 Created)")
    void register_Success() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("Password@123")
                .phone("+1234567890")
                .role(Role.RESIDENT)
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("mock-jwt-token")
                .tokenType("Bearer")
                .userId(UUID.randomUUID().toString())
                .username("johndoe")
                .email("john@example.com")
                .role(Role.RESIDENT)
                .build();

        when(authService.register(any(UserRegistrationRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.data.accessToken", is("mock-jwt-token")))
                .andExpect(jsonPath("$.data.username", is("johndoe")));

        verify(authService, times(1)).register(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Validation Error (400 Bad Request)")
    void register_ValidationError() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("") // Invalid blank
                .email("invalid-email") // Invalid email format
                .password("short") // Short password
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.statusCode", is(400)))
                .andExpect(jsonPath("$.message", is("Validation failed")));

        verify(authService, never()).register(any());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Duplicate Email (409 Conflict)")
    void register_DuplicateEmail() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("Password@123")
                .role(Role.RESIDENT)
                .build();

        when(authService.register(any())).thenThrow(new DuplicateResourceException("User", "email", "john@example.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.statusCode", is(409)))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Success (200 OK)")
    void login_Success() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("john@example.com")
                .password("Password@123")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("valid-jwt-token")
                .tokenType("Bearer")
                .userId(UUID.randomUUID().toString())
                .username("johndoe")
                .email("john@example.com")
                .role(Role.RESIDENT)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", is("valid-jwt-token")));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Bad Credentials (401 Unauthorized)")
    void login_BadCredentials() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("john@example.com")
                .password("WrongPassword")
                .build();

        when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.statusCode", is(401)))
                .andExpect(jsonPath("$.message", is("Invalid email or password")));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me - Success (200 OK)")
    void getCurrentUser_Success() throws Exception {
        UserResponse userResponse = UserResponse.builder()
                .id(UUID.randomUUID())
                .username("johndoe")
                .email("john@example.com")
                .role(Role.RESIDENT)
                .isEnabled(true)
                .build();

        when(authService.getCurrentUser()).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.email", is("john@example.com")));

        verify(authService, times(1)).getCurrentUser();
    }
}
