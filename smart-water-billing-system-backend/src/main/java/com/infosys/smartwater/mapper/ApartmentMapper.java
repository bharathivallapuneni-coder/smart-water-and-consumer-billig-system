package com.infosys.smartwater.mapper;

import com.infosys.smartwater.dto.request.ApartmentRequest;
import com.infosys.smartwater.dto.response.ApartmentResponse;
import com.infosys.smartwater.entity.Apartment;
import org.mapstruct.*;

/**
 * MapStruct mapper for {@link Apartment} ↔ DTO conversions.
 *
 * <p>Registered as a Spring bean via {@code componentModel = "spring"}.
 * Inject with {@code @Autowired} or constructor injection in service classes.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ApartmentMapper {

    /**
     * Maps an {@link Apartment} entity to an {@link ApartmentResponse} DTO.
     * All fields map by name convention — no custom mappings needed.
     *
     * @param apartment the entity to map
     * @return the response DTO
     */
    ApartmentResponse toResponse(Apartment apartment);

    /**
     * Maps an {@link ApartmentRequest} DTO to a new {@link Apartment} entity.
     * Fields that should not be set from the request (id, timestamps, households, totalHouseholds)
     * are ignored — they are managed by JPA or business logic.
     *
     * @param request the create request DTO
     * @return a new {@link Apartment} entity (not yet persisted)
     */
    @Mapping(target = "households",      ignore = true)
    @Mapping(target = "totalHouseholds", ignore = true)
    Apartment toEntity(ApartmentRequest request);

    /**
     * Updates an existing {@link Apartment} entity from an {@link ApartmentRequest}.
     * Uses {@link NullValuePropertyMappingStrategy#IGNORE} so that {@code null}
     * request fields do not overwrite existing entity values (useful for partial updates).
     *
     * @param request  the update request DTO
     * @param apartment the entity to update (modified in-place via {@code @MappingTarget})
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "households",      ignore = true)
    @Mapping(target = "totalHouseholds", ignore = true)
    void updateEntityFromRequest(ApartmentRequest request, @MappingTarget Apartment apartment);
}
