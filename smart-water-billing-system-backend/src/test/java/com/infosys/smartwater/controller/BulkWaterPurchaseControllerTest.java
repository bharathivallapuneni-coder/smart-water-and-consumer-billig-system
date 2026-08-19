package com.infosys.smartwater.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infosys.smartwater.entity.Apartment;
import com.infosys.smartwater.entity.BulkWaterPurchase;
import com.infosys.smartwater.exception.GlobalExceptionHandler;
import com.infosys.smartwater.service.BulkWaterPurchaseService;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("BulkWaterPurchaseController REST Endpoint Unit Tests")
class BulkWaterPurchaseControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private BulkWaterPurchaseService purchaseService;

    @InjectMocks
    private BulkWaterPurchaseController bulkWaterPurchaseController;

    private UUID apartmentId;
    private UUID purchaseId;
    private Apartment apartment;
    private BulkWaterPurchase purchase;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(bulkWaterPurchaseController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        apartmentId = UUID.randomUUID();
        purchaseId = UUID.randomUUID();

        apartment = Apartment.builder()
                .apartmentNumber("APT-1002")
                .buildingName("Green Valley Apartments")
                .address("123 Palm Avenue")
                .build();
        apartment.setId(apartmentId);

        purchase = BulkWaterPurchase.builder()
                .apartment(apartment)
                .sourceType("TANKER")
                .supplierName("Aqua Pure Tankers")
                .purchaseDate(LocalDate.now())
                .purchasedVolumeKl(new BigDecimal("50.00"))
                .totalCost(new BigDecimal("5000.00"))
                .unitCostPerKl(new BigDecimal("100.00"))
                .notes("Test purchase")
                .build();
        purchase.setId(purchaseId);
    }

    @Test
    @DisplayName("GET /api/bulk-purchases/building/{apartmentId} - Success (200 OK)")
    void getPurchases_Success() throws Exception {
        when(purchaseService.getPurchasesByApartment(apartmentId)).thenReturn(List.of(purchase));

        mockMvc.perform(get("/api/bulk-purchases/building/{apartmentId}", apartmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data[0].sourceType", is("TANKER")))
                .andExpect(jsonPath("$.data[0].supplierName", is("Aqua Pure Tankers")))
                .andExpect(jsonPath("$.data[0].purchasedVolumeKl", is(50.0)))
                .andExpect(jsonPath("$.data[0].totalCost", is(5000.0)))
                .andExpect(jsonPath("$.data[0].unitCostPerKl", is(100.0)));

        verify(purchaseService, times(1)).getPurchasesByApartment(apartmentId);
    }

    @Test
    @DisplayName("POST /api/bulk-purchases - Success (201 Created)")
    void createPurchase_Success() throws Exception {
        when(purchaseService.createPurchase(
                eq(apartmentId),
                isNull(),
                eq("TANKER"),
                eq("Aqua Pure Tankers"),
                any(LocalDate.class),
                eq(new BigDecimal("50")),
                eq(new BigDecimal("5000")),
                eq("Test purchase")
        )).thenReturn(purchase);

        String jsonPayload = String.format("""
                {
                  "apartmentId": "%s",
                  "sourceType": "TANKER",
                  "supplierName": "Aqua Pure Tankers",
                  "purchaseDate": "%s",
                  "purchasedVolumeKl": 50,
                  "totalCost": 5000,
                  "notes": "Test purchase"
                }
                """, apartmentId, LocalDate.now());

        mockMvc.perform(post("/api/bulk-purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.sourceType", is("TANKER")))
                .andExpect(jsonPath("$.data.unitCostPerKl", is(100.0)));
    }
}
