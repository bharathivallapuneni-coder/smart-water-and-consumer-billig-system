package com.infosys.smartwater.service;

import com.infosys.smartwater.dto.request.TariffPlanRequest;
import com.infosys.smartwater.dto.response.TariffPlanResponse;
import com.infosys.smartwater.entity.TariffPlan;
import com.infosys.smartwater.exception.DuplicateResourceException;
import com.infosys.smartwater.exception.InvalidOperationException;
import com.infosys.smartwater.mapper.TariffPlanMapper;
import com.infosys.smartwater.repository.BillingCycleRepository;
import com.infosys.smartwater.repository.TariffPlanRepository;
import com.infosys.smartwater.service.impl.TariffPlanServiceImpl;
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
class TariffPlanServiceTest {

    @Mock
    private TariffPlanRepository tariffPlanRepository;
    @Mock
    private BillingCycleRepository billingCycleRepository;
    @Mock
    private TariffPlanMapper tariffPlanMapper;

    @InjectMocks
    private TariffPlanServiceImpl tariffPlanService;

    private TariffPlanRequest request;
    private TariffPlan tariffPlan;
    private TariffPlanResponse response;
    private UUID planId;

    @BeforeEach
    void setUp() {
        planId = UUID.randomUUID();
        request = TariffPlanRequest.builder()
                .planName("Residential Tier 1")
                .ratePerUnit(new BigDecimal("12.50"))
                .fixedCharge(new BigDecimal("50.00"))
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .isActive(true)
                .build();

        tariffPlan = TariffPlan.builder()
                .planName("Residential Tier 1")
                .ratePerUnit(new BigDecimal("12.50"))
                .fixedCharge(new BigDecimal("50.00"))
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .isActive(true)
                .build();
        tariffPlan.setId(planId);

        response = TariffPlanResponse.builder()
                .id(planId)
                .planName("Residential Tier 1")
                .ratePerUnit(new BigDecimal("12.50"))
                .fixedCharge(new BigDecimal("50.00"))
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Create Tariff Plan - Success")
    void createTariffPlan_Success() {
        given(tariffPlanRepository.existsByPlanName("Residential Tier 1")).willReturn(false);
        given(tariffPlanRepository.existsOverlappingActivePlan(any(), any(), any())).willReturn(false);
        given(tariffPlanMapper.toEntity(request)).willReturn(tariffPlan);
        given(tariffPlanRepository.save(tariffPlan)).willReturn(tariffPlan);
        given(tariffPlanMapper.toResponse(tariffPlan)).willReturn(response);

        TariffPlanResponse result = tariffPlanService.createTariffPlan(request);

        assertNotNull(result);
        assertEquals("Residential Tier 1", result.getPlanName());
        verify(tariffPlanRepository, times(1)).save(tariffPlan);
    }

    @Test
    @DisplayName("Create Tariff Plan - Duplicate Name Throws Exception")
    void createTariffPlan_DuplicateName_ThrowsException() {
        given(tariffPlanRepository.existsByPlanName("Residential Tier 1")).willReturn(true);

        assertThrows(DuplicateResourceException.class, () -> tariffPlanService.createTariffPlan(request));
    }

    @Test
    @DisplayName("Create Tariff Plan - Invalid Date Range Throws Exception")
    void createTariffPlan_InvalidDateRange_ThrowsException() {
        TariffPlanRequest invalidRangeRequest = TariffPlanRequest.builder()
                .planName("Residential Tier 1")
                .ratePerUnit(new BigDecimal("12.50"))
                .fixedCharge(new BigDecimal("50.00"))
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .effectiveTo(LocalDate.of(2025, 12, 31)) // Before effectiveFrom (2026-01-01)
                .isActive(true)
                .build();
        given(tariffPlanRepository.existsByPlanName("Residential Tier 1")).willReturn(false);

        assertThrows(InvalidOperationException.class, () -> tariffPlanService.createTariffPlan(invalidRangeRequest));
    }
}
