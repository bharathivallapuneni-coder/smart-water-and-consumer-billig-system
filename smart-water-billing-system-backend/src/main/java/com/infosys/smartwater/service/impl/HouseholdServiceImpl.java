package com.infosys.smartwater.service.impl;

import com.infosys.smartwater.dto.request.HouseholdRequest;
import com.infosys.smartwater.dto.response.HouseholdResponse;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.entity.Apartment;
import com.infosys.smartwater.entity.Household;
import com.infosys.smartwater.entity.User;
import com.infosys.smartwater.exception.DuplicateResourceException;
import com.infosys.smartwater.exception.InvalidOperationException;
import com.infosys.smartwater.exception.ResourceNotFoundException;
import com.infosys.smartwater.mapper.HouseholdMapper;
import com.infosys.smartwater.repository.ApartmentRepository;
import com.infosys.smartwater.repository.BillingCycleRepository;
import com.infosys.smartwater.repository.HouseholdRepository;
import com.infosys.smartwater.repository.UserRepository;
import com.infosys.smartwater.repository.WaterUsageRepository;
import com.infosys.smartwater.service.HouseholdService;
import com.infosys.smartwater.utils.PageableUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of {@link HouseholdService}.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class HouseholdServiceImpl implements HouseholdService {

    private final HouseholdRepository householdRepository;
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final WaterUsageRepository waterUsageRepository;
    private final BillingCycleRepository billingCycleRepository;
    private final HouseholdMapper householdMapper;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Override
    public HouseholdResponse createHousehold(HouseholdRequest request) {
        log.info("Creating household '{}' in apartment id={}", request.getHouseholdNumber(), request.getApartmentId());

        // Uniqueness check
        if (householdRepository.existsByHouseholdNumber(request.getHouseholdNumber())) {
            throw new DuplicateResourceException("Household", "householdNumber", request.getHouseholdNumber());
        }

        // Find parent apartment
        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Apartment", "id", request.getApartmentId()));

        // Optionally resolve user
        User user = resolveUser(request.getUserId());

        // Build and persist household
        Household household = Household.builder()
                .householdNumber(request.getHouseholdNumber())
                .ownerName(request.getOwnerName())
                .contactPhone(request.getContactPhone())
                .apartment(apartment)
                .user(user)
                .isActive(true)
                .build();

        Household saved = householdRepository.save(household);

        // Update total households count on parent apartment
        if (apartment.getTotalHouseholds() == null) {
            apartment.setTotalHouseholds(1);
        } else {
            apartment.setTotalHouseholds(apartment.getTotalHouseholds() + 1);
        }
        apartmentRepository.save(apartment);

        log.info("Household '{}' created — id={}", saved.getHouseholdNumber(), saved.getId());
        return householdMapper.toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public HouseholdResponse getHouseholdById(UUID id) {
        return householdMapper.toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<HouseholdResponse> getAllHouseholds(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, sortDir);
        return PagedResponse.from(
                householdRepository.findAll(pageable).map(householdMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<HouseholdResponse> getHouseholdsByApartment(
            UUID apartmentId, int page, int size, String sortBy, String sortDir) {
        // Verify apartment exists
        if (!apartmentRepository.existsById(apartmentId)) {
            throw new ResourceNotFoundException("Apartment", "id", apartmentId);
        }
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, sortDir);
        return PagedResponse.from(
                householdRepository.findByApartmentId(apartmentId, pageable).map(householdMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<HouseholdResponse> searchHouseholds(String keyword, int page, int size) {
        Pageable pageable = PageableUtils.createPageable(page, size, "householdNumber", "asc");
        return PagedResponse.from(
                householdRepository.searchByKeyword(keyword, pageable).map(householdMapper::toResponse));
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Override
    public HouseholdResponse updateHousehold(UUID id, HouseholdRequest request) {
        log.info("Updating household id={}", id);
        Household household = findById(id);

        // Uniqueness check (excluding self)
        if (householdRepository.existsByHouseholdNumberAndIdNot(request.getHouseholdNumber(), id)) {
            throw new DuplicateResourceException("Household", "householdNumber", request.getHouseholdNumber());
        }

        // Resolve new apartment if changed
        if (!household.getApartment().getId().equals(request.getApartmentId())) {
            Apartment newApartment = apartmentRepository.findById(request.getApartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Apartment", "id", request.getApartmentId()));
            // Unlink from old apartment, link to new
            household.getApartment().removeHousehold(household);
            newApartment.addHousehold(household);
            household.setApartment(newApartment);
        }

        // Resolve optional user
        User user = resolveUser(request.getUserId());
        if (request.getUserId() != null && user != null
                && householdRepository.existsByUserId(request.getUserId())
                && !household.getId().equals(householdRepository.findByUserId(request.getUserId())
                        .map(Household::getId).orElse(null))) {
            throw new InvalidOperationException(
                    "User is already linked to another household. Unlink first.");
        }
        household.setUser(user);

        household.setHouseholdNumber(request.getHouseholdNumber());
        household.setOwnerName(request.getOwnerName());
        household.setContactPhone(request.getContactPhone());

        Household saved = householdRepository.save(household);
        log.info("Household '{}' updated — id={}", saved.getHouseholdNumber(), saved.getId());
        return householdMapper.toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Activate / Deactivate (soft-delete)
    // -------------------------------------------------------------------------

    @Override
    public HouseholdResponse activateHousehold(UUID id) {
        Household household = findById(id);
        household.setIsActive(true);
        log.info("Household '{}' activated.", household.getHouseholdNumber());
        return householdMapper.toResponse(householdRepository.save(household));
    }

    @Override
    public HouseholdResponse deactivateHousehold(UUID id) {
        Household household = findById(id);
        household.setIsActive(false);
        log.info("Household '{}' deactivated.", household.getHouseholdNumber());
        return householdMapper.toResponse(householdRepository.save(household));
    }

    // -------------------------------------------------------------------------
    // User assignment
    // -------------------------------------------------------------------------

    @Override
    public HouseholdResponse assignUser(UUID householdId, UUID userId) {
        log.info("Assigning user id={} to household id={}", userId, householdId);
        Household household = findById(householdId);

        // Check user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check user not already linked to another household
        if (householdRepository.existsByUserId(userId)) {
            throw new InvalidOperationException(
                    String.format("User '%s' is already linked to a household. Unlink first.", user.getEmail()));
        }

        household.setUser(user);
        Household saved = householdRepository.save(household);
        log.info("User '{}' assigned to household '{}'.", user.getEmail(), household.getHouseholdNumber());
        return householdMapper.toResponse(saved);
    }

    @Override
    public HouseholdResponse removeUser(UUID householdId) {
        log.info("Removing user from household id={}", householdId);
        Household household = findById(householdId);
        household.setUser(null);
        return householdMapper.toResponse(householdRepository.save(household));
    }

    @Override
    public HouseholdResponse createAndAssignResident(UUID householdId,
            com.infosys.smartwater.dto.request.UserRegistrationRequest residentRequest) {
        log.info("Creating RESIDENT account for household id={}", householdId);
        Household household = findById(householdId);

        if (userRepository.existsByEmail(residentRequest.getEmail())) {
            throw new DuplicateResourceException("User", "email", residentRequest.getEmail());
        }

        if (userRepository.existsByUsername(residentRequest.getUsername())) {
            throw new DuplicateResourceException("User", "username", residentRequest.getUsername());
        }

        User resident = User.builder()
                .username(residentRequest.getUsername())
                .email(residentRequest.getEmail())
                .password(passwordEncoder.encode(residentRequest.getPassword()))
                .phone(residentRequest.getPhone())
                .role(com.infosys.smartwater.entity.enums.Role.RESIDENT)
                .approvalStatus(com.infosys.smartwater.entity.enums.ApprovalStatus.APPROVED)
                .isEnabled(true)
                .build();

        User savedResident = userRepository.save(resident);
        household.setUser(savedResident);

        Household savedHousehold = householdRepository.save(household);
        log.info("RESIDENT user '{}' created and assigned to household '{}'", savedResident.getEmail(),
                savedHousehold.getHouseholdNumber());
        return householdMapper.toResponse(savedHousehold);
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Override
    public void deleteHousehold(UUID id) {
        log.info("Deleting household id={}", id);
        Household household = findById(id);

        long waterUsageCount = waterUsageRepository.countByHouseholdId(id);
        long billingCycleCount = billingCycleRepository.countByHouseholdId(id);

        if (waterUsageCount > 0 || billingCycleCount > 0) {
            throw new InvalidOperationException(
                    String.format("Cannot delete household '%s' — it has %d water usage record(s) and " +
                            "%d billing cycle(s). Use deactivate instead.",
                            household.getHouseholdNumber(), waterUsageCount, billingCycleCount));
        }

        // Unlink from apartment
        household.getApartment().removeHousehold(household);
        apartmentRepository.save(household.getApartment());

        householdRepository.delete(household);
        log.info("Household '{}' (id={}) deleted.", household.getHouseholdNumber(), id);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Household findById(UUID id) {
        return householdRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Household", "id", id));
    }

    /**
     * Resolves an optional user ID. Returns {@code null} if {@code userId} is
     * {@code null}.
     * Throws {@link ResourceNotFoundException} if {@code userId} is provided but
     * doesn't exist.
     */
    private User resolveUser(UUID userId) {
        if (userId == null)
            return null;
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}
