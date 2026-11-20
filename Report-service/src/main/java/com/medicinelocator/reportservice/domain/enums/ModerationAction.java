package com.medicinelocator.reportservice.domain.enums;

/**
 * The moderation actions an admin (super ADMIN or REPORT_ADMIN) can take, each
 * of which is recorded in the audit log for accountability.
 */
public enum ModerationAction {
    RESOLVE_REPORT,
    DISMISS_REPORT,
    WARN_PHARMACY,
    SUSPEND_PHARMACY,
    LIFT_SUSPENSION
}
