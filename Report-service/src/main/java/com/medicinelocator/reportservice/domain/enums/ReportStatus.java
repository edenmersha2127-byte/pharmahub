package com.medicinelocator.reportservice.domain.enums;

/**
 * Lifecycle of a report.
 *
 * PENDING                    -> just filed, awaiting attention
 * AWAITING_PHARMACY_RESPONSE -> pharmacy asked to respond (right-to-respond)
 * UNDER_REVIEW               -> an admin is looking at it (or pharmacy responded)
 * RESOLVED                   -> admin acted / issue addressed
 * DISMISSED                  -> admin judged it unfounded
 *
 * The first three are "unresolved" and count toward the auto-flag threshold.
 */
public enum ReportStatus {
    PENDING,
    AWAITING_PHARMACY_RESPONSE,
    UNDER_REVIEW,
    RESOLVED,
    DISMISSED;


    public boolean isUnresolved() {
        return this == PENDING || this == AWAITING_PHARMACY_RESPONSE || this == UNDER_REVIEW;
    }
}
