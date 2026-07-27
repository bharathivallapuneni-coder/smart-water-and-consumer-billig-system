package com.infosys.smartwater.entity.enums;

/**
 * Defines the approval status for user registration applications (specifically for {@link Role#BUILDING_OWNER}).
 */
public enum ApprovalStatus {

    /**
     * Registration application submitted by Building Owner, pending Superadmin review.
     */
    PENDING,

    /**
     * Application accepted by Superadmin; login access granted.
     */
    APPROVED,

    /**
     * Application rejected by Superadmin; login access denied.
     */
    REJECTED
}
