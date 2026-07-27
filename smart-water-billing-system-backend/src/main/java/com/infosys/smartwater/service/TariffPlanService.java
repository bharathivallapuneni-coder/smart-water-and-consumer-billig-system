package com.infosys.smartwater.service;

import com.infosys.smartwater.dto.request.TariffPlanRequest;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.dto.response.TariffPlanResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for tariff plan management.
 */
public interface TariffPlanService {

    /**
     * Creates a new tariff plan.
     *
     * @throws com.infosys.smartwater.exception.DuplicateResourceException if plan name already exists
     * @throws com.infosys.smartwater.exception.InvalidOperationException  if the effective date range is invalid
     *         or overlaps an existing active plan
     */
    TariffPlanResponse createTariffPlan(TariffPlanRequest request);

    TariffPlanResponse getTariffPlanById(UUID id);

    PagedResponse<TariffPlanResponse> getAllTariffPlans(int page, int size, String sortBy, String sortDir);

    /** Returns all currently active tariff plans (unpaged). */
    List<TariffPlanResponse> getAllActiveTariffPlans();

    TariffPlanResponse updateTariffPlan(UUID id, TariffPlanRequest request);

    /** Sets a tariff plan's {@code isActive} flag to {@code false}. */
    TariffPlanResponse deactivateTariffPlan(UUID id);

    /**
     * Hard-deletes a tariff plan.
     *
     * @throws com.infosys.smartwater.exception.InvalidOperationException if the plan is referenced by any billing cycle
     */
    void deleteTariffPlan(UUID id);
}
