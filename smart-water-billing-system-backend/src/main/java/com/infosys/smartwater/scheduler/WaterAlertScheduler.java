package com.infosys.smartwater.scheduler;

import com.infosys.smartwater.entity.Household;
import com.infosys.smartwater.entity.WaterUsage;
import com.infosys.smartwater.repository.HouseholdRepository;
import com.infosys.smartwater.repository.WaterUsageRepository;
import com.infosys.smartwater.service.AlertEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Spring @Scheduled component evaluating household water usage periodically.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WaterAlertScheduler {

    private final HouseholdRepository householdRepository;
    private final WaterUsageRepository waterUsageRepository;
    private final AlertEngineService alertEngineService;

    /**
     * Periodically evaluate active household consumption.
     * Schedule configured via application properties (default: fixed delay 1 hour).
     */
    @Scheduled(fixedDelayString = "${application.alerts.fixed-delay:3600000}", initialDelay = 30000)
    public void runPeriodicConsumptionEvaluation() {
        log.info("Starting scheduled household water consumption & leak alert evaluation...");
        List<Household> activeHouseholds = householdRepository.findAll().stream()
                .filter(h -> Boolean.TRUE.equals(h.getIsActive()))
                .toList();

        for (Household household : activeHouseholds) {
            try {
                // Fetch latest meter reading for household
                Optional<WaterUsage> latestOpt = waterUsageRepository.findLatestByHouseholdId(household.getId());

                if (latestOpt.isPresent()) {
                    WaterUsage latestReading = latestOpt.get();
                    BigDecimal latestUnits = latestReading.getUnitsConsumed();

                    alertEngineService.evaluateHouseholdConsumption(household, latestUnits, null);
                }
            } catch (Exception e) {
                log.error("Error evaluating alerts for household ID {}: {}", household.getId(), e.getMessage());
            }
        }
        log.info("Finished scheduled household water consumption evaluation.");
    }
}
