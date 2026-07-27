package com.infosys.smartwater.service.impl;

import com.infosys.smartwater.dto.request.TariffPlanRequest;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.dto.response.TariffPlanResponse;
import com.infosys.smartwater.entity.TariffPlan;
import com.infosys.smartwater.exception.DuplicateResourceException;
import com.infosys.smartwater.exception.InvalidOperationException;
import com.infosys.smartwater.exception.ResourceNotFoundException;
import com.infosys.smartwater.mapper.TariffPlanMapper;
import com.infosys.smartwater.repository.BillingCycleRepository;
import com.infosys.smartwater.repository.TariffPlanRepository;
import com.infosys.smartwater.service.TariffPlanService;
import com.infosys.smartwater.utils.PageableUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link TariffPlanService}.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TariffPlanServiceImpl implements TariffPlanService {

    /** Nil UUID used as an "exclude none" placeholder when checking overlaps on create. */
    private static final UUID NIL_UUID = new UUID(0L, 0L);

    private final TariffPlanRepository   tariffPlanRepository;
    private final BillingCycleRepository billingCycleRepository;
    private final TariffPlanMapper       tariffPlanMapper;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Override
    public TariffPlanResponse createTariffPlan(TariffPlanRequest request) {
        log.info("Creating tariff plan '{}'", request.getPlanName());

        // Unique name check
        if (tariffPlanRepository.existsByPlanName(request.getPlanName())) {
            throw new DuplicateResourceException("TariffPlan", "planName", request.getPlanName());
        }

        // Date range validation
        validateDateRange(request);

        // Overlap check against existing active plans
        if (Boolean.TRUE.equals(request.getIsActive())) {
            checkNoOverlap(request.getEffectiveFrom().toString(),
                           request.getEffectiveTo() != null ? request.getEffectiveTo().toString() : null,
                           NIL_UUID, request);
        }

        TariffPlan tariffPlan = tariffPlanMapper.toEntity(request);
        TariffPlan saved      = tariffPlanRepository.save(tariffPlan);

        log.info("TariffPlan '{}' created — id={}", saved.getPlanName(), saved.getId());
        return tariffPlanMapper.toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public TariffPlanResponse getTariffPlanById(UUID id) {
        return tariffPlanMapper.toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TariffPlanResponse> getAllTariffPlans(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, sortDir);
        return PagedResponse.from(
                tariffPlanRepository.findAll(pageable).map(tariffPlanMapper::toResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TariffPlanResponse> getAllActiveTariffPlans() {
        return tariffPlanRepository.findByIsActiveTrue()
                .stream()
                .map(tariffPlanMapper::toResponse)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Override
    public TariffPlanResponse updateTariffPlan(UUID id, TariffPlanRequest request) {
        log.info("Updating tariff plan id={}", id);
        TariffPlan tariffPlan = findById(id);

        // Unique name check (excluding self)
        if (tariffPlanRepository.existsByPlanNameAndIdNot(request.getPlanName(), id)) {
            throw new DuplicateResourceException("TariffPlan", "planName", request.getPlanName());
        }

        // Date range validation
        validateDateRange(request);

        // Overlap check (excluding self)
        if (Boolean.TRUE.equals(request.getIsActive())) {
            checkNoOverlap(request.getEffectiveFrom().toString(),
                           request.getEffectiveTo() != null ? request.getEffectiveTo().toString() : null,
                           id, request);
        }

        tariffPlanMapper.updateEntityFromRequest(request, tariffPlan);
        TariffPlan saved = tariffPlanRepository.save(tariffPlan);

        log.info("TariffPlan '{}' updated — id={}", saved.getPlanName(), saved.getId());
        return tariffPlanMapper.toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Deactivate / Delete
    // -------------------------------------------------------------------------

    @Override
    public TariffPlanResponse deactivateTariffPlan(UUID id) {
        TariffPlan tariffPlan = findById(id);
        tariffPlan.setIsActive(false);
        log.info("TariffPlan '{}' deactivated.", tariffPlan.getPlanName());
        return tariffPlanMapper.toResponse(tariffPlanRepository.save(tariffPlan));
    }

    @Override
    public void deleteTariffPlan(UUID id) {
        log.info("Deleting tariff plan id={}", id);
        TariffPlan tariffPlan = findById(id);

        // Block deletion if referenced by billing cycles
        if (!tariffPlan.getBillingCycles().isEmpty()) {
            throw new InvalidOperationException(
                    String.format("Cannot delete tariff plan '%s' — it is referenced by %d billing cycle(s).",
                            tariffPlan.getPlanName(), tariffPlan.getBillingCycles().size()));
        }

        tariffPlanRepository.delete(tariffPlan);
        log.info("TariffPlan '{}' (id={}) deleted.", tariffPlan.getPlanName(), id);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private TariffPlan findById(UUID id) {
        return tariffPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TariffPlan", "id", id));
    }

    private void validateDateRange(TariffPlanRequest request) {
        if (request.getEffectiveTo() != null
                && !request.getEffectiveTo().isAfter(request.getEffectiveFrom())) {
            throw new InvalidOperationException(
                    "Effective-to date must be strictly after effective-from date.");
        }
    }

    private void checkNoOverlap(String fromStr, String toStr, UUID excludeId, TariffPlanRequest request) {
        boolean hasOverlap = tariffPlanRepository.existsOverlappingActivePlan(
                request.getEffectiveFrom(), request.getEffectiveTo(), excludeId);
        if (hasOverlap) {
            throw new InvalidOperationException(
                    "An active tariff plan already exists that overlaps with the specified date range " +
                    "[" + fromStr + " – " + (toStr != null ? toStr : "open-ended") + "]. " +
                    "Deactivate the existing plan before creating or activating this one.");
        }
    }
}
