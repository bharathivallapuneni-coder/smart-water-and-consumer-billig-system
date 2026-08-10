package com.infosys.smartwater.scheduler;

import com.infosys.smartwater.entity.Household;
import com.infosys.smartwater.entity.WaterUsage;
import com.infosys.smartwater.repository.HouseholdRepository;
import com.infosys.smartwater.repository.WaterUsageRepository;
import com.infosys.smartwater.service.AlertEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

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
     * Fixed delay: 1 hour (3600000 ms), initial delay: 30 seconds.
     */
    @Scheduled(fixedDelay = 3600000, initialDelay = 30000)
    public void runPeriodicConsumptionEvaluation() {
        log.info("Starting scheduled household water consumption & leak alert evaluation...");
        List<Household> activeHouseholds = householdRepository.findByIsActiveTrue();

        for (Household household : activeHouseholds) {
            try {
                // Fetch latest meter reading for household
                List<WaterUsage> usages = waterUsageRepository
                        .findByHouseholdIdOrderByReadingDateDesc(household.getId(), PageRequest.of(0, 1))
                        .getContent();

                if (!usages.isEmpty()) {
                    BigDecimal latestUnits = usages.get(0).getUnitsConsumed();
                    alertEngineService.evaluateHouseholdConsumption(household, latestUnits);
                }
            } catch (Exception e) {
                log.error("Error evaluating alerts for household ID {}: {}", household.getId(), e.getMessage());
            }
        }
        log.info("Finished scheduled household water consumption evaluation.");
    }
}
