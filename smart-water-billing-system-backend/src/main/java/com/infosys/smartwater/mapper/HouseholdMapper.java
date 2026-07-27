package com.infosys.smartwater.mapper;

import com.infosys.smartwater.dto.response.HouseholdResponse;
import com.infosys.smartwater.entity.Household;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for {@link Household} → {@link HouseholdResponse} DTO conversions.
 *
 * <p>Flattens the nested {@code apartment} and optional {@code user} associations
 * into the response DTO fields, avoiding deep object nesting in API responses.
 *
 * <p>MapStruct handles null nested objects gracefully:
 * if {@code household.user} is {@code null}, all user-derived fields
 * ({@code userId}, {@code username}, {@code userEmail}) will be {@code null}
 * in the response DTO — no {@code NullPointerException}.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface HouseholdMapper {

    /**
     * Maps a {@link Household} entity to a {@link HouseholdResponse} DTO.
     *
     * <p>Flattened apartment fields:
     * <ul>
     *   <li>{@code apartment.id}              → {@code apartmentId}</li>
     *   <li>{@code apartment.apartmentNumber} → {@code apartmentNumber}</li>
     *   <li>{@code apartment.buildingName}    → {@code buildingName}</li>
     * </ul>
     *
     * <p>Flattened user fields (all {@code null} if no user assigned):
     * <ul>
     *   <li>{@code user.id}       → {@code userId}</li>
     *   <li>{@code user.username} → {@code username}</li>
     *   <li>{@code user.email}    → {@code userEmail}</li>
     * </ul>
     *
     * @param household the entity to map
     * @return the response DTO with flattened associations
     */
    @Mapping(source = "apartment.id",              target = "apartmentId")
    @Mapping(source = "apartment.apartmentNumber", target = "apartmentNumber")
    @Mapping(source = "apartment.buildingName",    target = "buildingName")
    @Mapping(source = "user.id",                   target = "userId")
    @Mapping(source = "user.username",             target = "username")
    @Mapping(source = "user.email",                target = "userEmail")
    HouseholdResponse toResponse(Household household);

    /**
     * Maps a list of {@link Household} entities to a list of {@link HouseholdResponse} DTOs.
     *
     * @param households the list of entities to map
     * @return list of response DTOs
     */
    List<HouseholdResponse> toResponseList(List<Household> households);
}
