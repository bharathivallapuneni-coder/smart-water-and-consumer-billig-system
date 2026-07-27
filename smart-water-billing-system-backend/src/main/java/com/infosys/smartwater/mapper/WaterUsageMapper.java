package com.infosys.smartwater.mapper;

import com.infosys.smartwater.dto.response.WaterUsageResponse;
import com.infosys.smartwater.entity.WaterUsage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for {@link WaterUsage} → {@link WaterUsageResponse} DTO conversions.
 *
 * <p>Flattens the {@code household} association into two denormalized fields
 * ({@code householdId}, {@code householdNumber}) so clients can display readings
 * without a secondary API call.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface WaterUsageMapper {

    /**
     * Maps a {@link WaterUsage} entity to a {@link WaterUsageResponse} DTO.
     *
     * <p>Flattened household fields:
     * <ul>
     *   <li>{@code household.id}              → {@code householdId}</li>
     *   <li>{@code household.householdNumber} → {@code householdNumber}</li>
     * </ul>
     *
     * @param waterUsage the entity to map
     * @return the response DTO
     */
    @Mapping(source = "household.id",              target = "householdId")
    @Mapping(source = "household.householdNumber", target = "householdNumber")
    WaterUsageResponse toResponse(WaterUsage waterUsage);
}
