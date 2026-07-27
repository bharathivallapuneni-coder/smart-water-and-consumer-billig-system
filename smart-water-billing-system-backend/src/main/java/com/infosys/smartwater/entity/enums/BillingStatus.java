package com.infosys.smartwater.entity.enums;

/**
 * Lifecycle states of a monthly {@code BillingCycle}.
 *
 * <ul>
 *   <li>{@link #PENDING}  — Bill generated; payment not yet received.</li>
 *   <li>{@link #PAID}     — Payment confirmed; {@code paidDate} is set.</li>
 *   <li>{@link #OVERDUE}  — Due date has passed without payment.
 *                           Triggered by a scheduled job or on-read evaluation.</li>
 * </ul>
 *
 * <p>Stored as a {@code VARCHAR(20)} in the database via {@code @Enumerated(EnumType.STRING)}.</p>
 */
public enum BillingStatus {

    /**
     * Bill has been generated; awaiting payment by the resident.
     */
    PENDING,

    /**
     * Payment received and confirmed; billing cycle is closed.
     */
    PAID,

    /**
     * Due date has passed without payment being recorded.
     */
    OVERDUE
}
