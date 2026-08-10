package com.infosys.smartwater.service;

import com.infosys.smartwater.entity.Household;
import com.infosys.smartwater.entity.TariffTier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Enterprise Billing Engine Service implementing:
 * 1. Tiered Tariff Calculation
 * 2. Metered vs Unmetered (Flat-area fallback) Procurement Cost Distribution
 * 3. Shared Area Water Allocation
 */
@Service
public class BillingEngineService {

    /**
     * Calculates base water consumption charge based on configurable tiers.
     * Support 0-10 kL base rate and >10 kL higher rate.
     */
    public BigDecimal calculateTieredCharge(BigDecimal consumptionKl, List<TariffTier> tiers) {
        if (consumptionKl == null || consumptionKl.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        // If no custom tiers configured for building, use standard default: 0-10 kL @ 10, >10 kL @ 15
        if (tiers == null || tiers.isEmpty()) {
            BigDecimal baseLimit = new BigDecimal("10.00");
            BigDecimal rate1 = new BigDecimal("10.00");
            BigDecimal rate2 = new BigDecimal("15.00");

            if (consumptionKl.compareTo(baseLimit) <= 0) {
                return consumptionKl.multiply(rate1).setScale(2, RoundingMode.HALF_UP);
            } else {
                BigDecimal tier1Charge = baseLimit.multiply(rate1);
                BigDecimal tier2Usage = consumptionKl.subtract(baseLimit);
                BigDecimal tier2Charge = tier2Usage.multiply(rate2);
                return tier1Charge.add(tier2Charge).setScale(2, RoundingMode.HALF_UP);
            }
        }

        // Process custom configured tiers
        BigDecimal totalCharge = BigDecimal.ZERO;
        for (TariffTier tier : tiers) {
            BigDecimal minKl = tier.getMinKl() != null ? tier.getMinKl() : BigDecimal.ZERO;
            BigDecimal maxKl = tier.getMaxKl(); // null means infinity
            BigDecimal rate = tier.getRatePerKl() != null ? tier.getRatePerKl() : BigDecimal.ZERO;

            if (consumptionKl.compareTo(minKl) > 0) {
                BigDecimal tierConsumption;
                if (maxKl != null) {
                    BigDecimal tierLimit = maxKl.subtract(minKl);
                    tierConsumption = consumptionKl.subtract(minKl).min(tierLimit);
                } else {
                    tierConsumption = consumptionKl.subtract(minKl);
                }

                if (tierConsumption.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal tierCost = tierConsumption.multiply(rate);
                    totalCharge = totalCharge.add(tierCost);
                }
            }

            if (tier.getFixedCharge() != null) {
                totalCharge = totalCharge.add(tier.getFixedCharge());
            }
        }

        return totalCharge.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Distributes water procurement cost among households cleanly handling:
     * - Metered households (ratio of metered consumption)
     * - Unmetered households (fallback ratio of flat area)
     * - Mixed buildings (safely avoiding division by zero)
     */
    public Map<UUID, BigDecimal> distributeProcurementCost(
            List<Household> households,
            BigDecimal totalProcurementCost,
            Map<UUID, BigDecimal> householdUsages
    ) {
        Map<UUID, BigDecimal> allocations = new HashMap<>();
        if (households == null || households.isEmpty() || totalProcurementCost == null || totalProcurementCost.compareTo(BigDecimal.ZERO) <= 0) {
            for (Household h : households) {
                allocations.put(h.getId(), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            }
            return allocations;
        }

        List<Household> meteredList = households.stream()
                .filter(h -> Boolean.TRUE.equals(h.getIsMetered()))
                .toList();

        List<Household> unmeteredList = households.stream()
                .filter(h -> !Boolean.TRUE.equals(h.getIsMetered()))
                .toList();

        BigDecimal totalMeteredUsage = meteredList.stream()
                .map(h -> householdUsages.getOrDefault(h.getId(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnmeteredArea = unmeteredList.stream()
                .map(h -> h.getFlatArea() != null ? h.getFlatArea() : new BigDecimal("1000.00"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Case 1: All households are metered or total usage > 0 and no unmetered
        if (unmeteredList.isEmpty() && totalMeteredUsage.compareTo(BigDecimal.ZERO) > 0) {
            for (Household h : meteredList) {
                BigDecimal usage = householdUsages.getOrDefault(h.getId(), BigDecimal.ZERO);
                BigDecimal ratio = usage.divide(totalMeteredUsage, 6, RoundingMode.HALF_UP);
                BigDecimal allocated = totalProcurementCost.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                allocations.put(h.getId(), allocated);
            }
            return allocations;
        }

        // Case 2: All households are unmetered or metered usage is 0
        if (meteredList.isEmpty() && totalUnmeteredArea.compareTo(BigDecimal.ZERO) > 0) {
            for (Household h : unmeteredList) {
                BigDecimal area = h.getFlatArea() != null ? h.getFlatArea() : new BigDecimal("1000.00");
                BigDecimal ratio = area.divide(totalUnmeteredArea, 6, RoundingMode.HALF_UP);
                BigDecimal allocated = totalProcurementCost.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                allocations.put(h.getId(), allocated);
            }
            return allocations;
        }

        // Case 3: Mixed building with both metered and unmetered households
        // Split cost proportionally by active count or metered usage weight
        BigDecimal meteredShareCost = BigDecimal.ZERO;
        BigDecimal unmeteredShareCost = BigDecimal.ZERO;

        int totalCount = households.size();
        int meteredCount = meteredList.size();
        int unmeteredCount = unmeteredList.size();

        if (totalCount > 0) {
            BigDecimal meteredWeight = BigDecimal.valueOf(meteredCount).divide(BigDecimal.valueOf(totalCount), 6, RoundingMode.HALF_UP);
            meteredShareCost = totalProcurementCost.multiply(meteredWeight);
            unmeteredShareCost = totalProcurementCost.subtract(meteredShareCost);
        }

        // Allocate metered portion
        for (Household h : meteredList) {
            BigDecimal usage = householdUsages.getOrDefault(h.getId(), BigDecimal.ZERO);
            if (totalMeteredUsage.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal ratio = usage.divide(totalMeteredUsage, 6, RoundingMode.HALF_UP);
                BigDecimal allocated = meteredShareCost.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                allocations.put(h.getId(), allocated);
            } else {
                BigDecimal equalShare = meteredShareCost.divide(BigDecimal.valueOf(meteredCount), 2, RoundingMode.HALF_UP);
                allocations.put(h.getId(), equalShare);
            }
        }

        // Allocate unmetered portion
        for (Household h : unmeteredList) {
            BigDecimal area = h.getFlatArea() != null ? h.getFlatArea() : new BigDecimal("1000.00");
            if (totalUnmeteredArea.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal ratio = area.divide(totalUnmeteredArea, 6, RoundingMode.HALF_UP);
                BigDecimal allocated = unmeteredShareCost.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                allocations.put(h.getId(), allocated);
            } else {
                BigDecimal equalShare = unmeteredShareCost.divide(BigDecimal.valueOf(unmeteredCount), 2, RoundingMode.HALF_UP);
                allocations.put(h.getId(), equalShare);
            }
        }

        return allocations;
    }

    /**
     * Allocates shared area water costs equally across active households.
     */
    public BigDecimal calculateSharedAreaAllocation(BigDecimal totalSharedCost, int activeHouseholdCount) {
        if (totalSharedCost == null || totalSharedCost.compareTo(BigDecimal.ZERO) <= 0 || activeHouseholdCount <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return totalSharedCost.divide(BigDecimal.valueOf(activeHouseholdCount), 2, RoundingMode.HALF_UP);
    }
}
