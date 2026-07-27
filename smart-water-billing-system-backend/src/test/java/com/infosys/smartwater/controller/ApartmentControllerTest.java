package com.infosys.smartwater.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infosys.smartwater.dto.request.ApartmentRequest;
import com.infosys.smartwater.dto.response.ApartmentResponse;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.exception.GlobalExceptionHandler;
import com.infosys.smartwater.exception.ResourceNotFoundException;
import com.infosys.smartwater.service.ApartmentService;
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
@DisplayName("ApartmentController REST Endpoint Unit Tests")
class ApartmentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ApartmentService apartmentService;

    @InjectMocks
    private ApartmentController apartmentController;

    private UUID apartmentId;
    private ApartmentRequest apartmentRequest;
    private ApartmentResponse apartmentResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(apartmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        apartmentId = UUID.randomUUID();

        apartmentRequest = ApartmentRequest.builder()
                .apartmentNumber("APT-101")
                .buildingName("Ocean View Towers")
                .address("123 Palm Street")
                .totalFloors(10)
                .build();

        apartmentResponse = ApartmentResponse.builder()
                .id(apartmentId)
                .apartmentNumber("APT-101")
                .buildingName("Ocean View Towers")
                .address("123 Palm Street")
                .totalFloors(10)
                .totalHouseholds(40)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/apartments - Success (201 Created)")
    void createApartment_Success() throws Exception {
        when(apartmentService.createApartment(any(ApartmentRequest.class))).thenReturn(apartmentResponse);

        mockMvc.perform(post("/api/v1/apartments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apartmentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.data.apartmentNumber", is("APT-101")))
                .andExpect(jsonPath("$.data.buildingName", is("Ocean View Towers")));

        verify(apartmentService, times(1)).createApartment(any(ApartmentRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/apartments - Success with totalUnits field alias")
    void createApartment_WithTotalUnitsAlias_Success() throws Exception {
        when(apartmentService.createApartment(any(ApartmentRequest.class))).thenReturn(apartmentResponse);

        String jsonWithTotalUnits = """
                {
                  "apartmentNumber": "APT-101",
                  "buildingName": "Green Valley Towers",
                  "address": "123 Main Street, Sector 5",
                  "totalUnits": 50
                }
                """;

        mockMvc.perform(post("/api/v1/apartments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithTotalUnits))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.statusCode", is(201)));

        verify(apartmentService, times(1)).createApartment(any(ApartmentRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/apartments - Missing totalFloors returns 400 Bad Request")
    void createApartment_MissingTotalFloors_ValidationFailure() throws Exception {
        String invalidJson = """
                {
                  "apartmentNumber": "APT-101",
                  "buildingName": "Green Valley Towers",
                  "address": "123 Main Street, Sector 5"
                }
                """;

        mockMvc.perform(post("/api/v1/apartments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.statusCode", is(400)))
                .andExpect(jsonPath("$.data.totalFloors", is("Total floors is required")));
    }

    @Test
    @DisplayName("GET /api/v1/apartments/{id} - Success (200 OK)")
    void getApartmentById_Success() throws Exception {
        when(apartmentService.getApartmentById(apartmentId)).thenReturn(apartmentResponse);

        mockMvc.perform(get("/api/v1/apartments/{id}", apartmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(apartmentId.toString())))
                .andExpect(jsonPath("$.data.apartmentNumber", is("APT-101")));

        verify(apartmentService, times(1)).getApartmentById(apartmentId);
    }

    @Test
    @DisplayName("GET /api/v1/apartments/{id} - Not Found (404 Not Found)")
    void getApartmentById_NotFound() throws Exception {
        when(apartmentService.getApartmentById(apartmentId))
                .thenThrow(new ResourceNotFoundException("Apartment", "id", apartmentId));

        mockMvc.perform(get("/api/v1/apartments/{id}", apartmentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.statusCode", is(404)))
                .andExpect(jsonPath("$.message", containsString("Apartment not found")));
    }

    @Test
    @DisplayName("GET /api/v1/apartments - Success (Paged 200 OK)")
    void getAllApartments_Success() throws Exception {
        PagedResponse<ApartmentResponse> pagedResponse = PagedResponse.<ApartmentResponse>builder()
                .content(List.of(apartmentResponse))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(apartmentService.getAllApartments(0, 10, "apartmentNumber", "asc")).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/apartments")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "apartmentNumber")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content[0].apartmentNumber", is("APT-101")))
                .andExpect(jsonPath("$.data.totalElements", is(1)));

        verify(apartmentService, times(1)).getAllApartments(0, 10, "apartmentNumber", "asc");
    }

    @Test
    @DisplayName("PUT /api/v1/apartments/{id} - Success (200 OK)")
    void updateApartment_Success() throws Exception {
        when(apartmentService.updateApartment(eq(apartmentId), any(ApartmentRequest.class))).thenReturn(apartmentResponse);

        mockMvc.perform(put("/api/v1/apartments/{id}", apartmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apartmentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.apartmentNumber", is("APT-101")));

        verify(apartmentService, times(1)).updateApartment(eq(apartmentId), any(ApartmentRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/apartments/{id} - Success (200 OK)")
    void deleteApartment_Success() throws Exception {
        doNothing().when(apartmentService).deleteApartment(apartmentId);

        mockMvc.perform(delete("/api/v1/apartments/{id}", apartmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Apartment deleted successfully")));

        verify(apartmentService, times(1)).deleteApartment(apartmentId);
    }
}
