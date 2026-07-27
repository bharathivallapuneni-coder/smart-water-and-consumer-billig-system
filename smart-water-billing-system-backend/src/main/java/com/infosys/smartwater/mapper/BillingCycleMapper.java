package com.infosys.smartwater.mapper;

import com.infosys.smartwater.dto.response.BillingCycleResponse;
import com.infosys.smartwater.entity.BillingCycle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for {@link BillingCycle} → {@link BillingCycleResponse} DTO conversions.
 *
 * <p>Flattens both the {@code household} and {@code tariffPlan} associations
 * into denormalized summary fields, providing clients with a self-contained
 * billing record response that requires no further API round-trips.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BillingCycleMapper {

    /**
     * Maps a {@link BillingCycle} entity to a {@link BillingCycleResponse} DTO.
     *
     * <p>Flattened household fields:
     * <ul>
     *   <li>{@code household.id}              → {@code householdId}</li>
     *   <li>{@code household.householdNumber} → {@code householdNumber}</li>
     *   <li>{@code household.ownerName}       → {@code ownerName}</li>
     * </ul>
     *
     * <p>Flattened tariff plan fields:
     * <ul>
     *   <li>{@code tariffPlan.id}          → {@code tariffPlanId}</li>
     *   <li>{@code tariffPlan.planName}    → {@code tariffPlanName}</li>
     *   <li>{@code tariffPlan.ratePerUnit} → {@code ratePerUnit}</li>
     * </ul>
     *
     * @param billingCycle the entity to map
     * @return the response DTO with flattened household and tariff plan summaries
     */
    @Mapping(source = "household.id",              target = "householdId")
    @Mapping(source = "household.householdNumber", target = "householdNumber")
    @Mapping(source = "household.ownerName",       target = "ownerName")
    @Mapping(source = "tariffPlan.id",             target = "tariffPlanId")
    @Mapping(source = "tariffPlan.planName",       target = "tariffPlanName")
    @Mapping(source = "tariffPlan.ratePerUnit",    target = "ratePerUnit")
    BillingCycleResponse toResponse(BillingCycle billingCycle);
}
