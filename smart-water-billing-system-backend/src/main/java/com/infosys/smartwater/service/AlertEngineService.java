package com.infosys.smartwater.service;

import com.infosys.smartwater.entity.BillingCycle;
import com.infosys.smartwater.entity.Household;
import com.infosys.smartwater.entity.HouseholdInvoice;
import com.infosys.smartwater.entity.Notification;
import com.infosys.smartwater.entity.TariffTier;
import com.infosys.smartwater.entity.enums.AlertType;
import com.infosys.smartwater.entity.enums.Severity;
import com.infosys.smartwater.repository.HouseholdInvoiceRepository;
import com.infosys.smartwater.repository.HouseholdRepository;
import com.infosys.smartwater.repository.NotificationRepository;
import com.infosys.smartwater.repository.TariffTierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertEngineService {

    private final HouseholdRepository householdRepository;
    private final HouseholdInvoiceRepository invoiceRepository;
    private final NotificationRepository notificationRepository;
    private final TariffTierRepository tariffTierRepository;
    private final EmailService emailService;

    @Transactional
    public void evaluateHouseholdConsumption(Household household, BigDecimal currentConsumptionKl, BillingCycle cycle) {
        if (household == null || currentConsumptionKl == null || household.getApartment() == null) return;

        UUID apartmentId = household.getApartment().getId();
        UUID cycleId = cycle != null ? cycle.getId() : null;

        // 1. DYNAMIC TARIFF CONFIGURATION EVALUATION (SINGLE SOURCE OF TRUTH)
        List<TariffTier> tiers = tariffTierRepository.findByApartmentIdOrderByMinKlAsc(apartmentId);
        evaluateTariffBasedAlert(household, currentConsumptionKl, tiers, cycle);

        // 2. STATISTICAL ABNORMAL USAGE / LEAK DETECTION (2 * STANDARD DEVIATION)
        evaluateLeakOutlier(household, currentConsumptionKl, cycle);
    }

    @Transactional
    public void evaluateTariffBasedAlert(Household household, BigDecimal currentConsumptionKl, List<TariffTier> tiers, BillingCycle cycle) {
        if (tiers == null || tiers.isEmpty()) {
            // Default fallback if building owner hasn't configured custom tiers yet
            // Base: 0-10 kL, Medium: 11-25 kL, High: 26+ kL
            double consumption = currentConsumptionKl.doubleValue();
            if (consumption >= 26.0) {
                createAlertIfNotExists(
                        household, cycle, AlertType.CRITICAL_HIGH_CONSUMPTION, Severity.CRITICAL,
                        "⚠ CRITICAL HIGH CONSUMPTION",
                        String.format("Your current water consumption is %.1f kL. You have entered the High Consumption tier (26+ kL). Please reduce usage to prevent high charges.", consumption),
                        "High Consumption Tier (26+ kL)", currentConsumptionKl, null, null
                );
            } else if (consumption >= 11.0) {
                createAlertIfNotExists(
                        household, cycle, AlertType.HIGH_CONSUMPTION, Severity.WARNING,
                        "⚠ HIGH WATER CONSUMPTION",
                        String.format("Your current water consumption is %.1f kL. You are currently in the Medium Consumption tier (11–25 kL). Please monitor your water usage.", consumption),
                        "Medium Consumption Tier (11–25 kL)", currentConsumptionKl, null, null
                );
            }
            return;
        }

        // Dynamically find applicable tier index from database
        double consumption = currentConsumptionKl.doubleValue();
        int tierIndex = -1;
        TariffTier activeTier = null;

        for (int i = 0; i < tiers.size(); i++) {
            TariffTier tier = tiers.get(i);
            double minKl = tier.getMinKl() != null ? tier.getMinKl().doubleValue() : 0.0;
            Double maxKl = tier.getMaxKl() != null ? tier.getMaxKl().doubleValue() : null;

            if (consumption > minKl && (maxKl == null || consumption <= maxKl)) {
                tierIndex = i;
                activeTier = tier;
                break;
            }
        }

        // If tierIndex == 0 (Base Tier e.g. 0-10 kL), no warning is generated
        if (tierIndex > 0 && activeTier != null) {
            String tierRangeStr = activeTier.getMaxKl() != null
                    ? String.format("%s (%.0f–%.0f kL)", activeTier.getTierName(), activeTier.getMinKl().doubleValue() + 1, activeTier.getMaxKl().doubleValue())
                    : String.format("%s (%.0f+ kL)", activeTier.getTierName(), activeTier.getMinKl().doubleValue() + 1);

            boolean isHighestTier = (tierIndex == tiers.size() - 1);
            AlertType alertType = isHighestTier ? AlertType.CRITICAL_HIGH_CONSUMPTION : AlertType.HIGH_CONSUMPTION;
            Severity severity = isHighestTier ? Severity.CRITICAL : Severity.WARNING;
            String title = isHighestTier ? "⚠ CRITICAL HIGH CONSUMPTION" : "⚠ HIGH WATER CONSUMPTION";
            String msg = isHighestTier
                    ? String.format("Your current water consumption is %.1f kL. You have entered the %s. Please reduce usage to avoid higher charges.", consumption, tierRangeStr)
                    : String.format("Your current water consumption is %.1f kL. You are currently in the %s. Please monitor your water usage.", consumption, tierRangeStr);

            createAlertIfNotExists(
                    household, cycle, alertType, severity, title, msg, tierRangeStr, currentConsumptionKl, null, null
            );
        }
    }

    @Transactional
    public void evaluateLeakOutlier(Household household, BigDecimal currentConsumptionKl, BillingCycle cycle) {
        List<HouseholdInvoice> pastInvoices = invoiceRepository
                .findByHouseholdIdOrderByGeneratedAtDesc(household.getId(), PageRequest.of(0, 10))
                .getContent();

        if (pastInvoices.size() < 2) {
            log.info("Skipping 2-sigma leak calculation for household {} — insufficient historical data (found {} cycles).", household.getHouseholdNumber(), pastInvoices.size());
            return;
        }

        List<Double> pastUsages = pastInvoices.stream()
                .map(inv -> inv.getMeteredConsumptionKl() != null ? inv.getMeteredConsumptionKl().doubleValue() : 0.0)
                .toList();

        double sum = 0.0;
        for (double val : pastUsages) sum += val;
        double mean = sum / pastUsages.size();

        double varianceSum = 0.0;
        for (double val : pastUsages) {
            varianceSum += Math.pow(val - mean, 2);
        }
        double stdDev = Math.sqrt(varianceSum / (pastUsages.size() - 1));

        double leakThreshold = mean + (2 * stdDev);
        double currentVal = currentConsumptionKl.doubleValue();

        if (currentVal > leakThreshold && currentVal > 10.0) {
            String title = "🚨 POSSIBLE WATER LEAK DETECTED";
            String msg = String.format(
                    "🚨 Possible Water Leak Detected\n\nYour current water consumption (%.1f kL) is significantly higher than your normal household usage (Average: %.1f kL).\n\nPlease check your taps, pipes, tanks, and other water connections for possible leakage.",
                    currentVal, mean
            );

            createAlertIfNotExists(
                    household, cycle, AlertType.POSSIBLE_WATER_LEAK, Severity.CRITICAL,
                    title, msg, "Abnormal Usage", currentConsumptionKl,
                    BigDecimal.valueOf(mean).setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.valueOf(stdDev).setScale(2, RoundingMode.HALF_UP)
            );
        }
    }

    private void createAlertIfNotExists(
            Household household,
            BillingCycle cycle,
            AlertType alertType,
            Severity severity,
            String title,
            String message,
            String tariffTier,
            BigDecimal currentConsumption,
            BigDecimal avgConsumption,
            BigDecimal stdDev
    ) {
        if (household == null || household.getUser() == null) return;

        UUID cycleId = cycle != null ? cycle.getId() : null;

        // Prevent duplicate alerts for the same (Household + AlertType + BillingCycle)
        if (cycleId != null && notificationRepository.existsByHouseholdIdAndAlertTypeAndBillingCycleId(household.getId(), alertType, cycleId)) {
            log.info("Duplicate alert prevented for household {} and alert type {}", household.getHouseholdNumber(), alertType);
            return;
        }

        Notification notification = Notification.builder()
                .user(household.getUser())
                .apartment(household.getApartment())
                .household(household)
                .billingCycle(cycle)
                .alertType(alertType)
                .severity(severity)
                .title(title)
                .message(message)
                .notificationType(alertType.name())
                .tariffTier(tariffTier)
                .currentConsumption(currentConsumption)
                .averageConsumption(avgConsumption)
                .standardDeviation(stdDev)
                .isRead(false)
                .isResolved(false)
                .build();

        notificationRepository.save(notification);
        log.info("Persisted resident alert ID {} ({}) for household {}", notification.getId(), alertType, household.getHouseholdNumber());

        if (household.getUser().getEmail() != null) {
            emailService.sendEmailAlert(household.getUser().getEmail(), title, message);
        }
    }
}
