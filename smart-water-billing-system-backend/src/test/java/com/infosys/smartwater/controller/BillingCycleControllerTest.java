package com.infosys.smartwater.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infosys.smartwater.dto.request.BillingCycleRequest;
import com.infosys.smartwater.dto.request.UpdateBillingStatusRequest;
import com.infosys.smartwater.dto.response.BillingCycleResponse;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.entity.enums.BillingStatus;
import com.infosys.smartwater.exception.GlobalExceptionHandler;
import com.infosys.smartwater.service.BillingCycleService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BillingCycleController REST Endpoint Unit Tests")
class BillingCycleControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private BillingCycleService billingCycleService;

    @InjectMocks
    private BillingCycleController billingCycleController;

    private UUID householdId;
    private UUID tariffPlanId;
    private UUID billId;
    private BillingCycleRequest billingRequest;
    private BillingCycleResponse billingResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(billingCycleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        householdId = UUID.randomUUID();
        tariffPlanId = UUID.randomUUID();
        billId = UUID.randomUUID();

        billingRequest = BillingCycleRequest.builder()
                .householdId(householdId)
                .tariffPlanId(tariffPlanId)
                .billingMonth(7)
                .billingYear(2026)
                .dueDate(LocalDate.now().plusDays(15))
                .build();

        billingResponse = BillingCycleResponse.builder()
                .id(billId)
                .householdId(householdId)
                .householdNumber("HH-101-A")
                .billingMonth(7)
                .billingYear(2026)
                .totalUnitsConsumed(new BigDecimal("100.00"))
                .totalAmount(new BigDecimal("1600.00"))
                .status(BillingStatus.PENDING)
                .dueDate(LocalDate.now().plusDays(15))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/billing-cycles/generate - Success (201 Created)")
    void generateBillingCycle_Success() throws Exception {
        when(billingCycleService.generateBillingCycle(any(BillingCycleRequest.class))).thenReturn(billingResponse);

        mockMvc.perform(post("/api/v1/billing-cycles/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billingRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.data.totalAmount", is(1600.00)))
                .andExpect(jsonPath("$.data.status", is("PENDING")));

        verify(billingCycleService, times(1)).generateBillingCycle(any(BillingCycleRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/billing-cycles/household/{householdId} - Success (Paged 200 OK)")
    void getHouseholdBills_Success() throws Exception {
        PagedResponse<BillingCycleResponse> paged = PagedResponse.<BillingCycleResponse>builder()
                .content(List.of(billingResponse))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(billingCycleService.getBillingCyclesByHousehold(eq(householdId), eq(0), eq(10))).thenReturn(paged);

        mockMvc.perform(get("/api/v1/billing-cycles/household/{householdId}", householdId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content[0].householdNumber", is("HH-101-A")));

        verify(billingCycleService, times(1)).getBillingCyclesByHousehold(eq(householdId), eq(0), eq(10));
    }

    @Test
    @DisplayName("PATCH /api/v1/billing-cycles/{id}/status - Success (200 OK)")
    void updateBillingStatus_Success() throws Exception {
        UpdateBillingStatusRequest statusRequest = UpdateBillingStatusRequest.builder()
                .status(BillingStatus.PAID)
                .paidDate(LocalDate.now())
                .build();

        BillingCycleResponse paidResponse = BillingCycleResponse.builder()
                .id(billId)
                .householdId(householdId)
                .status(BillingStatus.PAID)
                .paidDate(LocalDate.now())
                .build();

        when(billingCycleService.updateBillingStatus(eq(billId), any(UpdateBillingStatusRequest.class))).thenReturn(paidResponse);

        mockMvc.perform(patch("/api/v1/billing-cycles/{id}/status", billId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("PAID")));

        verify(billingCycleService, times(1)).updateBillingStatus(eq(billId), any(UpdateBillingStatusRequest.class));
    }
}
