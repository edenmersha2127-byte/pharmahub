package com.medicinelocator.salesservice.infrastructure.security;

/**
 * Capability "boxes" (see auth's {@code Permission} enum) that the sales-service
 * enforces. Values MUST match the enum names exactly — they arrive as plain
 * strings in the {@code X-User-Permissions} header issued by the gateway from
 * the staff member's JWT. A PHARMACY (owner) implicitly holds all of them.
 */
public final class PermissionConstants {

    /** Ring up a sale at the POS. */
    public static final String SELL_MEDICINE = "SELL_MEDICINE";

    /** Generate / re-print receipts. */
    public static final String PRINT_RECEIPT = "PRINT_RECEIPT";

    /** Manage the pharmacy's own walk-in customers. */
    public static final String MANAGE_CUSTOMERS = "MANAGE_CUSTOMERS";

    /** View dashboards / sales reports. */
    public static final String VIEW_REPORTS = "VIEW_REPORTS";

    private PermissionConstants() {
    }
}
