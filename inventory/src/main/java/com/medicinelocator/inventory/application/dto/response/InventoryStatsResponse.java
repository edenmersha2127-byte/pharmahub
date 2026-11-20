package com.medicinelocator.inventory.application.dto.response;

import java.math.BigDecimal;

/**
 * Inventory-side dashboard counters: catalogue size, stock/expiry alerts, and
 * stock-level totals. Computed server-side over the whole catalogue (not just
 * the first page) so the figures stay correct for large inventories.
 */
public record InventoryStatsResponse(
        long totalMedicines,
        long lowStockCount,
        long expiringSoonCount,
        long expiredCount,
        long inStockCount,
        long outOfStockCount,
        long totalStockUnits,
        BigDecimal totalInventoryValue
) {
}
