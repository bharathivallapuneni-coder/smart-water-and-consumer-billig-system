package com.infosys.smartwater.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infosys.smartwater.dto.request.HouseholdRequest;
import com.infosys.smartwater.dto.response.HouseholdResponse;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.exception.GlobalExceptionHandler;
import com.infosys.smartwater.exception.ResourceNotFoundException;
import com.infosys.smartwater.service.HouseholdService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HouseholdController REST Endpoint Unit Tests")
class HouseholdControllerTest {

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        @Mock
        private HouseholdService householdService;

        @InjectMocks
        private HouseholdController householdController;

        private UUID householdId;
        private UUID apartmentId;
        private UUID userId;
        private HouseholdRequest householdRequest;
        private HouseholdResponse householdResponse;

        @BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());

                mockMvc = MockMvcBuilders
                                .standaloneSetup(householdController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();

                householdId = UUID.randomUUID();
                apartmentId = UUID.randomUUID();
                userId = UUID.randomUUID();

                householdRequest = HouseholdRequest.builder()
                                .householdNumber("HH-101-A")
                                .ownerName("John Resident")
                                .contactPhone("+19876543210")
                                .apartmentId(apartmentId)
                                .build();

                householdResponse = HouseholdResponse.builder()
                                .id(householdId)
                                .householdNumber("HH-101-A")
                                .ownerName("John Resident")
                                .contactPhone("+19876543210")
                                .apartmentId(apartmentId)
                                .apartmentNumber("APT-101")
                                .isActive(true)
                                .build();
        }

        @Test
        @DisplayName("POST /api/v1/households - Success (201 Created)")
        void createHousehold_Success() throws Exception {
                when(householdService.createHousehold(any(HouseholdRequest.class))).thenReturn(householdResponse);

                mockMvc.perform(post("/api/v1/households")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(householdRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.statusCode", is(201)))
                                .andExpect(jsonPath("$.data.householdNumber", is("HH-101-A")));

                verify(householdService, times(1)).createHousehold(any(HouseholdRequest.class));
        }

        @Test
        @DisplayName("GET /api/v1/households/{id} - Success (200 OK)")
        void getHouseholdById_Success() throws Exception {
                when(householdService.getHouseholdById(householdId)).thenReturn(householdResponse);

                mockMvc.perform(get("/api/v1/households/{id}", householdId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.data.householdNumber", is("HH-101-A")));

                verify(householdService, times(1)).getHouseholdById(householdId);
        }

        @Test
        @DisplayName("GET /api/v1/households/{id} - Not Found (404)")
        void getHouseholdById_NotFound() throws Exception {
                when(householdService.getHouseholdById(householdId))
                                .thenThrow(new ResourceNotFoundException("Household", "id", householdId));

                mockMvc.perform(get("/api/v1/households/{id}", householdId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success", is(false)))
                                .andExpect(jsonPath("$.statusCode", is(404)));
        }

        @Test
        @DisplayName("GET /api/v1/households - Success (Paged 200 OK)")
        void getAllHouseholds_Success() throws Exception {
                PagedResponse<HouseholdResponse> pagedResponse = PagedResponse.<HouseholdResponse>builder()
                                .content(List.of(householdResponse))
                                .page(0)
                                .size(10)
                                .totalElements(1)
                                .totalPages(1)
                                .last(true)
                                .build();

                when(householdService.getAllHouseholds(0, 10, "householdNumber", "asc")).thenReturn(pagedResponse);

                mockMvc.perform(get("/api/v1/households")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.data.content[0].householdNumber", is("HH-101-A")));
        }

        @Test
        @DisplayName("PATCH /api/v1/households/{id}/activate - Success (200 OK)")
        void activateHousehold_Success() throws Exception {
                when(householdService.activateHousehold(householdId)).thenReturn(householdResponse);

                mockMvc.perform(patch("/api/v1/households/{id}/activate", householdId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.message", is("Household activated successfully")));

                verify(householdService, times(1)).activateHousehold(householdId);
        }

        @Test
        @DisplayName("POST /api/v1/households/{householdId}/assign-user/{userId} - Success (200 OK)")
        void assignUser_Success() throws Exception {
                when(householdService.assignUser(householdId, userId)).thenReturn(householdResponse);

                mockMvc.perform(post("/api/v1/households/{householdId}/assign-user/{userId}", householdId, userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.message", is("User assigned to household successfully")));

                verify(householdService, times(1)).assignUser(householdId, userId);
        }

        @Test
        @DisplayName("DELETE /api/v1/households/{id} - Success (200 OK)")
        void deleteHousehold_Success() throws Exception {
                doNothing().when(householdService).deleteHousehold(householdId);

                mockMvc.perform(delete("/api/v1/households/{id}", householdId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.message", is("Household deleted successfully")));

                verify(householdService, times(1)).deleteHousehold(householdId);
        }
}
