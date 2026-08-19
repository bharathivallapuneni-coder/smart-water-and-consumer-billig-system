package com.infosys.smartwater.dto.response;

import com.infosys.smartwater.entity.Notification;
import com.infosys.smartwater.entity.enums.AlertType;
import com.infosys.smartwater.entity.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {
    private UUID id;
    private AlertType alertType;
    private Severity severity;
    private String title;
    private String message;
    private String tariffTier;
    private BigDecimal currentConsumption;
    private BigDecimal averageConsumption;
    private BigDecimal standardDeviation;
    private Boolean isRead;
    private Boolean isResolved;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public static AlertResponse fromEntity(Notification entity) {
        if (entity == null) return null;
        return AlertResponse.builder()
                .id(entity.getId())
                .alertType(entity.getAlertType())
                .severity(entity.getSeverity())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .tariffTier(entity.getTariffTier())
                .currentConsumption(entity.getCurrentConsumption())
                .averageConsumption(entity.getAverageConsumption())
                .standardDeviation(entity.getStandardDeviation())
                .isRead(entity.getIsRead())
                .isResolved(entity.getIsResolved())
                .createdAt(entity.getCreatedAt())
                .resolvedAt(entity.getResolvedAt())
                .build();
    }
}
