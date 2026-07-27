package com.infosys.smartwater.entity.enums;

/**
 * Defines the roles available in the Smart Water Billing System.
 *
 * <ul>
 *   <li>{@link #SUPERADMIN}     — Built-in system administrator: full access to approve/reject
 *                                 building owner applications and manage top-level configuration.</li>
 *   <li>{@link #BUILDING_OWNER} — Apartment/Building owner: manages assigned apartments, households,
 *                                 meter readings, tariff plans, and billing cycles. Requires Superadmin approval.</li>
 *   <li>{@link #RESIDENT}       — Registered resident: view own household data, usage history,
 *                                 and bills. Account created by Building Owner.</li>
 * </ul>
 */
public enum Role {

    /**
     * Built-in super administrator.
     */
    SUPERADMIN,

    /**
     * Legacy alias for backward compatibility.
     */
    ADMIN,

    /**
     * Building owner manager.
     */
    BUILDING_OWNER,

    /**
     * Household resident account holder.
     */
    RESIDENT
}
