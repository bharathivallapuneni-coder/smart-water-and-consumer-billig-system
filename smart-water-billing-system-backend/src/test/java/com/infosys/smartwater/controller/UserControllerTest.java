package com.infosys.smartwater.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infosys.smartwater.dto.response.ApiResponse;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.dto.response.UserResponse;
import com.infosys.smartwater.entity.enums.Role;
import com.infosys.smartwater.exception.GlobalExceptionHandler;
import com.infosys.smartwater.exception.ResourceNotFoundException;
import com.infosys.smartwater.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController REST Endpoint Unit Tests")
class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UUID userId;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        userId = UUID.randomUUID();

        userResponse = UserResponse.builder()
                .id(userId)
                .username("johndoe")
                .email("john@example.com")
                .role(Role.RESIDENT)
                .isEnabled(true)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - Success (200 OK)")
    void getUserById_Success() throws Exception {
        when(userService.getUserById(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", is("johndoe")));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - Not Found (404)")
    void getUserById_NotFound() throws Exception {
        when(userService.getUserById(userId))
                .thenThrow(new ResourceNotFoundException("User", "id", userId));

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.statusCode", is(404)));
    }

    @Test
    @DisplayName("GET /api/v1/users - Success (Paged 200 OK)")
    void getAllUsers_Success() throws Exception {
        PagedResponse<UserResponse> pagedResponse = PagedResponse.<UserResponse>builder()
                .content(List.of(userResponse))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(userService.getAllUsers(0, 10, "username", "asc")).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content[0].username", is("johndoe")));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/enable - Success (200 OK)")
    void enableUser_Success() throws Exception {
        when(userService.enableUser(userId)).thenReturn(userResponse);

        mockMvc.perform(patch("/api/v1/users/{id}/enable", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User enabled successfully")));

        verify(userService, times(1)).enableUser(userId);
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} - Success (200 OK)")
    void deleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User deleted successfully")));

        verify(userService, times(1)).deleteUser(userId);
    }
}
