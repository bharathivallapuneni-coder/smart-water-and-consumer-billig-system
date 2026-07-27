package com.infosys.smartwater.mapper;

import com.infosys.smartwater.dto.response.UserResponse;
import com.infosys.smartwater.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for {@link User} → {@link UserResponse} DTO conversions.
 *
 * <p><b>Security:</b> The {@code password} field is <em>never</em> mapped.
 * The {@code UserResponse} DTO does not contain a password field by design.
 *
 * <p>Flattens the optional {@code household} back-reference (a {@code @OneToOne(mappedBy)})
 * into {@code householdId} and {@code householdNumber}:
 * <ul>
 *   <li>For {@code RESIDENT} users: maps to the linked household's ID and number</li>
 *   <li>For {@code ADMIN} users: both fields are {@code null} (household is null)</li>
 * </ul>
 *
 * <p><b>Transaction requirement:</b> Since {@code User.household} is LAZY-loaded,
 * this mapper must be called within an active Spring transaction to avoid
 * {@code LazyInitializationException}.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    /**
     * Maps a {@link User} entity to a {@link UserResponse} DTO.
     *
     * <p>Flattened household fields (null if user is ADMIN or has no linked household):
     * <ul>
     *   <li>{@code household.id}              → {@code householdId}</li>
     *   <li>{@code household.householdNumber} → {@code householdNumber}</li>
     * </ul>
     *
     * @param user the entity to map
     * @return the response DTO without password
     */
    @Mapping(source = "household.id",              target = "householdId")
    @Mapping(source = "household.householdNumber", target = "householdNumber")
    UserResponse toResponse(User user);
}
