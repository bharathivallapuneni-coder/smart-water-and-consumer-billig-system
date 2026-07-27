package com.infosys.smartwater.service;

import com.infosys.smartwater.dto.request.BillingCycleRequest;
import com.infosys.smartwater.dto.request.UpdateBillingStatusRequest;
import com.infosys.smartwater.dto.response.BillingCycleResponse;
import com.infosys.smartwater.entity.BillingCycle;
import com.infosys.smartwater.entity.Household;
import com.infosys.smartwater.entity.TariffPlan;
import com.infosys.smartwater.entity.enums.BillingStatus;
import com.infosys.smartwater.exception.DuplicateResourceException;
import com.infosys.smartwater.exception.InvalidOperationException;
import com.infosys.smartwater.mapper.BillingCycleMapper;
import com.infosys.smartwater.repository.BillingCycleRepository;
import com.infosys.smartwater.repository.HouseholdRepository;
import com.infosys.smartwater.repository.TariffPlanRepository;
import com.infosys.smartwater.repository.WaterUsageRepository;
import com.infosys.smartwater.service.impl.BillingCycleServiceImpl;
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
class BillingCycleServiceTest {

    @Mock
    private BillingCycleRepository billingCycleRepository;
    @Mock
    private HouseholdRepository householdRepository;
    @Mock
    private TariffPlanRepository tariffPlanRepository;
    @Mock
    private WaterUsageRepository waterUsageRepository;
    @Mock
    private BillingCycleMapper billingCycleMapper;

    @InjectMocks
    private BillingCycleServiceImpl billingCycleService;

    private UUID householdId;
    private UUID tariffPlanId;
    private Household household;
    private TariffPlan tariffPlan;
    private BillingCycleRequest request;
    private BillingCycle billingCycle;
    private BillingCycleResponse response;

    @BeforeEach
    void setUp() {
        householdId = UUID.randomUUID();
        tariffPlanId = UUID.randomUUID();

        household = Household.builder()
                .householdNumber("HH-101-A")
                .ownerName("John Doe")
                .isActive(true)
                .build();
        household.setId(householdId);

        tariffPlan = TariffPlan.builder()
                .planName("Standard Tariff 2026")
                .ratePerUnit(new BigDecimal("15.00"))
                .fixedCharge(new BigDecimal("100.00"))
                .isActive(true)
                .build();
        tariffPlan.setId(tariffPlanId);

        request = BillingCycleRequest.builder()
                .householdId(householdId)
                .tariffPlanId(tariffPlanId)
                .billingMonth(7)
                .billingYear(2026)
                .dueDate(LocalDate.of(2026, 8, 15))
                .build();

        billingCycle = BillingCycle.builder()
                .household(household)
                .tariffPlan(tariffPlan)
                .billingMonth(7)
                .billingYear(2026)
                .totalUnitsConsumed(new BigDecimal("100.00"))
                .totalAmount(new BigDecimal("1600.00"))
                .status(BillingStatus.PENDING)
                .dueDate(LocalDate.of(2026, 8, 15))
                .build();

        response = BillingCycleResponse.builder()
                .id(UUID.randomUUID())
                .householdId(householdId)
                .householdNumber("HH-101-A")
                .billingMonth(7)
                .billingYear(2026)
                .totalUnitsConsumed(new BigDecimal("100.00"))
                .totalAmount(new BigDecimal("1600.00"))
                .status(BillingStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Generate Billing Cycle - Success")
    void generateBillingCycle_Success() {
        given(householdRepository.findById(householdId)).willReturn(Optional.of(household));
        given(tariffPlanRepository.findById(tariffPlanId)).willReturn(Optional.of(tariffPlan));
        given(billingCycleRepository.existsByHouseholdIdAndBillingMonthAndBillingYear(householdId, 7, 2026)).willReturn(false);
        given(waterUsageRepository.sumUnitsConsumedBetween(eq(householdId), any(LocalDate.class), any(LocalDate.class))).willReturn(new BigDecimal("100.00"));
        given(billingCycleRepository.save(any(BillingCycle.class))).willReturn(billingCycle);
        given(billingCycleMapper.toResponse(billingCycle)).willReturn(response);

        BillingCycleResponse result = billingCycleService.generateBillingCycle(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("1600.00"), result.getTotalAmount());
        verify(billingCycleRepository, times(1)).save(any(BillingCycle.class));
    }

    @Test
    @DisplayName("Generate Billing Cycle - Duplicate Month/Year Throws Exception")
    void generateBillingCycle_Duplicate_ThrowsException() {
        given(householdRepository.findById(householdId)).willReturn(Optional.of(household));
        given(tariffPlanRepository.findById(tariffPlanId)).willReturn(Optional.of(tariffPlan));
        given(billingCycleRepository.existsByHouseholdIdAndBillingMonthAndBillingYear(householdId, 7, 2026)).willReturn(true);

        assertThrows(DuplicateResourceException.class, () -> billingCycleService.generateBillingCycle(request));
    }

    @Test
    @DisplayName("Update Billing Status - Transition PAID to PENDING Throws Exception")
    void updateBillingStatus_RevertPaid_ThrowsException() {
        UUID cycleId = UUID.randomUUID();
        billingCycle.setStatus(BillingStatus.PAID);
        given(billingCycleRepository.findById(cycleId)).willReturn(Optional.of(billingCycle));

        UpdateBillingStatusRequest statusRequest = UpdateBillingStatusRequest.builder()
                .status(BillingStatus.PENDING)
                .build();

        assertThrows(InvalidOperationException.class, () -> billingCycleService.updateBillingStatus(cycleId, statusRequest));
    }
}
