package com.infosys.smartwater.service;

import com.infosys.smartwater.dto.request.HouseholdRequest;
import com.infosys.smartwater.dto.response.HouseholdResponse;
import com.infosys.smartwater.dto.response.PagedResponse;

import java.util.UUID;

/**
 * Service contract for {@code Household} management operations.
 */
public interface HouseholdService {

    HouseholdResponse createHousehold(HouseholdRequest request);

    HouseholdResponse getHouseholdById(UUID id);

    PagedResponse<HouseholdResponse> getAllHouseholds(int page, int size, String sortBy, String sortDir);

    PagedResponse<HouseholdResponse> getHouseholdsByApartment(UUID apartmentId, int page, int size, String sortBy, String sortDir);

    PagedResponse<HouseholdResponse> searchHouseholds(String keyword, int page, int size);

    HouseholdResponse updateHousehold(UUID id, HouseholdRequest request);

    /** Soft-activates a household (sets {@code isActive = true}). */
    HouseholdResponse activateHousehold(UUID id);

    /** Soft-deactivates a household (sets {@code isActive = false}). */
    HouseholdResponse deactivateHousehold(UUID id);

    /**
     * Links a user account to a household.
     * The user must not already be linked to another household.
     */
    HouseholdResponse assignUser(UUID householdId, UUID userId);

    /** Removes (unlinks) the user account from a household. */
    HouseholdResponse removeUser(UUID householdId);

    /** Creates a new RESIDENT user account and links it to the specified household. */
    HouseholdResponse createAndAssignResident(UUID householdId, com.infosys.smartwater.dto.request.UserRegistrationRequest residentRequest);

    /**
     * Hard-deletes a household.
     * Throws {@code InvalidOperationException} if water usage or billing records exist.
     * Use {@link #deactivateHousehold(UUID)} for soft-deletion.
     */
    void deleteHousehold(UUID id);
}
