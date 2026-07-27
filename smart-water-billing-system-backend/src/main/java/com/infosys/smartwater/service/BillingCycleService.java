package com.infosys.smartwater.service;

import com.infosys.smartwater.dto.request.BillingCycleRequest;
import com.infosys.smartwater.dto.request.UpdateBillingStatusRequest;
import com.infosys.smartwater.dto.response.BillingCycleResponse;
import com.infosys.smartwater.dto.response.PagedResponse;

import java.util.UUID;

/**
 * Service contract for billing cycle generation and payment management.
 */
public interface BillingCycleService {

    /**
     * Generates a monthly billing cycle for a household.
     * Automatically aggregates water usage and applies the tariff plan.
     *
     * @throws com.infosys.smartwater.exception.DuplicateResourceException if a billing cycle already exists for this household + month + year
     * @throws com.infosys.smartwater.exception.InvalidOperationException  if the household is inactive
     */
    BillingCycleResponse generateBillingCycle(BillingCycleRequest request);

    BillingCycleResponse getBillingCycleById(UUID id);

    PagedResponse<BillingCycleResponse> getBillingCyclesByHousehold(UUID householdId, int page, int size);

    PagedResponse<BillingCycleResponse> getAllBillingCycles(int page, int size, String sortBy, String sortDir);

    PagedResponse<BillingCycleResponse> getBillingCyclesByMonth(int month, int year, int page, int size);

    /**
     * Updates the payment status of a billing cycle.
     * Enforces valid state transitions (e.g., PAID cannot be reverted).
     *
     * @throws com.infosys.smartwater.exception.InvalidOperationException if the transition is not allowed
     */
    BillingCycleResponse updateBillingStatus(UUID id, UpdateBillingStatusRequest request);

    /**
     * Detects all PENDING billing cycles past their due date and marks them OVERDUE.
     * Designed to be called by a scheduled task.
     *
     * @return the number of billing cycles transitioned to OVERDUE
     */
    int detectAndMarkOverdue();

    void deleteBillingCycle(UUID id);
}
