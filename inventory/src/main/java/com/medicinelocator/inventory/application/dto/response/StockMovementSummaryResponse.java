package com.medicinelocator.inventory.application.dto.response;

/**
 * One period's pharmacy-wide stock movement totals for the dashboard's
 * Stock Movement chart. {@code unitsIn} aggregates PURCHASE/INITIAL (and
 * positive ADJUSTMENT) movements; {@code unitsOut} aggregates SALE/EXPIRED
 * (and negative ADJUSTMENT). {@code net = unitsIn - unitsOut}.
 */
public record StockMovementSummaryResponse(
        String period,
        long unitsIn,
        long unitsOut,
        long net
) {
}
