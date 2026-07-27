package com.infosys.smartwater.mapper;

import com.infosys.smartwater.dto.request.TariffPlanRequest;
import com.infosys.smartwater.dto.response.TariffPlanResponse;
import com.infosys.smartwater.entity.TariffPlan;
import org.mapstruct.*;

/**
 * MapStruct mapper for {@link TariffPlan} ↔ DTO conversions.
 *
 * <p>Supports create, read, and update operations for tariff plan management.
 * All fields map by name convention — no nested entity associations to flatten.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TariffPlanMapper {

    /**
     * Maps a {@link TariffPlan} entity to a {@link TariffPlanResponse} DTO.
     *
     * @param tariffPlan the entity to map
     * @return the response DTO
     */
    TariffPlanResponse toResponse(TariffPlan tariffPlan);

    /**
     * Maps a {@link TariffPlanRequest} DTO to a new {@link TariffPlan} entity.
     * JPA-managed and relationship fields are excluded.
     *
     * @param request the create request DTO
     * @return a new {@link TariffPlan} entity (not yet persisted)
     */
    @Mapping(target = "billingCycles", ignore = true)
    TariffPlan toEntity(TariffPlanRequest request);

    /**
     * Updates an existing {@link TariffPlan} entity from a {@link TariffPlanRequest}.
     * Null request fields do not overwrite existing entity values.
     *
     * @param request    the update request DTO
     * @param tariffPlan the entity to update in-place
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "billingCycles", ignore = true)
    void updateEntityFromRequest(TariffPlanRequest request, @MappingTarget TariffPlan tariffPlan);
}
