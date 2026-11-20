package com.medicinelocator.prescription.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Port for resolving extracted prescription medicine names against a specific
 * pharmacy's own inventory (inventory-service's pharmacy-scoped lookup), so the
 * dispensing pharmacist gets ready-to-sell items (id, price, stock).
 */
public interface InventoryLookupClient {

    /** Name-match candidates in {@code pharmacyId}'s catalogue for {@code query}; empty on no match/error. */
    List<Medicine> lookup(UUID pharmacyId, String query);

    /** A matched medicine from the pharmacy's own inventory. */
    record Medicine(UUID medicineId, String name, BigDecimal price,
                    int stockQuantity, boolean requiresPrescription) {
    }
}
