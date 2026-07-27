package com.infosys.smartwater.dto.request;

import com.infosys.smartwater.entity.enums.BillingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for updating the payment status of a {@code BillingCycle}.
 *
 * <p>Used by {@code PATCH /api/billing-cycles/{id}/status}.
 *
 * <p>Business rules enforced in the service layer:
 * <ul>
 *   <li>Transitioning to {@link BillingStatus#PAID} requires a {@code paidDate}.</li>
 *   <li>A PAID cycle cannot be transitioned back to PENDING or OVERDUE.</li>
 *   <li>Only ADMIN can manually mark a cycle as OVERDUE
 *       (normally done by the scheduled job).</li>
 * </ul>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to update the payment status of a billing cycle")
public class UpdateBillingStatusRequest {

    @Schema(description = "The new billing status to set",
            example = "PAID",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Status is required")
    private BillingStatus status;

    @Schema(description = "Date on which payment was received — required when status is PAID",
            example = "2026-08-15",
            nullable = true)
    private LocalDate paidDate;
}
