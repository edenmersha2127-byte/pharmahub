package com.medicinelocator.salesservice.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Talks to the inventory-service to deduct stock as part of a POS sale. The
 * implementation forwards the caller's gateway identity headers so inventory can
 * enforce SELL_MEDICINE against the same staff member.
 */
public interface InventoryClient {

    /**
     * Deduct a whole cart atomically. Returns the affected medicines (with the
     * authoritative selling price and name) so the sale can be recorded with a
     * price snapshot. Throws {@code CheckoutFailedException} on any rejection.
     */
    List<DeductedItem> deductForSale(UUID pharmacyId, List<Line> items);

    /** One requested cart line. */
    record Line(UUID medicineId, int quantity) {}

    /** Result of deducting one line: the medicine's snapshot at sale time. */
    record DeductedItem(UUID medicineId, String medicineName, BigDecimal unitPrice, BigDecimal purchasePrice) {}
}
