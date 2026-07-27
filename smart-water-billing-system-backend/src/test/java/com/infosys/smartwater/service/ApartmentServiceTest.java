package com.infosys.smartwater.service;

import com.infosys.smartwater.dto.request.ApartmentRequest;
import com.infosys.smartwater.dto.response.ApartmentResponse;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.entity.Apartment;
import com.infosys.smartwater.exception.DuplicateResourceException;
import com.infosys.smartwater.exception.InvalidOperationException;
import com.infosys.smartwater.exception.ResourceNotFoundException;
import com.infosys.smartwater.mapper.ApartmentMapper;
import com.infosys.smartwater.repository.ApartmentRepository;
import com.infosys.smartwater.service.impl.ApartmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApartmentServiceTest {

    @Mock
    private ApartmentRepository apartmentRepository;

    @Mock
    private ApartmentMapper apartmentMapper;

    @InjectMocks
    private ApartmentServiceImpl apartmentService;

    private Apartment apartment;
    private ApartmentRequest apartmentRequest;
    private ApartmentResponse apartmentResponse;
    private UUID apartmentId;

    @BeforeEach
    void setUp() {
        apartmentId = UUID.randomUUID();
        apartmentRequest = ApartmentRequest.builder()
                .apartmentNumber("APT-101")
                .buildingName("Sunrise Towers")
                .totalFloors(10)
                .address("123 Main St")
                .build();

        apartment = Apartment.builder()
                .apartmentNumber("APT-101")
                .buildingName("Sunrise Towers")
                .totalFloors(10)
                .address("123 Main St")
                .households(new ArrayList<>())
                .build();
        apartment.setId(apartmentId);

        apartmentResponse = ApartmentResponse.builder()
                .id(apartmentId)
                .apartmentNumber("APT-101")
                .buildingName("Sunrise Towers")
                .totalFloors(10)
                .address("123 Main St")
                .totalHouseholds(0)
                .build();
    }

    @Test
    @DisplayName("Create Apartment - Success")
    void createApartment_Success() {
        given(apartmentRepository.existsByApartmentNumber("APT-101")).willReturn(false);
        given(apartmentMapper.toEntity(apartmentRequest)).willReturn(apartment);
        given(apartmentRepository.save(apartment)).willReturn(apartment);
        given(apartmentMapper.toResponse(apartment)).willReturn(apartmentResponse);

        ApartmentResponse result = apartmentService.createApartment(apartmentRequest);

        assertNotNull(result);
        assertEquals("APT-101", result.getApartmentNumber());
        verify(apartmentRepository, times(1)).save(apartment);
    }

    @Test
    @DisplayName("Create Apartment - Duplicate Throws Exception")
    void createApartment_Duplicate_ThrowsException() {
        given(apartmentRepository.existsByApartmentNumber("APT-101")).willReturn(true);

        assertThrows(DuplicateResourceException.class, () -> apartmentService.createApartment(apartmentRequest));
        verify(apartmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get Apartment By ID - Success")
    void getApartmentById_Success() {
        given(apartmentRepository.findById(apartmentId)).willReturn(Optional.of(apartment));
        given(apartmentMapper.toResponse(apartment)).willReturn(apartmentResponse);

        ApartmentResponse result = apartmentService.getApartmentById(apartmentId);

        assertNotNull(result);
        assertEquals(apartmentId, result.getId());
    }

    @Test
    @DisplayName("Get Apartment By ID - Not Found Throws Exception")
    void getApartmentById_NotFound_ThrowsException() {
        given(apartmentRepository.findById(apartmentId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> apartmentService.getApartmentById(apartmentId));
    }

    @Test
    @DisplayName("Get All Apartments - Paginated")
    void getAllApartments_Success() {
        Page<Apartment> page = new PageImpl<>(List.of(apartment));
        given(apartmentRepository.findAll(any(Pageable.class))).willReturn(page);
        given(apartmentMapper.toResponse(apartment)).willReturn(apartmentResponse);

        PagedResponse<ApartmentResponse> result = apartmentService.getAllApartments(0, 10, "apartmentNumber", "asc");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Delete Apartment - Blocked if Households Exist")
    void deleteApartment_BlockedWhenHouseholdsExist() {
        apartment.getHouseholds().add(mock(com.infosys.smartwater.entity.Household.class));
        given(apartmentRepository.findById(apartmentId)).willReturn(Optional.of(apartment));

        assertThrows(InvalidOperationException.class, () -> apartmentService.deleteApartment(apartmentId));
        verify(apartmentRepository, never()).delete(any());
    }
}
