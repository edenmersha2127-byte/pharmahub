package com.medicinelocator.paymentservice.domain.enums;

/**
 * Lifecycle of a pharmacy's subscription.
 *
 * TRIAL     -> free trial period, full access
 * PENDING   -> a paid subscription created, awaiting payment confirmation
 * ACTIVE    -> paid and currently valid
 * EXPIRED   -> trial or paid period elapsed; access revoked
 * CANCELLED -> ended early
 *
 * TRIAL and ACTIVE mean the pharmacy currently has access.
 */
public enum SubscriptionStatus {
    TRIAL,
    PENDING,
    ACTIVE,
    EXPIRED,
    CANCELLED;

    /** True when this status grants the pharmacy access to the app. */
    public boolean grantsAccess() {
        return this == TRIAL || this == ACTIVE;
    }
}
