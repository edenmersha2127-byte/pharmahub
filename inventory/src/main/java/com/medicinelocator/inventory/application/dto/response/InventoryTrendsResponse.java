package com.medicinelocator.inventory.application.dto.response;

import java.math.BigDecimal;

/**
 * Percentage change of catalogue size and inventory value between now and the
 * baseline snapshot ~30 days ago (the dashboard's "↑ x% from last month" chips).
 * A field is {@code null} when no baseline snapshot exists yet (snapshots only
 * start accruing once the daily job has run) or the baseline was zero.
 */
public record InventoryTrendsResponse(
        BigDecimal medicinesDeltaPct,
        BigDecimal inventoryValueDeltaPct
) {
}
