package com.medicinelocator.salesservice.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Aggregated sales figures over a date range for the dashboard / financial
 * reports: revenue, profit (sell - buy), and number of sales.
 */
public record SalesAnalyticsResponse(
        LocalDate from,
        LocalDate to,
        long salesCount,
        BigDecimal revenue,
        BigDecimal profit
) {
}
