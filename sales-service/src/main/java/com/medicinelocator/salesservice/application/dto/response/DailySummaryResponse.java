package com.medicinelocator.salesservice.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Today's sales figures for the dashboard: sales count + revenue, plus the
 * previous day's revenue and the day-over-day percentage change (the "Today's
 * Sales ↑ x% from yesterday" chip). {@code revenueDeltaPct} is {@code null}
 * when yesterday had no revenue (nothing to compare against).
 */
public record DailySummaryResponse(
        LocalDate date,
        long salesCount,
        BigDecimal totalRevenue,
        BigDecimal previousDayRevenue,
        BigDecimal revenueDeltaPct
) {
}
