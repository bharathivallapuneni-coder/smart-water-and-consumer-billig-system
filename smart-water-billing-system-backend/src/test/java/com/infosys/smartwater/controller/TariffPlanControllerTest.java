package com.infosys.smartwater.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infosys.smartwater.dto.request.TariffPlanRequest;
import com.infosys.smartwater.dto.response.TariffPlanResponse;
import com.infosys.smartwater.exception.GlobalExceptionHandler;
import com.infosys.smartwater.service.TariffPlanService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TariffPlanController REST Endpoint Unit Tests")
class TariffPlanControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TariffPlanService tariffPlanService;

    @InjectMocks
    private TariffPlanController tariffPlanController;

    private UUID planId;
    private TariffPlanRequest tariffRequest;
    private TariffPlanResponse tariffResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(tariffPlanController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        planId = UUID.randomUUID();

        tariffRequest = TariffPlanRequest.builder()
                .planName("Standard Residential Tariff")
                .ratePerUnit(new BigDecimal("15.5000"))
                .fixedCharge(new BigDecimal("50.00"))
                .minUnits(BigDecimal.ZERO)
                .effectiveFrom(LocalDate.now())
                .build();

        tariffResponse = TariffPlanResponse.builder()
                .id(planId)
                .planName("Standard Residential Tariff")
                .ratePerUnit(new BigDecimal("15.5000"))
                .fixedCharge(new BigDecimal("50.00"))
                .minUnits(BigDecimal.ZERO)
                .effectiveFrom(LocalDate.now())
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/tariff-plans - Success (201 Created)")
    void createTariffPlan_Success() throws Exception {
        when(tariffPlanService.createTariffPlan(any(TariffPlanRequest.class))).thenReturn(tariffResponse);

        mockMvc.perform(post("/api/v1/tariff-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tariffRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.data.planName", is("Standard Residential Tariff")));

        verify(tariffPlanService, times(1)).createTariffPlan(any(TariffPlanRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/tariff-plans/active - Success (200 OK)")
    void getActiveTariffPlans_Success() throws Exception {
        when(tariffPlanService.getAllActiveTariffPlans()).thenReturn(List.of(tariffResponse));

        mockMvc.perform(get("/api/v1/tariff-plans/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data[0].planName", is("Standard Residential Tariff")));

        verify(tariffPlanService, times(1)).getAllActiveTariffPlans();
    }
}
