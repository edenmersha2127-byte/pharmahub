package com.medicinelocator.inventory.infrastructure.security;

/**
 * Capability "boxes" (see auth's {@code Permission} enum) that the
 * inventory-service enforces. Values MUST match the enum names exactly, because
 * they arrive as plain strings in the {@code X-User-Permissions} header issued
 * by the gateway from the staff member's JWT.
 *
 * <p>A PHARMACY (owner) account implicitly holds all of these; only
 * PHARMACY_STAFF are constrained to the subset the owner ticked for them.</p>
 */
public final class PermissionConstants {

    /** Add / edit / delete medicines in the catalogue. */
    public static final String MANAGE_MEDICINES = "MANAGE_MEDICINES";

    /** Read-only view of the catalogue / stock levels. */
    public static final String VIEW_INVENTORY = "VIEW_INVENTORY";

    /** Ring up a sale (drives stock deduction). */
    public static final String SELL_MEDICINE = "SELL_MEDICINE";

    /** Receive stock into inventory (purchase receiving). */
    public static final String RECEIVE_STOCK = "RECEIVE_STOCK";

    /** Create / edit suppliers. */
    public static final String MANAGE_SUPPLIERS = "MANAGE_SUPPLIERS";

    /** Create / manage purchase orders. */
    public static final String MANAGE_PURCHASES = "MANAGE_PURCHASES";

    private PermissionConstants() {
    }
}
