package com.infosys.smartwater.service;

import com.infosys.smartwater.dto.request.HouseholdRequest;
import com.infosys.smartwater.dto.response.HouseholdResponse;
import com.infosys.smartwater.entity.Apartment;
import com.infosys.smartwater.entity.Household;
import com.infosys.smartwater.exception.DuplicateResourceException;
import com.infosys.smartwater.exception.ResourceNotFoundException;
import com.infosys.smartwater.mapper.HouseholdMapper;
import com.infosys.smartwater.repository.ApartmentRepository;
import com.infosys.smartwater.repository.BillingCycleRepository;
import com.infosys.smartwater.repository.HouseholdRepository;
import com.infosys.smartwater.repository.UserRepository;
import com.infosys.smartwater.repository.WaterUsageRepository;
import com.infosys.smartwater.service.impl.HouseholdServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HouseholdServiceTest {

    @Mock
    private HouseholdRepository householdRepository;
    @Mock
    private ApartmentRepository apartmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WaterUsageRepository waterUsageRepository;
    @Mock
    private BillingCycleRepository billingCycleRepository;
    @Mock
    private HouseholdMapper householdMapper;

    @InjectMocks
    private HouseholdServiceImpl householdService;

    private UUID householdId;
    private UUID apartmentId;
    private Apartment apartment;
    private Household household;
    private HouseholdRequest request;
    private HouseholdResponse response;

    @BeforeEach
    void setUp() {
        householdId = UUID.randomUUID();
        apartmentId = UUID.randomUUID();

        apartment = Apartment.builder()
                .apartmentNumber("APT-101")
                .households(new ArrayList<>())
                .build();
        apartment.setId(apartmentId);

        request = HouseholdRequest.builder()
                .householdNumber("HH-101-A")
                .ownerName("John Doe")
                .contactPhone("+1234567890")
                .apartmentId(apartmentId)
                .build();

        household = Household.builder()
                .householdNumber("HH-101-A")
                .ownerName("John Doe")
                .contactPhone("+1234567890")
                .apartment(apartment)
                .isActive(true)
                .build();
        household.setId(householdId);

        response = HouseholdResponse.builder()
                .id(householdId)
                .householdNumber("HH-101-A")
                .ownerName("John Doe")
                .apartmentId(apartmentId)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Create Household - Success")
    void createHousehold_Success() {
        given(householdRepository.existsByHouseholdNumber("HH-101-A")).willReturn(false);
        given(apartmentRepository.findById(apartmentId)).willReturn(Optional.of(apartment));
        given(householdRepository.save(any(Household.class))).willReturn(household);
        given(householdMapper.toResponse(household)).willReturn(response);

        HouseholdResponse result = householdService.createHousehold(request);

        assertNotNull(result);
        assertEquals("HH-101-A", result.getHouseholdNumber());
        verify(householdRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Create Household - Duplicate Number Throws Exception")
    void createHousehold_Duplicate_ThrowsException() {
        given(householdRepository.existsByHouseholdNumber("HH-101-A")).willReturn(true);

        assertThrows(DuplicateResourceException.class, () -> householdService.createHousehold(request));
    }

    @Test
    @DisplayName("Deactivate Household - Success")
    void deactivateHousehold_Success() {
        given(householdRepository.findById(householdId)).willReturn(Optional.of(household));
        given(householdRepository.save(household)).willReturn(household);

        HouseholdResponse deactivatedResponse = HouseholdResponse.builder()
                .id(householdId)
                .isActive(false)
                .build();
        given(householdMapper.toResponse(household)).willReturn(deactivatedResponse);

        HouseholdResponse result = householdService.deactivateHousehold(householdId);

        assertNotNull(result);
        assertFalse(result.getIsActive());
        assertFalse(household.getIsActive());
    }

    @Test
    @DisplayName("Get Household By ID - Not Found Throws Exception")
    void getHouseholdById_NotFound_ThrowsException() {
        given(householdRepository.findById(householdId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> householdService.getHouseholdById(householdId));
    }
}
