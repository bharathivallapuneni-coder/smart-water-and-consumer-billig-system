package com.infosys.smartwater.service;

import com.infosys.smartwater.entity.Household;
import com.infosys.smartwater.entity.HouseholdInvoice;
import com.infosys.smartwater.entity.Notification;
import com.infosys.smartwater.repository.HouseholdInvoiceRepository;
import com.infosys.smartwater.repository.HouseholdRepository;
import com.infosys.smartwater.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertEngineService {

    private final HouseholdRepository householdRepository;
    private final HouseholdInvoiceRepository invoiceRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Transactional
    public void evaluateHouseholdConsumption(Household household, BigDecimal currentConsumptionKl) {
        if (household == null || currentConsumptionKl == null) return;

        BigDecimal threshold = household.getAlertThresholdKl() != null ? household.getAlertThresholdKl() : new BigDecimal("20.00");

        // 1. Configurable Usage Threshold Alert
        if (currentConsumptionKl.compareTo(threshold) > 0) {
            String title = "High Water Usage Alert";
            String msg = String.format(
                    "High Water Usage Alert\nYour water consumption for the current billing cycle has reached %.2f kL, exceeding your configured threshold of %.2f kL.",
                    currentConsumptionKl.doubleValue(), threshold.doubleValue()
            );

            createNotificationAndEmail(household, title, msg, "HIGH_USAGE");
        }

        // 2. Statistical Outlier & Leak Detection (2 * Standard Deviation)
        evaluateLeakOutlier(household, currentConsumptionKl);
    }

    @Transactional
    public void evaluateLeakOutlier(Household household, BigDecimal currentConsumptionKl) {
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
            String title = "Potential Water Leak Warning";
            String msg = String.format(
                    "Anomalous Water Usage Detected!\nYour current consumption of %.2f kL exceeds your historical average (%.2f kL ± %.2f kL). Potential leak detected at flat %s.",
                    currentVal, mean, stdDev, household.getHouseholdNumber()
            );

            createNotificationAndEmail(household, title, msg, "LEAK_ALERT");
        }
    }

    private void createNotificationAndEmail(Household household, String title, String msg, String type) {
        if (household.getUser() != null) {
            // Save in-app notification
            Notification notification = Notification.builder()
                    .user(household.getUser())
                    .apartment(household.getApartment())
                    .title(title)
                    .message(msg)
                    .notificationType(type)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);

            // Send Email if recipient email exists
            if (household.getUser().getEmail() != null) {
                emailService.sendEmailAlert(household.getUser().getEmail(), title, msg);
            }
        }
    }
}
