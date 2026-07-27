package com.infosys.smartwater.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infosys.smartwater.dto.request.WaterUsageRequest;
import com.infosys.smartwater.dto.response.CsvImportSummaryResponse;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.dto.response.WaterUsageResponse;
import com.infosys.smartwater.entity.enums.ReadingType;
import com.infosys.smartwater.exception.GlobalExceptionHandler;
import com.infosys.smartwater.service.WaterUsageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
@DisplayName("WaterUsageController REST Endpoint Unit Tests")
class WaterUsageControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private WaterUsageService waterUsageService;

    @InjectMocks
    private WaterUsageController waterUsageController;

    private UUID householdId;
    private UUID usageId;
    private WaterUsageRequest usageRequest;
    private WaterUsageResponse usageResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(waterUsageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        householdId = UUID.randomUUID();
        usageId = UUID.randomUUID();

        usageRequest = WaterUsageRequest.builder()
                .householdId(householdId)
                .readingDate(LocalDate.now())
                .meterReading(new BigDecimal("150.50"))
                .build();

        usageResponse = WaterUsageResponse.builder()
                .id(usageId)
                .householdId(householdId)
                .householdNumber("HH-101-A")
                .readingDate(LocalDate.now())
                .meterReading(new BigDecimal("150.50"))
                .unitsConsumed(new BigDecimal("50.50"))
                .readingType(ReadingType.MANUAL)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/water-usage - Success (201 Created)")
    void recordWaterUsage_Success() throws Exception {
        when(waterUsageService.createWaterUsage(any(WaterUsageRequest.class))).thenReturn(usageResponse);

        mockMvc.perform(post("/api/v1/water-usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usageRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.data.meterReading", is(150.50)));

        verify(waterUsageService, times(1)).createWaterUsage(any(WaterUsageRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/water-usage/household/{householdId}/monthly - Success (200 OK)")
    void getMonthlyReadings_Success() throws Exception {
        PagedResponse<WaterUsageResponse> pagedResponse = PagedResponse.<WaterUsageResponse>builder()
                .content(List.of(usageResponse))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(waterUsageService.getMonthlyReadings(householdId, 7, 2026, 0, 31)).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/water-usage/household/{householdId}/monthly", householdId)
                        .param("month", "7")
                        .param("year", "2026")
                        .param("page", "0")
                        .param("size", "31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content[0].householdNumber", is("HH-101-A")));

        verify(waterUsageService, times(1)).getMonthlyReadings(householdId, 7, 2026, 0, 31);
    }

    @Test
    @DisplayName("POST /api/v1/water-usage/import-csv - Success (200 OK)")
    void uploadWaterUsagesCsv_Success() throws Exception {
        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "water_readings.csv",
                "text/csv",
                "householdNumber,readingDate,meterReading\nHH-101-A,2026-07-24,150.50".getBytes()
        );

        CsvImportSummaryResponse summary = CsvImportSummaryResponse.builder()
                .totalRows(1)
                .successCount(1)
                .failedCount(0)
                .skippedCount(0)
                .build();

        when(waterUsageService.importFromCsv(any())).thenReturn(summary);

        mockMvc.perform(multipart("/api/v1/water-usage/import-csv").file(csvFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.successCount", is(1)));

        verify(waterUsageService, times(1)).importFromCsv(any());
    }
}
