package com.medicinelocator.auth.domain.enums;

/**
 * The fixed menu of capability "boxes" a pharmacy owner can grant to a staff
 * member. Access in downstream services is decided by the PRESENCE of a
 * permission, not by a hard-coded role — the owner ticks whichever boxes a
 * given employee needs.
 *
 * A PHARMACY (owner) account implicitly holds ALL permissions; only
 * PHARMACY_STAFF accounts are constrained by the subset granted to them.
 */
public enum Permission {

    /** Ring up a sale at the POS (sales-service). */
    SELL_MEDICINE,

    /** Generate / re-print receipts (sales-service). */
    PRINT_RECEIPT,

    /** Add / edit / delete medicines in the catalogue (inventory-service). */
    MANAGE_MEDICINES,

    /** Receive stock into inventory (inventory-service). */
    RECEIVE_STOCK,

    /** Create / edit suppliers (inventory-service). */
    MANAGE_SUPPLIERS,

    /** Create purchase orders (inventory-service). */
    MANAGE_PURCHASES,

    /** Read-only view of the catalogue / stock levels. */
    VIEW_INVENTORY,

    /** View dashboards, financial reports and profit figures. */
    VIEW_REPORTS,

    /** Manage the pharmacy's own walk-in customers (sales-service CRM). */
    MANAGE_CUSTOMERS,

    /** Create / edit other staff members (normally owner-only). */
    MANAGE_STAFF
}
