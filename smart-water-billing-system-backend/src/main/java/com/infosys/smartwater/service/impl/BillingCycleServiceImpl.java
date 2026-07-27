package com.infosys.smartwater.service.impl;

import com.infosys.smartwater.dto.request.BillingCycleRequest;
import com.infosys.smartwater.dto.request.UpdateBillingStatusRequest;
import com.infosys.smartwater.dto.response.BillingCycleResponse;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.entity.BillingCycle;
import com.infosys.smartwater.entity.Household;
import com.infosys.smartwater.entity.TariffPlan;
import com.infosys.smartwater.entity.enums.BillingStatus;
import com.infosys.smartwater.exception.DuplicateResourceException;
import com.infosys.smartwater.exception.InvalidOperationException;
import com.infosys.smartwater.exception.ResourceNotFoundException;
import com.infosys.smartwater.mapper.BillingCycleMapper;
import com.infosys.smartwater.repository.BillingCycleRepository;
import com.infosys.smartwater.repository.HouseholdRepository;
import com.infosys.smartwater.repository.TariffPlanRepository;
import com.infosys.smartwater.repository.WaterUsageRepository;
import com.infosys.smartwater.service.BillingCycleService;
import com.infosys.smartwater.utils.PageableUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link BillingCycleService}.
 *
 * <p>Billing cycle generation automatically aggregates water usage for the
 * billing period and applies the tariff plan's pricing formula.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BillingCycleServiceImpl implements BillingCycleService {

    private final BillingCycleRepository billingCycleRepository;
    private final HouseholdRepository    householdRepository;
    private final TariffPlanRepository   tariffPlanRepository;
    private final WaterUsageRepository   waterUsageRepository;
    private final BillingCycleMapper     billingCycleMapper;

    // -------------------------------------------------------------------------
    // Generate
    // -------------------------------------------------------------------------

    @Override
    public BillingCycleResponse generateBillingCycle(BillingCycleRequest request) {
        log.info("Generating billing cycle for household id={} [{}/{}]",
                request.getHouseholdId(), request.getBillingMonth(), request.getBillingYear());

        // 1. Validate household is active
        Household household = householdRepository.findById(request.getHouseholdId())
                .orElseThrow(() -> new ResourceNotFoundException("Household", "id", request.getHouseholdId()));

        if (!Boolean.TRUE.equals(household.getIsActive())) {
            throw new InvalidOperationException(
                    "Cannot generate a billing cycle for inactive household: " + household.getHouseholdNumber());
        }

        // 2. Resolve tariff plan (by ID if provided, otherwise resolve active tariff plan for the date)
        LocalDate startDate = LocalDate.of(request.getBillingYear(), request.getBillingMonth(), 1);
        LocalDate endDate   = startDate.withDayOfMonth(startDate.lengthOfMonth());

        TariffPlan tariffPlan;
        if (request.getTariffPlanId() != null) {
            tariffPlan = tariffPlanRepository.findById(request.getTariffPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("TariffPlan", "id", request.getTariffPlanId()));
        } else {
            tariffPlan = tariffPlanRepository.findApplicablePlanForDate(endDate)
                    .or(() -> tariffPlanRepository.findByIsActiveTrue().stream().findFirst())
                    .orElseThrow(() -> new ResourceNotFoundException("TariffPlan", "active status",
                            "No active tariff plan found in system. Please create an active tariff plan first."));
        }

        // 3. Duplicate guard — one cycle per household per month/year
        if (billingCycleRepository.existsByHouseholdIdAndBillingMonthAndBillingYear(
                request.getHouseholdId(), request.getBillingMonth(), request.getBillingYear())) {
            throw new DuplicateResourceException("BillingCycle",
                    "householdId + billingMonth + billingYear",
                    String.format("%s for %02d/%d",
                            household.getHouseholdNumber(), request.getBillingMonth(), request.getBillingYear()));
        }

        // 4. Compute billing period (inclusive, first → last day of month) - already declared above

        // 5. Aggregate water consumption for the period
        BigDecimal totalUnitsConsumed = waterUsageRepository
                .sumUnitsConsumedBetween(household.getId(), startDate, endDate);

        // 6. Apply tariff plan pricing: max(units, minUnits) × ratePerUnit + fixedCharge
        BigDecimal totalAmount = tariffPlan.computeBillAmount(totalUnitsConsumed);

        // 7. Persist billing cycle
        BillingCycle billingCycle = BillingCycle.builder()
                .household(household)
                .tariffPlan(tariffPlan)
                .billingMonth(request.getBillingMonth())
                .billingYear(request.getBillingYear())
                .totalUnitsConsumed(totalUnitsConsumed)
                .totalAmount(totalAmount)
                .status(BillingStatus.PENDING)
                .dueDate(request.getDueDate())
                .build();

        BillingCycle saved = billingCycleRepository.save(billingCycle);

        log.info("BillingCycle generated — id={}, household='{}', amount=₹{}, units={}",
                saved.getId(), household.getHouseholdNumber(), totalAmount, totalUnitsConsumed);
        return billingCycleMapper.toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public BillingCycleResponse getBillingCycleById(UUID id) {
        return billingCycleMapper.toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BillingCycleResponse> getBillingCyclesByHousehold(
            UUID householdId, int page, int size) {
        if (!householdRepository.existsById(householdId)) {
            throw new ResourceNotFoundException("Household", "id", householdId);
        }
        Pageable pageable = PageableUtils.createPageable(page, size,
                Sort.by("billingYear").descending().and(Sort.by("billingMonth").descending()));
        return PagedResponse.from(
                billingCycleRepository.findByHouseholdId(householdId, pageable)
                        .map(billingCycleMapper::toResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BillingCycleResponse> getAllBillingCycles(
            int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, sortDir);
        return PagedResponse.from(
                billingCycleRepository.findAll(pageable).map(billingCycleMapper::toResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BillingCycleResponse> getBillingCyclesByMonth(
            int month, int year, int page, int size) {
        Pageable pageable = PageableUtils.createPageable(page, size,
                Sort.by("household.householdNumber").ascending());
        return PagedResponse.from(
                billingCycleRepository.findByBillingMonthAndBillingYear(month, year, pageable)
                        .map(billingCycleMapper::toResponse)
        );
    }

    // -------------------------------------------------------------------------
    // Status management
    // -------------------------------------------------------------------------

    @Override
    public BillingCycleResponse updateBillingStatus(UUID id, UpdateBillingStatusRequest request) {
        log.info("Updating billing cycle id={} to status={}", id, request.getStatus());
        BillingCycle billingCycle = findById(id);

        BillingStatus current = billingCycle.getStatus();
        BillingStatus target  = request.getStatus();

        // Cannot revert a PAID billing cycle
        if (BillingStatus.PAID.equals(current) && !BillingStatus.PAID.equals(target)) {
            throw new InvalidOperationException(
                    "Cannot change the status of a PAID billing cycle (id=" + id + "). " +
                    "PAID is a terminal state.");
        }

        // paidDate required when transitioning to PAID
        if (BillingStatus.PAID.equals(target) && request.getPaidDate() == null) {
            throw new InvalidOperationException(
                    "Payment date (paidDate) is required when marking a billing cycle as PAID.");
        }

        switch (target) {
            case PAID    -> billingCycle.markAsPaid(request.getPaidDate());
            case OVERDUE -> billingCycle.markAsOverdue();
            case PENDING -> billingCycle.setStatus(BillingStatus.PENDING); // manual ADMIN reset
        }

        BillingCycle saved = billingCycleRepository.save(billingCycle);
        log.info("BillingCycle id={} status updated: {} → {}", id, current, target);
        return billingCycleMapper.toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Scheduler support
    // -------------------------------------------------------------------------

    /**
     * Called by a scheduled task to detect and transition overdue billing cycles.
     * Finds all PENDING cycles whose {@code dueDate} is before today and marks them OVERDUE.
     *
     * @return the number of billing cycles transitioned to OVERDUE
     */
    @Override
    public int detectAndMarkOverdue() {
        List<BillingCycle> overdue = billingCycleRepository
                .findByStatusAndDueDateBefore(BillingStatus.PENDING, LocalDate.now());

        overdue.forEach(BillingCycle::markAsOverdue);
        billingCycleRepository.saveAll(overdue);

        if (!overdue.isEmpty()) {
            log.info("Marked {} billing cycle(s) as OVERDUE.", overdue.size());
        }
        return overdue.size();
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Override
    public void deleteBillingCycle(UUID id) {
        BillingCycle billingCycle = findById(id);

        // Prevent deletion of paid cycles to preserve financial history
        if (BillingStatus.PAID.equals(billingCycle.getStatus())) {
            throw new InvalidOperationException(
                    "Cannot delete a PAID billing cycle (id=" + id + "). " +
                    "Financial records must be preserved.");
        }

        billingCycleRepository.delete(billingCycle);
        log.info("BillingCycle id={} deleted.", id);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private BillingCycle findById(UUID id) {
        return billingCycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BillingCycle", "id", id));
    }
}
