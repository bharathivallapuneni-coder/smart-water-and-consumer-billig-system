package com.infosys.smartwater.service;

import com.infosys.smartwater.dto.request.WaterUsageRequest;
import com.infosys.smartwater.dto.response.WaterUsageResponse;
import com.infosys.smartwater.entity.Household;
import com.infosys.smartwater.entity.WaterUsage;
import com.infosys.smartwater.entity.enums.ReadingType;
import com.infosys.smartwater.exception.DuplicateResourceException;
import com.infosys.smartwater.exception.InvalidOperationException;
import com.infosys.smartwater.mapper.WaterUsageMapper;
import com.infosys.smartwater.repository.HouseholdRepository;
import com.infosys.smartwater.repository.WaterUsageRepository;
import com.infosys.smartwater.service.impl.WaterUsageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaterUsageServiceTest {

    @Mock
    private WaterUsageRepository waterUsageRepository;
    @Mock
    private HouseholdRepository householdRepository;
    @Mock
    private WaterUsageMapper waterUsageMapper;

    @InjectMocks
    private WaterUsageServiceImpl waterUsageService;

    private UUID householdId;
    private Household household;
    private WaterUsageRequest request;
    private WaterUsage waterUsage;
    private WaterUsageResponse response;

    @BeforeEach
    void setUp() {
        householdId = UUID.randomUUID();

        household = Household.builder()
                .householdNumber("HH-101-A")
                .ownerName("John Doe")
                .isActive(true)
                .build();
        household.setId(householdId);

        request = WaterUsageRequest.builder()
                .householdId(householdId)
                .readingDate(LocalDate.of(2026, 7, 23))
                .meterReading(new BigDecimal("150.50"))
                .previousReading(new BigDecimal("100.00"))
                .readingType(ReadingType.MANUAL)
                .notes("Regular reading")
                .build();

        waterUsage = WaterUsage.builder()
                .household(household)
                .readingDate(LocalDate.of(2026, 7, 23))
                .meterReading(new BigDecimal("150.50"))
                .previousReading(new BigDecimal("100.00"))
                .unitsConsumed(new BigDecimal("50.50"))
                .readingType(ReadingType.MANUAL)
                .notes("Regular reading")
                .build();

        response = WaterUsageResponse.builder()
                .id(UUID.randomUUID())
                .householdId(householdId)
                .householdNumber("HH-101-A")
                .readingDate(LocalDate.of(2026, 7, 23))
                .meterReading(new BigDecimal("150.50"))
                .previousReading(new BigDecimal("100.00"))
                .unitsConsumed(new BigDecimal("50.50"))
                .readingType(ReadingType.MANUAL)
                .build();
    }

    @Test
    @DisplayName("Create Water Usage - Success")
    void createWaterUsage_Success() {
        given(householdRepository.findById(householdId)).willReturn(Optional.of(household));
        given(waterUsageRepository.existsByHouseholdIdAndReadingDate(householdId, LocalDate.of(2026, 7, 23))).willReturn(false);
        given(waterUsageRepository.save(any(WaterUsage.class))).willReturn(waterUsage);
        given(waterUsageMapper.toResponse(waterUsage)).willReturn(response);

        WaterUsageResponse result = waterUsageService.createWaterUsage(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("50.50"), result.getUnitsConsumed());
        verify(waterUsageRepository, times(1)).save(any(WaterUsage.class));
    }

    @Test
    @DisplayName("Create Water Usage - Duplicate Reading Date Throws Exception")
    void createWaterUsage_DuplicateDate_ThrowsException() {
        given(householdRepository.findById(householdId)).willReturn(Optional.of(household));
        given(waterUsageRepository.existsByHouseholdIdAndReadingDate(householdId, LocalDate.of(2026, 7, 23))).willReturn(true);

        assertThrows(DuplicateResourceException.class, () -> waterUsageService.createWaterUsage(request));
    }

    @Test
    @DisplayName("Create Water Usage - Meter Reading Less Than Previous Reading Throws Exception")
    void createWaterUsage_InvalidReadings_ThrowsException() {
        WaterUsageRequest invalidReadingRequest = WaterUsageRequest.builder()
                .householdId(householdId)
                .readingDate(LocalDate.of(2026, 7, 23))
                .meterReading(new BigDecimal("90.00")) // Less than 100.00
                .previousReading(new BigDecimal("100.00"))
                .readingType(ReadingType.MANUAL)
                .notes("Regular reading")
                .build();
        given(householdRepository.findById(householdId)).willReturn(Optional.of(household));
        given(waterUsageRepository.existsByHouseholdIdAndReadingDate(householdId, LocalDate.of(2026, 7, 23))).willReturn(false);

        assertThrows(InvalidOperationException.class, () -> waterUsageService.createWaterUsage(invalidReadingRequest));
    }
}
