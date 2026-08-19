package com.infosys.smartwater.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.smartwater.entity.Apartment;
import com.infosys.smartwater.entity.TariffTier;
import com.infosys.smartwater.exception.GlobalExceptionHandler;
import com.infosys.smartwater.repository.ApartmentRepository;
import com.infosys.smartwater.repository.TariffTierRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuildingTariffController REST Endpoint Unit Tests")
class BuildingTariffControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TariffTierRepository tariffTierRepository;

    @Mock
    private ApartmentRepository apartmentRepository;

    @InjectMocks
    private BuildingTariffController buildingTariffController;

    private UUID buildingId;
    private Apartment apartment;
    private TariffTier tier1;
    private TariffTier tier2;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(buildingTariffController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        buildingId = UUID.randomUUID();

        apartment = Apartment.builder()
                .apartmentNumber("APT-1002")
                .buildingName("Green Valley Apartments")
                .address("123 Palm Avenue")
                .build();
        apartment.setId(buildingId);

        tier1 = TariffTier.builder()
                .apartment(apartment)
                .tierName("Base Tier (0-10 kL)")
                .minKl(BigDecimal.ZERO)
                .maxKl(new BigDecimal("10"))
                .ratePerKl(new BigDecimal("15"))
                .fixedCharge(BigDecimal.ZERO)
                .build();
        tier1.setId(UUID.randomUUID());

        tier2 = TariffTier.builder()
                .apartment(apartment)
                .tierName("High Tier (>10 kL)")
                .minKl(new BigDecimal("11"))
                .maxKl(null)
                .ratePerKl(new BigDecimal("40"))
                .fixedCharge(BigDecimal.ZERO)
                .build();
        tier2.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("GET /api/tariffs/building/{buildingId} - Success (200 OK)")
    void getBuildingTariffs_Success() throws Exception {
        when(tariffTierRepository.findByApartmentIdOrderByMinKlAsc(buildingId)).thenReturn(List.of(tier1, tier2));

        mockMvc.perform(get("/api/tariffs/building/{buildingId}", buildingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data[0].tierName", is("Base Tier (0-10 kL)")))
                .andExpect(jsonPath("$.data[0].ratePerKl", is(15)))
                .andExpect(jsonPath("$.data[1].tierName", is("High Tier (>10 kL)")))
                .andExpect(jsonPath("$.data[1].ratePerKl", is(40)));

        verify(tariffTierRepository, times(1)).findByApartmentIdOrderByMinKlAsc(buildingId);
    }

    @Test
    @DisplayName("POST /api/tariffs/building/{buildingId} - Success (200 OK)")
    void saveBuildingTariffs_Success() throws Exception {
        when(apartmentRepository.findById(buildingId)).thenReturn(Optional.of(apartment));
        doNothing().when(tariffTierRepository).deleteByApartmentId(buildingId);
        when(tariffTierRepository.save(any(TariffTier.class))).thenReturn(tier1).thenReturn(tier2);

        String jsonPayload = """
                [
                  {
                    "tierName": "Base Tier (0-10 kL)",
                    "minKl": 0,
                    "maxKl": 10,
                    "ratePerKl": 15,
                    "fixedCharge": 0
                  },
                  {
                    "tierName": "High Tier (>10 kL)",
                    "minKl": 11,
                    "maxKl": null,
                    "ratePerKl": 40,
                    "fixedCharge": 0
                  }
                ]
                """;

        mockMvc.perform(post("/api/tariffs/building/{buildingId}", buildingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data[0].tierName", is("Base Tier (0-10 kL)")))
                .andExpect(jsonPath("$.data[1].tierName", is("High Tier (>10 kL)")));

        verify(tariffTierRepository, times(1)).deleteByApartmentId(buildingId);
        verify(tariffTierRepository, times(2)).save(any(TariffTier.class));
    }
}
